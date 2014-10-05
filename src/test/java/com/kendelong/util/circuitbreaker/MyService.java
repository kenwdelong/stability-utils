package com.kendelong.util.circuitbreaker;

public class MyService
{

	@CircuitBreakable
	public void serviceMethod()
	{
		throw new RuntimeException();
	}
}
