package com.kendelong.util.spring;

import java.util.Map;

import javax.management.ObjectName;

import org.aopalliance.aop.Advice;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.Advisor;
import org.springframework.aop.aspectj.AbstractAspectJAdvice;
import org.springframework.aop.framework.Advised;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.jmx.export.MBeanExportException;
import org.springframework.jmx.export.MBeanExporter;

import com.kendelong.util.monitoring.webservice.ExternalNameElementComputer;

/**
 * This bean can export AspectJ-style aspects to JMX when registered.  For example:
 * 
 * <pre>
 * {@code
	<bean class="com.kendelong.util.circuitbreaker.CircuitBreakerAspect" scope="prototype"/>
			
	<bean class="com.kendelong.util.spring.JmxExportingAspectPostProcessor" lazy-init="false">
		<property name="mbeanExporter" ref="mbeanExporter"/>
		<property name="annotationToServiceNames">
			<map>
				<entry key="com.kendelong.util.circuitbreaker.CircuitBreakerAspect" value="circuitbreaker"/>
			</map>
		</property>
		<property name="jmxDomain" value="app.mystuff"/>
	</bean>  
   }
	</pre>
 * 
 * Important properties to set are the jmxDomain to use for the ONames of the MBeans,
 * and the value of the map which will be also used for the OName.  OName structure is 
 *      jmxDomain:service=[map value],bean=[original advised bean name]
 * 
 * @author Ken DeLong
 *
 */
public class JmxExportingAspectPostProcessor implements BeanPostProcessor
{
	private MBeanExporter mbeanExporter;
	
	private Map<Class<?>, String> annotationToServiceNames;
	
	private String jmxDomain;
	
	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	
	private final ExternalNameElementComputer nameElementComputer = new ExternalNameElementComputer();
	
	public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException
	{
		boolean advised = bean instanceof Advised;
		if(advised)
		{
			Advised advisedBean = (Advised) bean;
			Advisor[] advisors = advisedBean.getAdvisors();
			for(Advisor advisor : advisors)
			{
				Advice advice = advisor.getAdvice();
				if(advice instanceof AbstractAspectJAdvice)
				{
					AbstractAspectJAdvice aaja = (AbstractAspectJAdvice) advice;
					Class<?> aspectPojoClass = aaja.getAspectJAdviceMethod().getDeclaringClass();
					String serviceType = annotationToServiceNames.get(aspectPojoClass);
					if(serviceType != null)
					{
						ObjectName oname;
						String onameString = getDomain(bean) + "." + serviceType + ":bean=" + beanName;
						try
						{
							/* as long as the aspect is marked prototype, each advised bean
							 * gets a new instance, as seen by the return value of this next 
							 * method call.  I've also checked that successive calls to the
							 * getAspectInstance() method return the same object.
							 */
							Object target = aaja.getAspectInstanceFactory().getAspectInstance();
							oname = new ObjectName(onameString);
							registerMBean(oname, target, beanName);
						}
						catch(Exception e)
						{
							logger.error("Cannot register mbean with oname [" + onameString +"]", e);
						}
					}
				}
			}
		}
		return bean;
	}
	
	private String getDomain(Object bean)
	{
		String webserviceNameElement = nameElementComputer.computeExternalNameElement(bean.getClass());
		if(webserviceNameElement == null)
			return jmxDomain;
		else
			return jmxDomain + ".webservice." + webserviceNameElement;
	}


	protected void registerMBean(ObjectName oname, Object target, String beanName)
	{
		try
		{
			// For some reason, we some objects (notably controllers) get registered 2-3 times. Subsequent registrations throw exceptions.
			// unregisterManagedResource() is very tolerant and won't blow up if the object isn't registered.
			// With this "last one wins" strategy, your dev environment stays clean when JRebel reloads the web context.
			mbeanExporter.unregisterManagedResource(oname);
			mbeanExporter.registerManagedResource(target, oname);
			logger.info("Exported " + beanName + " to JMX as " + oname.toString());
		}
		catch(MBeanExportException e)
		{
			logger.warn("Error registering MBean [" + oname + "]; cause: [" + e.getCause() + "]");
		}
	}


	public Object postProcessBeforeInitialization(Object bean, String beanName)
			throws BeansException
	{
		return bean;
	}


	public MBeanExporter getMbeanExporter()
	{
		return mbeanExporter;
	}


	public void setMbeanExporter(MBeanExporter mbeanExporter)
	{
		this.mbeanExporter = mbeanExporter;
	}


	public Map<Class<?>, String> getAnnotationToServiceNames()
	{
		return annotationToServiceNames;
	}


	public void setAnnotationToServiceNames(Map<Class<?>, String> annotationToServiceNames)
	{
		this.annotationToServiceNames = annotationToServiceNames;
	}


	public String getJmxDomain()
	{
		return jmxDomain;
	}


	public void setJmxDomain(String jmxDomain)
	{
		this.jmxDomain = jmxDomain;
	}

}
