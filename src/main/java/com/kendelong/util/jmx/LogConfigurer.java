package com.kendelong.util.jmx;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedOperationParameter;
import org.springframework.jmx.export.annotation.ManagedOperationParameters;
import org.springframework.jmx.export.annotation.ManagedResource;
import org.springframework.stereotype.Component;

/**
 * A JMX bean that allows you to set logger levels at runtime through the JMX console.
 * 
 * @author Ken DeLong
 *
 */
@ManagedResource(objectName="logging:service=logger", description="Log4j Logger Configurer")
@Component
public class LogConfigurer
{
	private final org.slf4j.Logger mylogger = org.slf4j.LoggerFactory.getLogger(this.getClass());
	
	@ManagedOperation(description="Retrieve the logger level for a specific class")
	@ManagedOperationParameters
	({
		@ManagedOperationParameter(name="clazz", description="The class name for the logger")
	})
	public String getLoggerLevel(String clazz)
	{
		Logger logger = Logger.getLogger(clazz);
		if (logger == null)
		{
			return "logger not present";
		}
		Level level = logger.getLevel();
		return level == null? "not defined" : level.toString();
	}

	@ManagedOperation(description="Set the logger level for a specific class")
	@ManagedOperationParameters
	({
		@ManagedOperationParameter(name="clazz", description="The class name for the logger"),
		@ManagedOperationParameter(name="level", description="The level for the logger (DEBUG, INFO, ERROR, FATAL, WARN)")
	})
	public void setLoggerLevel(String clazz, String level)
	{
		Level elevel = Level.toLevel(level);
		if (!elevel.toString().equals(level)) 
		{
			throw new IllegalArgumentException(level + " is not a valid logger level");
		}
		Logger logger = Logger.getLogger(clazz);
		if (logger != null)
		{
			logger.setLevel(elevel);
			mylogger.info("Setting [" + clazz + "] to level [" + level + "]");
		}
	}

}
