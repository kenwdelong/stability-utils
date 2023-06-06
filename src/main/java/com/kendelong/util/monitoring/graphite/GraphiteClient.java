package com.kendelong.util.monitoring.graphite;

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.timgroup.statsd.NoOpStatsDClient;
import com.timgroup.statsd.NonBlockingStatsDClient;
import com.timgroup.statsd.StatsDClient;
import com.timgroup.statsd.StatsDClientException;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;

public class GraphiteClient
{
	private StatsDClient statsdClient;
	
	private String serverEnv;
	private String appName;
	
	private String statsdHost;
	private int statsdPort;
	
	private boolean stripDomain = false;

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	@PostConstruct
	public void init()
	{
		String prefix = serverEnv + "." + appName + "." + getHostname();
		try
		{
			statsdClient = new NonBlockingStatsDClient(prefix, statsdHost, statsdPort);
			logger.info("Using [{}] as the prefix for graphite buckets", prefix);
		}
		catch(StatsDClientException e)
		{
			logger.error("Unable to open connection to statsd at [" + statsdHost + ":" + statsdPort + "]; using NoOp client.");
			statsdClient = new NoOpStatsDClient();
		}
	}
	
	@PreDestroy
	public void stop()
	{
		if(statsdClient != null)
		{
			statsdClient.stop();
		}
	}

	private String getHostname()
	{
		String hostname = null;
		try 
		{
		    InetAddress addr = InetAddress.getLocalHost();
		    hostname = addr.getHostName();
		    if(!stripDomain)
		    {
		    	hostname = hostname.replaceAll("\\.", "-");
		    }
		    else
		    {
		    	hostname = hostname.split("\\.")[0];
		    }
		} 
		catch (UnknownHostException e) 
		{
			hostname = "unknown";
		}
		return hostname;
	}

	public void count(String aspect, long delta)
	{
		statsdClient.count(aspect, delta);
	}

	public void count(String aspect, long delta, double sampleRate)
	{
		statsdClient.count(aspect, delta, sampleRate);
	}

	public void incrementCounter(String aspect)
	{
		statsdClient.incrementCounter(aspect);
	}

	public void increment(String aspect)
	{
		statsdClient.increment(aspect);
	}

	public void decrementCounter(String aspect)
	{
		statsdClient.decrementCounter(aspect);
	}

	public void decrement(String aspect)
	{
		statsdClient.decrement(aspect);
	}

	public void recordGaugeValue(String aspect, long value)
	{
		statsdClient.recordGaugeValue(aspect, value);
	}

	public void recordGaugeDelta(String aspect, long delta)
	{
		statsdClient.recordGaugeDelta(aspect, delta);
	}

	public void gauge(String aspect, long value)
	{
		statsdClient.gauge(aspect, value);
	}

	public void recordSetEvent(String aspect, String eventName)
	{
		statsdClient.recordSetEvent(aspect, eventName);
	}

	public void recordExecutionTime(String aspect, long timeInMs)
	{
		statsdClient.recordExecutionTime(aspect, timeInMs);
	}

	public void recordExecutionTime(String aspect, long timeInMs, double sampleRate)
	{
		statsdClient.recordExecutionTime(aspect, timeInMs, sampleRate);
	}

	public void recordExecutionTimeToNow(String aspect, long systemTimeMillisAtStart)
	{
		statsdClient.recordExecutionTimeToNow(aspect, systemTimeMillisAtStart);
	}

	public void time(String aspect, long value)
	{
		statsdClient.time(aspect, value);
	}

	public String getServerEnv()
	{
		return serverEnv;
	}

	public void setServerEnv(String env)
	{
		this.serverEnv = env;
	}

	public String getAppName()
	{
		return appName;
	}

	public void setAppName(String app)
	{
		this.appName = app;
	}

	public String getStatsdHost()
	{
		return statsdHost;
	}

	public void setStatsdHost(String statsdHost)
	{
		this.statsdHost = statsdHost;
	}

	public int getStatsdPort()
	{
		return statsdPort;
	}

	public void setStatsdPort(int statsdPort)
	{
		this.statsdPort = statsdPort;
	}

	public boolean isStripDomain()
	{
		return stripDomain;
	}

	public void setStripDomain(boolean stripDomain)
	{
		this.stripDomain = stripDomain;
	}
}
