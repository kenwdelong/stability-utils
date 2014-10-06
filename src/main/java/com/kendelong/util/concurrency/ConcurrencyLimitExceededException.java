package com.kendelong.util.concurrency;


public class ConcurrencyLimitExceededException extends RuntimeException
{

	public ConcurrencyLimitExceededException()
	{
	}

	public ConcurrencyLimitExceededException(String message)
	{
		super(message);
	}

	public ConcurrencyLimitExceededException(Throwable cause)
	{
		super(cause);
	}

	public ConcurrencyLimitExceededException(String message, Throwable cause)
	{
		super(message, cause);
	}

}
