package com.kendelong.util.concurrency;

import java.util.concurrent.atomic.AtomicInteger;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.jmx.export.annotation.ManagedAttribute;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedResource;

/**
 * This is an interceptor that will prevent more than a certain number of threads accessing a given resource
 * at the same time.  Usage could be
 * 
 * <pre>
 * {@code
 * 	<bean id="concurrencyThrottleJmxExporter" class="com.kendelong.util.spring.interceptor.JmxExportingBeanNameAutoproxyCreator" lazy-init="false">
		<property name="jmxInterceptorClass" value="com.kendelong.util.concurrency.ConcurrencyLimitingInterceptor" />
		<property name="exporter" ref="callableJmxExporter" />
		<property name="prefix" value="myapp.admin.concurrencythrottle" />
		<property name="serviceName" value="concurrencyThrottle"/>
		<property name="beanNames">
			<list>
				<value>MyService1</value>
			</list>
		</property>
	</bean>
	}
	</pre>
 *
 *
 * @author kdelong
 */
@Aspect
@ManagedResource(description="An interceptor that limits the number of threads that can be in a component at any one time")
public class ConcurrencyLimitingAspect implements MethodInterceptor
{
	private final AtomicInteger threadLimit = new AtomicInteger(20);
	private final AtomicInteger threadCount = new AtomicInteger();
	private final AtomicInteger tripCount = new AtomicInteger();

	@Around("@annotation(com.kendelong.util.concurrency.ConcurrencyThrottle)")
	public Object applyConcurrencyThrottle(ProceedingJoinPoint pjp) throws Throwable
	{
		Object result;
		try
		{
			int threadNum = threadCount.incrementAndGet();
			if(threadNum > getThreadLimit())
			{
				tripCount.incrementAndGet();
				throw new ConcurrencyLimitExceededException("This thread exceeded the thread limit of " + getThreadLimit());
			}
			result = pjp.proceed();
		}
		finally
		{
			threadCount.decrementAndGet();
		}
		
		return result;
		
	}

	
	@Override
	public Object invoke(MethodInvocation methodInvocation) throws Throwable
	{
		Object result;
		try
		{
			int threadNum = threadCount.incrementAndGet();
			if(threadNum > getThreadLimit())
			{
				tripCount.incrementAndGet();
				throw new ConcurrencyLimitExceededException("This thread exceeded the thread limit of " + getThreadLimit());
			}
			result = methodInvocation.proceed();
		}
		finally
		{
			threadCount.decrementAndGet();
		}
		
		return result;
	}

	@ManagedAttribute(description="The maximum number of threads allowed in the component at one time")
	public int getThreadLimit()
	{
		return threadLimit.get();
	}
	
	@ManagedAttribute
	public void setThreadLimit(int val)
	{
		threadLimit.set(val);
	}
	
	@ManagedAttribute(description="The current number of threads in the component")
	public int getThreadCount()
	{
		return threadCount.get();
	}
	
	@ManagedAttribute(description="The number of accesses that were rejected due to the maximum number of threads being reached")
	public int getTripCount()
	{
		return tripCount.get();
	}
	
	@ManagedOperation(description="Reset the trip count to zero")
	public void resetStatistics()
	{
		tripCount.set(0);
	}
	
}
