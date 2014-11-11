package com.kendelong.util.monitoring.webservice;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class ExternalNameElementComputer
{
	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	public String computeExternalNameElement(Class<?> clazz)
	{
		Class<?> correctClazz = findOriginalClass(clazz);
		String nameElement = null;
		if(correctClazz.isAnnotationPresent(WebServiceClient.class))
		{
			nameElement = "client";
		}
		if(correctClazz.isAnnotationPresent(WebServiceEndpoint.class))
		{
			nameElement = "endpoint";
		}
				
		return nameElement;
	}
	
	private Class<?> findOriginalClass(Class<?> clazz)
	{
		String className = clazz.getName();
		String newClassName = cleanClassName(className);
		if(newClassName.equals(className))
		{
			return clazz;
		}
		else
		{
			try
			{
				return clazz.getClassLoader().loadClass(newClassName);
			}
			catch(ClassNotFoundException e)
			{
				logger.warn("Class not found: [" + newClassName + "]");
				return clazz;
			}
		}
	}

	String cleanClassName(String cn)
	{
		int index = cn.indexOf("$$EnhancerBySpring");
		if(index < 0)
		{
			return cn;
		}
		else
		{
			return cn.substring(0, index);
		}
	}
}
