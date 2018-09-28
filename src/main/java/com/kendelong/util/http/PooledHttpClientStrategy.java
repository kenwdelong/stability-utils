package com.kendelong.util.http;

import java.io.IOException;
import java.nio.charset.Charset;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import javax.net.ssl.SSLContext;

import org.apache.http.client.config.RequestConfig;
import org.apache.http.config.ConnectionConfig;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.config.SocketConfig;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.pool.PoolStats;
import org.apache.http.ssl.SSLContexts;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.scheduling.annotation.Scheduled;



/**
 * This Strategy allows for the configuration of pooled http connections. All the properties
 * in this class will be configurable from a jmx interface
 *
 */
public class PooledHttpClientStrategy implements IHttpClientStrategy,InitializingBean, DisposableBean
{
	private final AtomicReference<PoolingHttpClientConnectionManager> connectionManager = new AtomicReference<PoolingHttpClientConnectionManager>();
	private final AtomicReference<CloseableHttpClient> httpClient = new AtomicReference<CloseableHttpClient>();
	
	private volatile int maxConnectionsPerHost = 6;
	private volatile int maxTotalConnections = 30;
	// Connection timeout is how long the client will wait for a connection to be created
	private volatile int connectionTimeoutInMs = 5000;
	// Socket timeout is how long the socket can be idle before the client aborts
	private volatile int socketTimeoutInMs = 5000;
	private volatile boolean ignoreCookies = false;
	
	// retrieveConnectionTimeout is how long in ms the client will wait for the connection manager to deliver a connection
	private volatile int retrieveConnectionTimeoutInMs = 500;
	
	// enables a scheduled cleaning sweep of our connections
    private volatile boolean connectionCleaningEnabled = true;
    // timeout value used for scheduled cleansing of unused connections (measured in seconds)
    private volatile int idleConnectionTimeoutInSeconds = 30;
    
    // How long a connection must be invalid before a stale connection check is issued by the pool manager before leasing. Negative value disables check.
    private volatile int validateAfterInactivityMs = 1000;
    
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    
    private boolean allowAllSsl = false;

	@Override
	public CloseableHttpClient getHttpClient()
	{
		return httpClient.get();
	}

	public void reset()
	{
		// ************* Create connection manager **********************
		
		PoolingHttpClientConnectionManager cm;
		if(allowAllSsl)
		{
			try
			{
				Registry<ConnectionSocketFactory> r = RegistryBuilder.<ConnectionSocketFactory>create()
				        .register("https", getLenientSslSocketFactory())
				        .build();
				cm = new PoolingHttpClientConnectionManager(r);
			}
			catch(KeyManagementException | NoSuchAlgorithmException | KeyStoreException e)
			{
				logger.error("Could not create lenient socket factory", e);
				cm = new PoolingHttpClientConnectionManager();
			}
		}
		else
		{
			cm = new PoolingHttpClientConnectionManager();
		}
		
		cm.setMaxTotal(maxTotalConnections);
		cm.setDefaultMaxPerRoute(maxConnectionsPerHost);
		cm.setValidateAfterInactivity(validateAfterInactivityMs);
		ConnectionConfig connectionConfig = ConnectionConfig.custom()
				.setCharset(Charset.forName("UTF-8"))
	            .build();
        cm.setDefaultConnectionConfig(connectionConfig);
        SocketConfig socketConfig = SocketConfig.custom()
                .setTcpNoDelay(true)
                .setSoTimeout(socketTimeoutInMs)
                .build();
        cm.setDefaultSocketConfig(socketConfig);
        setConnectionManager(cm);

        
        // **************** Create Client *****************
        
        // these are reusable: https://hc.apache.org/httpcomponents-client-4.3.x/tutorial/html/connmgmt.html#d5e380   Section 2.4
		// this ref is better than the official docs
		// http://www.baeldung.com/httpclient-timeout
		
		RequestConfig config = RequestConfig.custom()
				.setConnectTimeout(connectionTimeoutInMs)
				.setConnectionRequestTimeout(retrieveConnectionTimeoutInMs)
				.setSocketTimeout(socketTimeoutInMs)
				.build();
		CloseableHttpClient client = HttpClientBuilder.create()
				.setDefaultRequestConfig(config)
				.setConnectionManager(getConnectionManager())
				.build();
		
		httpClient.set(client);
		
//		HttpProtocolParams.setUserAgent(params, "Apache httpclient-4.1.3 ops@me.com");

        // ignore cookies
        // gzip

	}

	/**
	 * This method will be invoked after spring instantiates the object. 
	 * If the properties are configured in spring xml this method will enable honoring those properties
	 * 
	 */
	@Override
	public void afterPropertiesSet() throws Exception
	{
		reset();
	}

