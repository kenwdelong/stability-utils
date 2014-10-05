package com.kendelong.util.performance;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jmx.export.annotation.ManagedAttribute;
import org.springframework.jmx.export.annotation.ManagedResource;

@Aspect
@ManagedResource(objectName="com.kendelong:service=performanceMonitor")
public class PerformanceMonitoringAspect
{
	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	private final Map<String, PerformanceMonitor> monitors = new ConcurrentHashMap<>();
	
	//@Around("within(com.myapp.service.*)")  WORKS
	//@Around("bean(*Service) or bean(*Controller)")  WORKS
	// proxying controllers caused them not to be picked up as controllers anymore. Do they need interfaces?
	@Around("@within(org.springframework.stereotype.Service)")
	public Object monitorInvocation(ProceedingJoinPoint pjp) throws Throwable
	{
		String classKey = StringUtils.substringAfterLast(pjp.getSignature().getDeclaringTypeName(), ".");
		String methodName = pjp.getSignature().getName();
		String methodKey = classKey + "." + methodName;
		PerformanceMonitor classMonitor = getMonitor(classKey);
		PerformanceMonitor methodMonitor = getMonitor(methodKey);
		Object value;
		long startTime = System.currentTimeMillis();
		try
		{
			value = pjp.proceed();
			
			long stopTime = System.currentTimeMillis();
			long duration = stopTime - startTime;
			classMonitor.addTiming(duration);
			methodMonitor.addTiming(duration);
			logger.debug("Performance monitor [" + methodKey + "] finished in [" + duration + "] ms");
			
			return value;
		}
		catch(Throwable t)
		{
			classMonitor.addException();
			methodMonitor.addException();
			throw t;
		}
	}

	private PerformanceMonitor getMonitor(String key)
	{
		PerformanceMonitor monitor = null;
		monitor = monitors.get(key);
		if(monitor == null)
		{
			monitor = new PerformanceMonitor();
			monitors.put(key, monitor);
		}
		return monitor;
	}
	
	@ManagedAttribute(description="Show performance report")
	public String getPerformanceReport()
	{
		ReportFormatter formatter = new ReportFormatter();
		return formatter.formatReport(monitors);
	}
}
