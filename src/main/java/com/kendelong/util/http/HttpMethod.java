package com.kendelong.util.http;

public enum HttpMethod
{
	GET(false), DELETE(false), HEAD(false), POST(true), PUT(true), PATCH(true), OPTIONS(false), TRACE(false);
	
	private boolean supportsEntity;
	
	private HttpMethod(boolean supportsEntity)
	{
		this.supportsEntity = supportsEntity;
	}
	
	public boolean supportsEntity()
	{
		return supportsEntity;
	}
}
