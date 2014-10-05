package com.kendelong.util.circuitbreaker;


public interface ICircuitBreakerState
{

	public void preInvoke(CircuitBreakerAspect circuitBreakerAspect) throws Throwable;
	public void postInvoke(CircuitBreakerAspect circuitBreakerAspect) throws Throwable;
	public void onError(CircuitBreakerAspect circuitBreakerAspect, Throwable t) throws Throwable;

}
