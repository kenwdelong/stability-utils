package com.kendelong.util.monitoring.graphite;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;

/**
 * A logback appender that sends the log levels to the Graphite server. With this appender, you will get charts of how many log
 * messages are being written by category: one chart for ERROR, one for WARN, etc.  
 * 
 * Because this appender is instantiated by the
 * logback framework, it can't be autowired.  In order to transfer the reference of the graphiteClient from the Spring
 * context to this appender, a second instance of the appender is created in applicationContext.xml just for the purpose
 * of harvesting the reference and transferring it to the static variable here.  It's a sucky hack, but I couldn't think
 * of any other way to get the reference to the GraphiteClient.
 * 
 * Configuration in application context is like this:
 * <pre>
   {@code
	<bean class="com.kendelong.util.monitoring.graphite.LogbackGraphiteAppender">
		<property name="graphiteClient" ref="graphiteClient"/>
	</bean>
   }
   </pre>
 * 
 * @author kdelong
 *
 */
public class LogbackGraphiteAppender extends AppenderBase<ILoggingEvent>
{
	private volatile static GraphiteClient graphiteClient;

	@Override
	protected void append(ILoggingEvent eventObject)
	{
		Level level = eventObject.getLevel();
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
