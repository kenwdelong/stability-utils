package com.kendelong.util.http;

import java.util.HashMap;
import java.util.Map;

import org.junit.Ignore;
import org.junit.Test;

public class HttpConnectionServiceTest
{

	@Test
	@Ignore
	public void testConnectionsAreAvailable() throws Exception
	{
//		<bean class="com.kendelong.util.http.HttpConnectionService">
//		<property name="httpClientStrategy" ref="pooledHttpStrategy"/>
//	</bean>
//	
//	<bean id="pooledHttpStrategy" class="com.kendelong.util.http.PooledHttpClientStrategy">
//		<property name="connectionTimeoutInMs" value="100"/>
//		<property name="socketTimeoutInMs" value="6000"/>
//		<property name="retrieveConnectionTimeoutInMs" value="1000"/>
//		<property name="maxConnectionsPerHost" value="2"/>
//		<property name="maxTotalConnections" value="6"/>
//	</bean>

		PooledHttpClientStrategy strat = new PooledHttpClientStrategy();
		strat.setConnectionTimeoutInMs(100);
		strat.setSocketTimeoutInMs(6000);
		strat.setRetrieveConnectionTimeoutInMs(1000);
		strat.setMaxConnectionsPerHost(2);
		strat.setMaxTotalConnections(6);
		
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
	@Ignore
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
