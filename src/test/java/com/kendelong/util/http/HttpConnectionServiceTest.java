package com.kendelong.util.http;

import java.util.HashMap;
import java.util.Map;

import org.junit.Ignore;
import org.junit.Test;

@Ignore
public class HttpConnectionServiceTest
{

	@Test
	public void testConnectionsAreAvailable() throws Exception
	{
		PooledHttpClientStrategy strat = new PooledHttpClientStrategy();
		strat.setConnectionTimeoutInMs(100);
		strat.setSocketTimeoutInMs(6000);
		strat.setRetrieveConnectionTimeoutInMs(1000);
		strat.setMaxConnectionsPerHost(2);
		strat.setMaxTotalConnections(6);
		strat.afterPropertiesSet();
		
		HttpConnectionService svc = new HttpConnectionService();
		svc.setHttpClientStrategy(strat);
		
		
		Map<String, String> params = new HashMap<>();
		params.put("q", "dog");
		for(int i = 0; i < 10; i++)
		{
			HttpResponseObject resp = svc.getResult("http://tomcat.apache.org", params);
			System.out.println("Response was " + resp.getStatusCode());
		}
	}
	
	@Test
	public void givenLenientSSL_itConnectsToAnIp() throws Exception
	{
		PooledHttpClientStrategy strat = new PooledHttpClientStrategy();
		strat.setConnectionTimeoutInMs(100);
		strat.setSocketTimeoutInMs(6000);
		strat.setRetrieveConnectionTimeoutInMs(1000);
		strat.setMaxConnectionsPerHost(2);
		strat.setMaxTotalConnections(6);
		strat.setAllowAllSsl(true);
		strat.afterPropertiesSet();
		
		HttpConnectionService svc = new HttpConnectionService();
		svc.setHttpClientStrategy(strat);
		
		HttpRequest request = new HttpRequest()
				// You might need to change this address - find an https site and use its IP (or AWS ELB A record)
				.withUrl("https://216.58.195.68:443")
				.withMethod(HttpMethod.GET)
				;

		HttpResponseObject response = svc.sendGenericRequest(request);
		System.out.println(response.getBody());
	}
	
	@Test
	// https://stackoverflow.com/questions/5725430/http-test-server-that-accepts-get-post-calls
	public void testGenericRequest() throws Exception
	{
		HttpRequest request = new HttpRequest()
				.withUrl("http://httpbin.org/put")
				.addHeader("X-Ken", "X Man")
				.addQueryParameter("foo", "bar")
				.postAsText("Hey")
				.withChunkedEncoding(false)
				.withMethod(HttpMethod.PUT)
				;
		
		HttpConnectionService service = new HttpConnectionService();
		service.setHttpClientStrategy(new SimpleHttpClientStrategy());
		service.init();
		
		HttpResponseObject response = service.sendGenericRequest(request);
		System.out.println(response.getBody());
	}

}
