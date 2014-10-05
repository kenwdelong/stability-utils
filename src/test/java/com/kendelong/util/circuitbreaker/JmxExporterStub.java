package com.kendelong.util.circuitbreaker;

import javax.management.ObjectName;

import com.kendelong.util.spring.JmxExportingAspectPostProcessor;

public class JmxExporterStub extends JmxExportingAspectPostProcessor
{
	private Object pojo;
	
	public Object getPojo()
	{
		return pojo;
	}

	@Override
	protected void registerMBean(ObjectName oname, Object target)
	{
		pojo = target;
	}
}
