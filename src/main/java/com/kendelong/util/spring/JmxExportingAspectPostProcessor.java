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

public class JmxExportingAspectPostProcessor implements BeanPostProcessor
{
	private MBeanExporter mbeanExporter;
	
	private Map<Class<?>, String> annotationToServiceNames;
	
	private String jmxDomain;
	
	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	
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
						String onameString = jmxDomain + ":service=" + serviceType + ",bean=" + beanName;
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


	protected void registerMBean(ObjectName oname, Object target, String beanName)
	{
		try
		{
			mbeanExporter.registerManagedResource(target, oname);
			logger.info("Exported " + beanName + " to JMX as " + oname.toString());
		}
		catch(MBeanExportException e)
		{
			logger.warn("Error registering MBean [" + oname + "]");
			// TODO probably we should unregister and retry on  javax.management.InstanceAlreadyExistsException
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
