package com.kendelong.util.concurrency;

import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.jmx.export.annotation.ManagedAttribute;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedResource;

import com.kendelong.util.monitoring.graphite.GraphiteClient;

/**
 * This is an interceptor that will prevent more than a certain number of threads accessing a given resource
 * at the same time.  Usage could be
 * 
 * <pre>
 * {@code
	<bean class="com.kendelong.util.concurrency.ConcurrencyLimitingAspect" scope="prototype">
		<property name="graphiteClient" ref="graphiteClient"/>
	</bean>
			
	<bean class="com.kendelong.util.spring.JmxExportingAspectPostProcessor" lazy-init="false">
		<property name="mbeanExporter" ref="mbeanExporter"/>
		<property name="annotationToServiceNames">
			<map>
				<entry key="com.kendelong.util.concurrency.ConcurrencyLimitingAspect" value="concurrencyThrottle" />
			</map>
		</property>
		<property name="jmxDomain" value="app.mystuff"/>
	</bean>
	}
	</pre>
 *
 *
 * @author kdelong
 */
@Aspect
@ManagedResource(description="An interceptor that limits the number of threads that can be in a component at any one time")
@Order(300)
public class ConcurrencyLimitingAspect implements Ordered
{
	private final AtomicInteger threadLimit = new AtomicInteger(20);
	private final AtomicInteger threadCount = new AtomicInteger();
	private final AtomicInteger tripCount = new AtomicInteger();
	
	private GraphiteClient graphiteClient;
	
	private int order = 0;

//	@Around("@annotation(com.kendelong.util.concurrency.ConcurrencyThrottle)")
//	public Object applyConcurrencyThrottle(ProceedingJoinPoint pjp) throws Throwable
	@Around("@annotation(ann)")
	public Object applyConcurrencyThrottle(ProceedingJoinPoint pjp, ConcurrencyThrottle ann) throws Throwable
	{
		this.threadLimit.set(ann.threadLimit());
		String key = null;
		if(graphiteClient != null)
		{
			String classKey = StringUtils.substringAfterLast(pjp.getSignature().getDeclaringTypeName(), ".");
			String methodName = pjp.getSignature().getName();
			key = "concurrencyThrottle." + classKey + "." + methodName;
			graphiteClient.increment(key + ".accesses");
		}
		
		Object result;
		try
		{
			int threadNum = threadCount.incrementAndGet();
			if(threadNum > getThreadLimit())
			{
				tripCount.incrementAndGet();
				if(graphiteClient != null) graphiteClient.increment(key + ".trips");
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

	public GraphiteClient getGraphiteClient()
	{
		return graphiteClient;
	}

	public void setGraphiteClient(GraphiteClient graphiteClient)
	{
		this.graphiteClient = graphiteClient;
	}
	
	@Override
	public int getOrder()
	{
		return order;
	}
	
	public void setOrder(int theOrder)
	{
		order = theOrder;
	}

}