	/**
	 * Clean the connections.  For this method to be invoked, the Spring context needs a couple of beans:
	 * 
     * <pre>
       {@code
	   	<task:scheduler id="threadPoolTaskScheduler" pool-size="1" />
	    <task:annotation-driven scheduler="threadPoolTaskScheduler" />
        }
       </pre>
       Or
       just put @EnableScheduling on one of the @Config classes
	 *  
	 *  This will activate the timer that actually calls this method.
	 *  
	 *  See http://hc.apache.org/httpcomponents-client-ga/tutorial/html/connmgmt.html for an explanation (section 2.10)
	 */
	@Scheduled(fixedDelay=30000)
	public void cleanConnections()
	{
		if (isConnectionCleaningEnabled())
		{
			logger.trace("cleaning http connections");
			this.connectionManager.get().closeExpiredConnections();
			this.connectionManager.get().closeIdleConnections(getIdleConnectionTimeoutInSeconds(), TimeUnit.SECONDS);
		}
	}
	
	private SSLConnectionSocketFactory getLenientSslSocketFactory() throws KeyManagementException, NoSuchAlgorithmException, KeyStoreException
	{
	    SSLContext sslContext = SSLContexts.createSystemDefault();
	    SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(
	            sslContext,
	            NoopHostnameVerifier.INSTANCE);
	    return sslsf;
	}

	private PoolingHttpClientConnectionManager getConnectionManager()
	{
		return connectionManager.get();
	}
	
	private void setConnectionManager(PoolingHttpClientConnectionManager newConMgr)
	{
		PoolingHttpClientConnectionManager oldConMgr = connectionManager.getAndSet(newConMgr);
		if(oldConMgr != null)
		{
			oldConMgr.shutdown();
		}
	}

	public int getMaxConnectionsPerHost()
	{
		return maxConnectionsPerHost;
	}

	public void setMaxConnectionsPerHost(int defaultMaxConnectionsPerHost)
	{
		this.maxConnectionsPerHost = defaultMaxConnectionsPerHost;
	}

	public int getMaxTotalConnections()
	{
		return maxTotalConnections;
	}

	public void setMaxTotalConnections(int maxTotalConnections)
	{
		this.maxTotalConnections = maxTotalConnections;
	}

	public int getConnectionTimeoutInMs()
	{
		return connectionTimeoutInMs;
	}

	public void setConnectionTimeoutInMs(int connectionTimeout)
	{
		this.connectionTimeoutInMs = connectionTimeout;
	}

	public int getSocketTimeoutInMs()
	{
		return socketTimeoutInMs;
	}

	public void setSocketTimeoutInMs(int socketTimeout)
	{
		this.socketTimeoutInMs = socketTimeout;
	}

	public boolean isIgnoreCookies()
	{
		return ignoreCookies;
	}

	public void setIgnoreCookies(boolean ignoreCookies)
	{
		this.ignoreCookies = ignoreCookies;
	}

	public int getRetrieveConnectionTimeoutInMs()
	{
		return retrieveConnectionTimeoutInMs;
	}

	public void setRetrieveConnectionTimeoutInMs(int retrieveConnectionTimeout)
	{
		this.retrieveConnectionTimeoutInMs = retrieveConnectionTimeout;
	}

	public boolean isConnectionCleaningEnabled()
	{
		return connectionCleaningEnabled;
	}

	public void setConnectionCleaningEnabled(boolean connectionCleaningEnabled)
	{
		this.connectionCleaningEnabled = connectionCleaningEnabled;
	}

	public int getIdleConnectionTimeoutInSeconds()
	{
		return idleConnectionTimeoutInSeconds;
	}

	public void setIdleConnectionTimeoutInSeconds(int idleConnectionTimeout)
	{
		this.idleConnectionTimeoutInSeconds = idleConnectionTimeout;
	}

	public boolean isAllowAllSsl()
	{
		return allowAllSsl;
	}

	public void setAllowAllSsl(boolean allowAllSsl)
	{
		this.allowAllSsl = allowAllSsl;
	}
	
	public PoolStats getPoolStats()
	{
		return getConnectionManager().getTotalStats();
	}

	public int getValidateAfterInactivityMs()
	{
		return validateAfterInactivityMs;
	}

	public void setValidateAfterInactivityMs(int validateAfterInactivityMs)
	{
		this.validateAfterInactivityMs = validateAfterInactivityMs;
	}
	
	@Override
	public void destroy()
	{
		try
		{
			getHttpClient().close();
		}
		catch(IOException e)
		{
			// seriously?
			getConnectionManager().close();
		}
	}

}
