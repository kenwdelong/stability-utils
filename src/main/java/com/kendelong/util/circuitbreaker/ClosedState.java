package com.kendelong.util.circuitbreaker;

import java.util.concurrent.atomic.AtomicInteger;


/**
 * This is a "closed" circuit breaker, i.e. operational, that passes all calls on to the
 * intercepted object.  If the calls are failing (throwing exceptions), if we count 
 * "failureThreshold" exceptions in a row, we trip the breaker and go to Open state.
 * If a successful call arrives we reset the count of errors. 
 *
 * @author kdelong
 */
public class ClosedState implements ICircuitBreakerState
{
	private AtomicInteger failureThreshold = new AtomicInteger();
	private AtomicInteger failureCount = new AtomicInteger();

	public void preInvoke(CircuitBreakerAspect circuitBreakerAspect) throws Throwable
	{
		// NO OP
	}

	public void postInvoke(CircuitBreakerAspect circuitBreakerAspect) throws Throwable
	{
		resetFailureCount();
	}

	void resetFailureCount()
	{
		failureCount.set(0);
	}

	public void onError(CircuitBreakerAspect circuitBreakerAspect, Throwable t) throws Throwable
	{
		int currentCount = failureCount.incrementAndGet();
		int threshold = failureThreshold.get();
		if(currentCount >= threshold)
			circuitBreakerAspect.tripBreaker();
	}

	
	void setFailureThreshold(int threshold)
	{
		failureThreshold.set(threshold);
	}

	public int getFailureThreshold()
	{
		return failureThreshold.get();
	}

	public int getCurrentFailureCount()
	{
		return failureCount.get();
	}

}
