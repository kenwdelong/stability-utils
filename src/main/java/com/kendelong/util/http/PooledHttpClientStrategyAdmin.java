package com.kendelong.util.http;

import org.springframework.jmx.export.annotation.ManagedAttribute;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedResource;


/**
 * Wrapper to the PooledHttpClientStrategy for jmx
 * @author survig
 *
 */
@ManagedResource
public class PooledHttpClientStrategyAdmin
{
    private PooledHttpClientStrategy pooledStrategy;
	
    @ManagedAttribute(description="The number of milliseconds that the client will wait to establish a connection")
	public int getConnectionTimeout()
	{
		return getPooledStrategy().getConnectionTimeout();
	}

    @ManagedAttribute(description="Max connections per host")
	public int getDefaultMaxConnectionsPerHost()
	{
		return getPooledStrategy().getMaxConnectionsPerHost();
	}

    @ManagedAttribute(description="Total number of connections this client will open")
	public int getMaxTotalConnections()
	{
		return getPooledStrategy().getMaxTotalConnections();
	}

    @ManagedOperation(description="Resets the connection manager to the configured parameters; if you update one of " +
    		"the parameters on this page, you need to also press reset for it to take effect")
	public void reset() throws Exception
	{
		getPooledStrategy().reset();
	}

    @ManagedAttribute
	public void setConnectionTimeout(int connectionTimeout)
	{
		getPooledStrategy().setConnectionTimeout(connectionTimeout);

	}

    @ManagedAttribute
	public void setDefaultMaxConnectionsPerHost(int defaultMaxConnectionsPerHost)
	{
		getPooledStrategy().setMaxConnectionsPerHost(defaultMaxConnectionsPerHost);

	}

    @ManagedAttribute
	public void setMaxTotalConnections(int maxTotalConnections)
	{
		getPooledStrategy().setMaxTotalConnections(maxTotalConnections);
	}

    @ManagedAttribute(description="The number of milliseconds before the client destroys an idle socket " +
    		"eg., if the server is not sending data")
	public int getSocketTimeout()
	{
		return pooledStrategy.getSocketTimeout();
	}

    @ManagedAttribute
	public void setSocketTimeout(int socketTimeout)
	{
		pooledStrategy.setSocketTimeout(socketTimeout);
	}
    
    @ManagedAttribute(description="The number of milliseconds that a client will wait to obtain a connection from " +
    		"the connection pool before receiving an exception")
    public long getRetrieveConnectionTimeout()
    {
    	return pooledStrategy.getRetrieveConnectionTimeout();
    }
    
    @ManagedAttribute
    public void setRetrieveConnectionTimeout(int val)
    {
    	pooledStrategy.setRetrieveConnectionTimeout(val);
    }

    
    @ManagedAttribute(description="If this is true, the connection manager will send a ping over the connection "
    	 + "before handing it out to the client.  This assures the remote socket has not been closed by the server "
    	 + "or a firewall, but adds 10-30ms to every request.")
	public boolean isStaleConnectionCheck()
	{
		return pooledStrategy.isStaleConnectionCheck();
	}

    @ManagedAttribute
	public void setStaleConnectionCheck(boolean staleConnectionCheck)
	{
		pooledStrategy.setStaleConnectionCheck(staleConnectionCheck);
	}

	public PooledHttpClientStrategy getPooledStrategy()
	{
		return pooledStrategy;
	}

	/**
	 * @param pooledStrategy the pooledStrategy to set
	 */
	public void setPooledStrategy(PooledHttpClientStrategy pooledStrategy)
	{
		this.pooledStrategy = pooledStrategy;
	}

    @ManagedAttribute(description="If this is true, the pooled http strategy will ask its ClientConnectionManager "
    		+ "to try to clean out expired and idle connections")
   	public boolean isConnectionCleaningEnabled()
   	{
   		return pooledStrategy.isConnectionCleaningEnabled();
   	}
    @ManagedAttribute
	public void setConnectionCleaningEnabled(boolean enabled)
	{
		getPooledStrategy().setConnectionCleaningEnabled(enabled);

	}

    @ManagedAttribute(description="timeout value used for scheduled cleansing of unused connections (measured in seconds)")
    public int getIdleConnectionTimeout()
    {
    	return pooledStrategy.getIdleConnectionTimeout();
    }
    
    @ManagedAttribute
    public void setIdleConnectionTimeout(int val)
    {
    	pooledStrategy.setIdleConnectionTimeout(val);
    }
    
}
