package com.kendelong.util.circuitbreaker;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;


/**
 * This is the state that we go to when we are detecting serious errors from the
 * remote service.  In this state, the breaker is tripped and we don't send any calls
 * to the remote service, thus giving it time to catch its breath and perhaps come 
 * online.  After "timeout" milliseconds, we go "half-open" and make another 
 * connection attempt.
 *
 * @author kdelong
 */
public class OpenState implements ICircuitBreakerState
{
	private final AtomicInteger timeout = new AtomicInteger();
	private final AtomicLong tripTime = new AtomicLong();
	private final int DEFAULT_TIMEOUT = 60000;
	
	public OpenState()
	{
		timeout.set(DEFAULT_TIMEOUT);
	}
	
	public void preInvoke(CircuitBreakerAspect circuitBreakerAspect) throws Throwable
	{
		long now = System.currentTimeMillis();
		long elapsed = now - tripTime.get();
		if(elapsed > timeout.get())
		{
			circuitBreakerAspect.attemptReset();
		}
		else
		{
			throw new CircuitBreakerException("Circuit Breaker is open; calls are failing fast");
		}
	}

	public void postInvoke(CircuitBreakerAspect circuitBreakerAspect) throws Throwable
	{
		// NO OP
	}

	public void onError(CircuitBreakerAspect circuitBreakerAspect, Throwable t) throws Throwable
	{
		// NO OP
	}


	void trip()
	{
		long now = System.currentTimeMillis();
		tripTime.set(now);
	}

	void setRecoveryTimeout(int timeoutVal)
	{
		timeout.set(timeoutVal);
	}

	public int getRecoveryTimeout()
	{
		return timeout.get();
	}

	public long getTimeToNextRetry()
	{
		long now = System.currentTimeMillis();
		long trip = tripTime.get();
		long timeOut = timeout.get();
		return timeOut - (now - trip);
	}

}
