package com.kendelong.util.retry;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.core.Ordered;
import org.springframework.jmx.export.annotation.ManagedAttribute;
import org.springframework.jmx.export.annotation.ManagedResource;

/**
 * Make sure the "order" property on this is lower than the transaction interceptor's order.
 * That way the "bad" transaction is rolled back, then on the way out this interceptor is hit,
 * and then it retries with a new transaction.
 * 
 * See Spring 3.0 reference PDF, end of section 7.2
 * 
 * Configuration is like
 * <pre>
 * ${@code
 * 	<bean id="concurrentRetryInterceptor" class="com.kendelong.util.concurrency.ConcurrentOperationRetryInterceptor">
		<property name="order" value="1" />
		<property name="exceptionClassesToRetry">
			<list>
				<value>org.hibernate.StaleObjectStateException</value>
				<value>org.springframework.orm.hibernate4.HibernateOptimisticLockingFailureException</value>
				<value>java.io.EOFException</value>
				<value>com.mysql.jdbc.exceptions.jdbc4.CommunicationsException</value>
				<value>org.hibernate.PessimisticLockException</value>
				<value>org.springframework.dao.CannotAcquireLockException</value>
			</list>
		</property>
	</bean>
 * }
 * </pre>
 *
 * @author kdelong
 */
@Aspect
@ManagedResource(description="Retry interceptor that retries operations when a known exception is thrown")
public class RetryInterceptor implements Ordered
{
	private final AtomicInteger maxRetries = new AtomicInteger(2);
	private int order = 1;
	private List<Class<? extends Exception>> exceptionClassesToRetry = new ArrayList<Class<? extends Exception>>();
	
	// Instrumentation
	private final AtomicInteger accesses = new AtomicInteger();
	private final AtomicInteger retriedOperations = new AtomicInteger();
	private final AtomicInteger failedOperations = new AtomicInteger();
	private final Map<String, AtomicInteger> failedMethods = new ConcurrentHashMap<String, AtomicInteger>(); 
	
	private final Log logger = LogFactory.getLog(this.getClass());

	@Around("@annotation(com.kendelong.util.retry.RetryableOperation)")
	public Object doConcurrentOperation(ProceedingJoinPoint pjp) throws Throwable
	{
		int numAttempts = 0;
		Exception concurrencyFailureException;
		do
		{
			accesses.incrementAndGet();
			if(numAttempts > 0) retriedOperations.incrementAndGet();

			numAttempts++;
			try
			{
				return pjp.proceed();
			}
			catch(Exception e)
			{
				// if it's not in the list of exceptions to catch and retry, then keep going
				// throw the exceptions to the client and stop counting
				if(!exceptionClassesToRetry.contains(e.getClass()))
				{
					throw e;
				}
				
				// Otherwise, count it as a concurrency failure.
				concurrencyFailureException = e;				
				logger.warn("Exception [" + e.getMessage() + "] caught, attempt number [" + numAttempts + "]");
				logConcurrencyFailure(pjp);
			}
		} while(numAttempts <= this.getMaxRetries());
		
		logger.warn("Max retries reached; rethrowing exception to client");
		failedOperations.incrementAndGet();
		throw concurrencyFailureException;
	}

	private void logConcurrencyFailure(ProceedingJoinPoint pjp)
	{
		String key = pjp.getSignature().toShortString();
		AtomicInteger counter = failedMethods.get(key);
		if(counter == null)
		{
			// yeah, this block is not threadsafe, so what
			counter = new AtomicInteger();
			failedMethods.put(key, counter);
		}
		counter.incrementAndGet();
	}

	public void setMaxRetries(int maxRetries)
	{
		this.maxRetries.set(maxRetries);
	}

	@Override
	public int getOrder()
	{
		return this.order;
	}

	public void setOrder(int order)
	{
		this.order = order;
	}

	public void setExceptionClassesToRetry(List<Class<? extends Exception>> exceptionClasses)
	{
		this.exceptionClassesToRetry = exceptionClasses;
	}

	public List<Class<? extends Exception>> getExceptionClassesToRetry()
	{
		return exceptionClassesToRetry;
	}
	
	@ManagedAttribute(description="Times this interceptor was accessed")
	public int getNumberOfAccesses()
	{
		return accesses.get();
	}
	
	@ManagedAttribute(description="Operations that were retried because an exception in the retry list was caught")
	public int getRetriedOperations()
	{
		return retriedOperations.get();
	}
	
	@ManagedAttribute(description="Operations that reached the retry limit and then were failed because all retries failed")
	public int getFailedOperations()
	{
		return failedOperations.get();
	}
	
	@ManagedAttribute(description="Number of times the interceptor will retry a failed operation")
	public int getMaxRetries()
	{
		return maxRetries.get();
	}
	
	@ManagedAttribute(description="The names of the methods that failed with concurrent exceptions")
	public String getFailedMethods()
	{
		StringBuilder builder = new StringBuilder();
		builder.append("<script src=\"/js/sorttable.js\"></script>");
		builder.append("<script src=\"/webassets/js/sorttable.js\"></script>");
		builder.append("<table border=\"2\" class=\"sortable\"><tbody><tr><th>Method Name</th><th>Failures</th></tr>");
		for(String methodName : failedMethods.keySet())
		{
			int val = failedMethods.get(methodName).get();

			builder.append("<tr>");
			builder.append("<td style=\"text-align: left;\">").append(methodName).append("</td>");
			builder.append("<td style=\"text-align: right;\">").append(val).append("</td>");
			builder.append("</tr>");
		}
		builder.append("</tbody></table>");
		return builder.toString();
	}

}
