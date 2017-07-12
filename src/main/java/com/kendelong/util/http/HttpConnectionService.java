package com.kendelong.util.http;

import static com.kendelong.util.http.HttpEntityEnclosingMethod.POST;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPatch;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;

public class HttpConnectionService implements IHttpConnectionService
{
	private Logger logger = LoggerFactory.getLogger(this.getClass());
	public static final String ENCODING = "UTF-8";
	private IHttpClientStrategy httpClientStrategy;

	@Override
	public HttpResponseObject sendStringAsRequestEntity(String connectionURL, HttpEntityEnclosingMethod method, String data, String contentType, Map<String, String> headers) throws Exception
	{
		return sendStringAsRequestEntity(connectionURL, method, data, contentType, headers, true);
	}
	
	@Override
	public HttpResponseObject sendStringAsRequestEntity(String connectionURL, HttpEntityEnclosingMethod method, String data, String contentType, Map<String, String> headers, boolean chunked) throws Exception
	{
		HttpEntityEnclosingRequestBase request;
		switch(method)
		{
			case POST:
				request = new HttpPost(connectionURL);
				break;
			case PUT:
				request = new HttpPut(connectionURL);
				break;
			case PATCH:
				request = new HttpPatch(connectionURL);
				break;
			default:
				throw new UnsupportedOperationException("Can't process method [" + method + "]");
		}
		
		StringEntity entity = new StringEntity(data, ENCODING);
		entity.setContentType(contentType);
		entity.setChunked(chunked);
		request.setEntity(entity);
		for(String name : headers.keySet())
		{
			request.setHeader(name, headers.get(name));
		}
		return doExecuteAndGetResponse(request);		
	}
		
	@Override
	public HttpResponseObject postStringAsRequestEntity(String connectionURL, String data, String contentType, Map<String, String> headers) throws Exception
	{
		return sendStringAsRequestEntity(connectionURL, POST, data, contentType, headers);
	}
		
	@Override
	public HttpResponseObject postStringAsRequestEntity(String connectionURL, String data, String contentType) throws Exception
	{
		Map<String, String> headers = new HashMap<>();
		return postStringAsRequestEntity(connectionURL, data, contentType, headers);
	}
		
	@Override
	public HttpResponseObject postStringAsXml(String connectionURL, String data) throws Exception
	{
		return postStringAsRequestEntity(connectionURL, data, "text/xml");
	}

	@Override
	public HttpResponseObject postStringAsJson(String connectionURL, String data) throws Exception
	{
		return postStringAsRequestEntity(connectionURL, data, "application/json");
	}

	@Override
	public HttpResponseObject postData(String connectionURL, Map<String, String> parameters)
			throws Exception
	{
		HttpPost postRequest = new HttpPost(connectionURL);
		// Put the parameters in the request
		List<NameValuePair> nvPairs = new ArrayList<NameValuePair>();
		for(Map.Entry<String, String> entry : parameters.entrySet())
		{
			NameValuePair pair = new BasicNameValuePair(entry.getKey(), entry.getValue());
			nvPairs.add(pair);
		}
		UrlEncodedFormEntity entity = new UrlEncodedFormEntity(nvPairs, ENCODING);
		postRequest.setEntity(entity);
		return doExecuteAndGetResponse(postRequest);
	}


	@Override
	public HttpResponseObject getResult(String connectionUrl, Map<String, String> parameters, Map<String, String> headers) throws Exception
	{
		// add parameters to GET
		StringBuilder urlWithParams = new StringBuilder(connectionUrl);
		if(parameters != null  && !parameters.isEmpty())
		{
			urlWithParams.append("?");
			for(String key : parameters.keySet())
			{
				String value = URLEncoder.encode(parameters.get(key), ENCODING);
				urlWithParams.append(key).append("=").append(value).append("&");
			}
			urlWithParams.deleteCharAt(urlWithParams.length() - 1);
			// Wish I had Groovy here...sigh.
		}
		HttpGet getRequest = new HttpGet(urlWithParams.toString());
		for(String name : headers.keySet())
		{
			getRequest.setHeader(name, headers.get(name));
		}

		return doExecuteAndGetResponse(getRequest);
	}
	
	@Override
	public HttpResponseObject getResult(String connectionUrl, Map<String, String> parameters) throws Exception
	{
		Map<String, String> headers = new HashMap<>();
		return getResult(connectionUrl, parameters, headers);
	}
	
	@Override
	public HttpResponseObject simpleGet(String url) throws Exception 
	{
		HttpGet getRequest = new HttpGet(url);
		return doExecuteAndGetResponse(getRequest);
	}
	
	/**
	 * Execute the method and pass it to be processed and populate an
	 * HttpResponseObject with the response data.
	 * 
	 * @param httpRequest	Method to be executed
	 * @return response object populated with body, headers, length, status info
	 * @throws IOException 
	 */
	private HttpResponseObject doExecuteAndGetResponse(HttpUriRequest httpRequest) throws IOException
	{
		HttpClient client = getHttpClientStrategy().getHttpClient();
		HttpResponseObject response = null;

		try
		{
			logger.debug("Executing [" + httpRequest.getRequestLine() + "]");
			// Send the data and get the response
			HttpResponseHandler handler = new HttpResponseHandler();
			response = client.execute(httpRequest, handler);
		}
		catch(ClientProtocolException e)
		{
			logger.warn("Fatal protocol violation: " + e.getMessage());
			throw e;
		}
		catch(IOException e)
		{
			logger.warn("Fatal transport error: " + e.getMessage());
			throw e;
		}
		
		return response;
	}

	private class HttpResponseHandler implements ResponseHandler<HttpResponseObject>
	{
		@Override
		public HttpResponseObject handleResponse(HttpResponse response) throws ClientProtocolException, IOException
		{
			HttpResponseObject responseObject = new HttpResponseObject();

			HttpEntity entity = response.getEntity();
			String body = EntityUtils.toString(entity, ENCODING);
			long length = (entity != null ? entity.getContentLength() : 0);
			Header contentType = (entity != null ? entity.getContentType() : null);
			String encoding = (contentType != null ? contentType.getValue() : ENCODING);

			responseObject.setBody(body);
			responseObject.setHeaders(response.getAllHeaders());
			responseObject.setLength(length);
			responseObject.setStatusCode(response.getStatusLine().getStatusCode());
			responseObject.setStatusText(response.getStatusLine().getReasonPhrase());
			
			if(logger.isDebugEnabled())
			{
				logger.debug("Received reponse from remote server."
								+ "  Use logger httpclient.wire.content=DEBUG to see it."
								+ "  Response was [" + responseObject.getLength()
								+ "] characters long; encoding was [" + encoding +"]");
				logger.trace(responseObject.getBody());
			}
			return responseObject;
		}
	}


	public IHttpClientStrategy getHttpClientStrategy()
	{
		return httpClientStrategy;
	}

	@Required
	public void setHttpClientStrategy(IHttpClientStrategy httpClientStrategy)
	{
		this.httpClientStrategy = httpClientStrategy;
	}
	
	@PostConstruct
	public void init()
	{
		if(this.httpClientStrategy == null)
		{
			this.httpClientStrategy = new SimpleHttpClientStrategy();
			logger.warn("No strategy detected; using Simple strategy");
		}
	}
}
