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

import com.kendelong.util.monitoring.graphite.GraphiteClient;
import com.kendelong.util.monitoring.webservice.ExternalNameElementComputer;

/**
 * This interceptor will log all calls to the proxied bean, and record max, min, and average response times, call rates,
 * and number of exceptions at both the class and method level.  By default it proxies every bean whose name ends with
 * "Controller" or "Service", as well as any bean annotated with the MonitorPerformance annotation.
 * 
 * Like all aspects, you must create one in each ApplicationContext that you wish to use it in.  Be sure to give the prototype instance
 * an id; if not, the auto-assigned id's in the different ApplicationContexts will conflict.
 * 
 * Export the bean to JMX (no point in monitoring response times if you can't see the results!) using the 
 * com.kendelong.util.spring.JmxExportingAspectPostProcessor
 * 
 * <pre>
 * {@code
	<bean id="servicePerformanceMonitor" class="com.kendelong.util.performance.PerformanceMonitoringAspect" scope="prototype">
		<property name="graphiteClient" ref="graphiteClient"/>
	</bean>
		
	<bean id="aspectJmxExporter" class="com.kendelong.util.spring.JmxExportingAspectPostProcessor" lazy-init="false">
		<property name="mbeanExporter" ref="mbeanExporter"/>
		<property name="annotationToServiceNames">
			<map>
				<entry key="com.kendelong.util.performance.PerformanceMonitoringAspect" value="performance" />
			</map>
		</property>
		<property name="jmxDomain" value="app.mystuff"/>
	</bean>

 * }
 * </pre>
 * 
 * @author Ken DeLong
 * @see com.kendelong.util.performance.MonitorPerformance
 * @see com.kendelong.util.spring.JmxExportingAspectPostProcessor
 *
 */
@Aspect
@ManagedResource(description="Monitor basic performance metrics")
public class PerformanceMonitoringAspect
{
	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	private final Map<String, PerformanceMonitor> monitors = new ConcurrentHashMap<>();
	
	private final ExternalNameElementComputer nameElementComputer = new ExternalNameElementComputer();
	
	private GraphiteClient graphiteClient;
	
	@Around("bean(*Service) or bean(*Controller) or @within(com.kendelong.util.performance.MonitorPerformance)")
	public Object monitorInvocation(ProceedingJoinPoint pjp) throws Throwable
	{
		String classKey = StringUtils.substringAfterLast(pjp.getSignature().getDeclaringTypeName(), ".");
		String methodName = pjp.getSignature().getName();
		String methodKey = classKey + "." + methodName;
		PerformanceMonitor classMonitor = getMonitor(classKey);
		PerformanceMonitor methodMonitor = getMonitor(methodKey);
		Object value;
		long startTime = System.currentTimeMillis();
		String graphitePrefix = null;
		try
		{
			if(graphiteClient != null)
			{
				String nameElement = nameElementComputer.computeExternalNameElement(pjp.getTarget().getClass());
				graphitePrefix = "performance.";
				if(nameElement != null) graphitePrefix = "webservice." + nameElement + ".";
				graphiteClient.increment(graphitePrefix + classKey + ".accesses"); 
				graphiteClient.increment(graphitePrefix + methodKey + ".accesses"); 
			}

			value = pjp.proceed();
			
			long stopTime = System.currentTimeMillis();
			long duration = stopTime - startTime;
			classMonitor.addTiming(duration);
			methodMonitor.addTiming(duration);
			logger.debug("Performance monitor [" + methodKey + "] finished in [" + duration + "] ms");
			if(graphiteClient != null)
			{
				graphiteClient.time(graphitePrefix + classKey, duration);
				graphiteClient.time(graphitePrefix + methodKey, duration);
			}
			
			return value;
		}
		catch(Throwable t)
		{
			classMonitor.addException();
			methodMonitor.addException();
			if(graphiteClient != null)
			{
				graphiteClient.increment(graphitePrefix + classKey + ".error");
				graphiteClient.increment(graphitePrefix + methodKey + ".error");
			}			
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

	public GraphiteClient getGraphiteClient()
	{
		return graphiteClient;
	}

	public void setGraphiteClient(GraphiteClient graphiteClient)
	{
		this.graphiteClient = graphiteClient;
	}
}
