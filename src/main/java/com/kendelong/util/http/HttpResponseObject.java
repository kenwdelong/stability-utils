package com.kendelong.util.http;

import org.apache.http.Header;

public class HttpResponseObject
{
	private Header[] headers;
	private int statusCode;
	private String statusText;
	private String body;
	private long length;

	private int getResponseCodeRange()
	{
		return getStatusCode()/100;
	}
	
	public boolean isError()
	{
		return isClientError() || isServerError();
	}
	
	public boolean isInformational()
	{
		return getResponseCodeRange() == 1;
	}
	
	public boolean isSuccess()
	{
		return getResponseCodeRange() == 2;
	}
	
	public boolean isRedirect()
	{
		return getResponseCodeRange() == 3;
	}
	
	public boolean isClientError()
	{
		return getResponseCodeRange() == 4;
	}
	
	public boolean isServerError()
	{
		return getResponseCodeRange() == 5;
	}
	
	public boolean isPermanentRedirect()
	{
		return getStatusCode() == 301;
	}
	
	public boolean isNormal200Success()
	{
		return getStatusCode() == 200;
	}
	
	public Header[] getHeaders()
	{
		return headers;
	}

	public void setHeaders(Header[] headers)
	{
		this.headers = headers;
	}

	public int getStatusCode()
	{
		return statusCode;
	}

	public void setStatusCode(int statusCode)
	{
		this.statusCode = statusCode;
	}

	public String getBody()
	{
		return body;
	}

	public void setBody(String body)
	{
		this.body = body;
	}

	public String getStatusText()
	{
		return statusText;
	}

	public void setStatusText(String statusText)
	{
		this.statusText = statusText;
	}

	public long getLength()
	{
		return length;
	}

	public void setLength(long length)
	{
		this.length = length;
	}
	
	@Override
	public String toString()
	{
		return getBody();
	}

}
