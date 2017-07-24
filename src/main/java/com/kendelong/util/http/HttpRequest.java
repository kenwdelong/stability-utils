package com.kendelong.util.http;

import java.util.HashMap;
import java.util.Map;

public class HttpRequest
{
	private String connectionURL;
	private HttpMethod method; 
	private String data; 
	private String contentType;
	private Map<String, String> headers = new HashMap<>(); 
	private Map<String, String> queryParams = new HashMap<>(); 
	private boolean chunked = true;
	private String encoding = "UTF-8";

	public HttpRequest withUrl(String url)
	{
		this.connectionURL = url;
		return this;
	}
	
	public HttpRequest withMethod(HttpMethod method)
	{
		this.method = method;
		return this;
	}
	
	public HttpRequest withBodyData(String data)
	{
		this.data = data;
		return this;
	}
	
	public HttpRequest withContentType(String contentType) 
	{
		this.contentType = contentType;
		return this;
	}
	
	public HttpRequest addHeader(String name, String value)
	{
		this.headers.put(name, value);
		return this;
	}
	
	public HttpRequest addQueryParameter(String name, String value)
	{
		this.queryParams.put(name, value);
		return this;
	}
	
	public HttpRequest withChunkedEncoding(boolean chunked)
	{
		this.chunked = chunked;
		return this;
	}
	
	public HttpRequest withEncoding(String encoding)
	{
		this.encoding = encoding;
		return this;
	}
	
	// Pre-assembled methods
	
	public HttpRequest postAsXml(String data)
	{
		this.method = HttpMethod.POST;
		this.data = data;
		this.contentType = "text/xml";
		return this;
	}
	
	public HttpRequest postAsJson(String data)
	{
		this.method = HttpMethod.POST;
		this.data = data;
		this.contentType = "application/json";
		return this;
	}
	
	public HttpRequest postAsText(String data)
	{
		this.method = HttpMethod.POST;
		this.data = data;
		this.contentType = "text/plain";
		return this;
	}
	
	// Getters
	
	public String getConnectionURL()
	{
		return connectionURL;
	}

	public HttpMethod getMethod()
	{
		return method;
	}

	public String getData()
	{
		return data;
	}

	public String getContentType()
	{
		return contentType;
	}

	public Map<String, String> getHeaders()
	{
		return headers;
	}

	public Map<String, String> getQueryParams()
	{
		return queryParams;
	}

	public boolean isChunked()
	{
		return chunked;
	}
	
	public String getEncoding()
	{
		return encoding;
	}

}
