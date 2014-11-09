package com.kendelong.util.monitoring.webservice;

public class ExternalNameElementComputer
{
	public String computeExternalNameElement(Class<?> clazz)
	{
		String nameElement = null;
		if(clazz.isAnnotationPresent(WebServiceClient.class))
		{
			nameElement = "client";
		}
		if(clazz.isAnnotationPresent(WebServiceEndpoint.class))
		{
			nameElement = "endpoint";
		}
				
		return nameElement;
	}
}
