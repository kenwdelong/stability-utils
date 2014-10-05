package com.kendelong.util.circuitbreaker;


/**
 * In this state, the breaker makes an attempt at contacting the remote service. If it's
 * successful, it it resets the breaker and we go back to normal operation.  If it's 
 * unsuccessful, we go back to the Open (error) state.
 *
 * @author kdelong
 */
public class HalfOpenState implements ICircuitBreakerState
{

	public void preInvoke(CircuitBreakerAspect circuitBreakerAspect) throws Throwable
	{
		// NO OP
	}

	public void postInvoke(CircuitBreakerAspect circuitBreakerAspect) throws Throwable
	{
		circuitBreakerAspect.reset();
	}

	public void onError(CircuitBreakerAspect circuitBreakerAspect, Throwable e) throws Throwable
	{
		circuitBreakerAspect.tripBreaker();
		throw new CircuitBreakerException(e);
	}

}
