package com.kendelong.util.http;

import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
/**
 * This class supports the creation of a single http client. 
 * If you are interested in a pooled connection instead of a single one
 * please use the PooledHttpClientStrategy
 * 
 *
 */
public class SimpleHttpClientStrategy implements IHttpClientStrategy
{
	private boolean allowLenientSsl;

    @Override
	public CloseableHttpClient getHttpClient()
	{
    	return HttpClients.createDefault();
	}

	public boolean isAllowLenientSsl()
	{
		return allowLenientSsl;
	}

	public void setAllowLenientSsl(boolean allowLenientSsl)
	{
		this.allowLenientSsl = allowLenientSsl;
	}

}
