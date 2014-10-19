package com.kendelong.util.monitoring.graphite;

import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.Level;
import org.apache.log4j.spi.LoggingEvent;


/**
 * A log4j log appender that sends the log levels to the Graphite server. With this appender, you will get charts of how many log
 * messages are being written by category: one chart for ERROR, one for WARN, etc.  
 * 
 * Because this appender is instantiated by the
 * log4j framework, it can't be autowired.  In order to transfer the reference of the graphiteClient from the Spring
 * context to this appender, a second instance of the appender is created in applicationContext.xml just for the purpose
 * of harvesting the reference and transferring it to the static variable here.  It's a sucky hack, but I couldn't think
 * of any other way to get the reference to the GraphiteClient.
 * 
 * Configuration in application context is like this:
	<bean class="com.bc.intl.util.GraphiteAppender">
		<property name="graphiteClient" ref="graphiteClient"/>
	</bean>
 * 
 * @author kdelong
 *
 */
public class GraphiteAppender extends AppenderSkeleton
{
	private volatile static GraphiteClient graphiteClient;

	@Override
	public void close()
	{
	}

	@Override
	public boolean requiresLayout()
	{
		return false;
	}

	@Override
	protected void append(LoggingEvent event)
	{
		Level level = event.getLevel();
		if(graphiteClient != null)
		{
			graphiteClient.increment("logs." + level.toString());
		}
	}
	
	public void setGraphiteClient(GraphiteClient gc)
	{
		graphiteClient = gc;
	}

}
