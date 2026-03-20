package com.kendelong.util.concurrency;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.core.annotation.Order;
import org.springframework.jmx.export.annotation.ManagedAttribute;
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
@Order(150)
public class ConcurrencyLimitingAspect
{
	private final Map<String, Integer> threadLimits  = new ConcurrentHashMap<>();  // For displays
	private final Map<String, AtomicInteger> tripCounts  = new ConcurrentHashMap<>();
	private final Map<String, Semaphore> semaphores = new ConcurrentHashMap<>();
	
	private GraphiteClient graphiteClient;
	
	@Around("@annotation(ann)")
	public Object applyConcurrencyThrottle(ProceedingJoinPoint pjp, ConcurrencyThrottle ann) throws Throwable
	{
		final String methodName = pjp.getSignature().getName();
		final int threadLimit = ann.threadLimit();
		threadLimits.computeIfAbsent(methodName, k -> threadLimit);
	    final Semaphore semaphore = semaphores.computeIfAbsent(methodName, k -> new Semaphore(threadLimit));
		final AtomicInteger tripCount = tripCounts.computeIfAbsent(methodName, k -> new AtomicInteger());
				
		String key = null;
		if(graphiteClient != null)
		{
			String classKey = StringUtils.substringAfterLast(pjp.getSignature().getDeclaringTypeName(), ".");
			key = "concurrencyThrottle." + classKey + "." + methodName;
			graphiteClient.increment(key + ".accesses");
		}
		
	    if(!semaphore.tryAcquire())
	    {
	        tripCount.incrementAndGet();
	        if(graphiteClient != null) graphiteClient.increment(key + ".trips");
	        throw new ConcurrencyLimitExceededException("This thread exceeded the thread limit of " + threadLimit + " for method " + methodName);
	    }

	    try
	    {
	        return pjp.proceed();
	    }
	    finally
	    {
	        semaphore.release();
	    }
	}
	
	@ManagedAttribute(description="The maximum number of threads allowed in the component at one time")
	public String getThreadLimit()
	{
		StringBuilder sb = new StringBuilder();
		threadLimits.forEach((k, v) -> sb.append(k).append(": ").append(v).append("; \n"));
		return sb.toString();
	}

	@ManagedAttribute(description="The current number of threads in the component")
	public String getThreadCount()
	{
	    StringBuilder sb = new StringBuilder();
	    semaphores.forEach((k, semaphore) -> {
	        int total = threadLimits.get(k);
	        int inUse = total - semaphore.availablePermits();
	        sb.append(k).append(": ").append(inUse).append(" / ").append(total).append("; \n");
	    });
	    return sb.toString();	
	}
	
	@ManagedAttribute(description="The number of accesses that were rejected due to the maximum number of threads being reached")
	public String getTripCount()
	{
		return formatMap(tripCounts);
	}
	
	private String formatMap(Map<String, AtomicInteger> map)
	{
		StringBuilder sb = new StringBuilder();
		map.forEach((k, v) -> sb.append(k).append(": ").append(v.get()).append("; \n"));
		return sb.toString();
	}
	
	public GraphiteClient getGraphiteClient()
	{
		return graphiteClient;
	}

	public void setGraphiteClient(GraphiteClient graphiteClient)
	{
		this.graphiteClient = graphiteClient;
	}	
}
