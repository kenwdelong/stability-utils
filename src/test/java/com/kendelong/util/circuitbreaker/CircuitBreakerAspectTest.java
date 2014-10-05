package com.kendelong.util.circuitbreaker;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.fail;

import org.aspectj.lang.ProceedingJoinPoint;
import org.junit.Before;
import org.junit.Test;


public class CircuitBreakerAspectTest
{
	private CircuitBreakerAspect aspect;
	private ProceedingJoinPoint mockPjp;
	private Object returnValue;

	@Before
	public void setUp()
	{
		aspect = new CircuitBreakerAspect();
		mockPjp = createMock(ProceedingJoinPoint.class);
		returnValue = new Object();
	}

	@Test
	public void testNormalCallGoesThrough() throws Throwable
	{
		expect(mockPjp.proceed()).andReturn(returnValue);
		replay(mockPjp);
		
		Object result = aspect.applyCircuitBreaker(mockPjp);
		assertSame(returnValue, result);
		verify(mockPjp);		
	}
	
	@Test
	public void testFailsFastAfterInvocationFailures() throws Throwable
	{
		int threshold = 3;
		aspect.setFailureThreshold(threshold);
		expect(mockPjp.proceed()).andThrow(new RuntimeException()).times(threshold);
		replay(mockPjp);
		
		for(int i = 0; i < threshold; i++)
		{
			try
			{
				aspect.applyCircuitBreaker(mockPjp);
			}
			catch(Throwable e)	// NOPMD
			{
				// the first several calls should fail
			}
		}
		
		// Now, after "threshold" number of calls, the circuit breaker should open, and throw a CBException
		try
		{
			aspect.applyCircuitBreaker(mockPjp);
		}
		catch(CircuitBreakerException cbe) // NOPMD
		{
			// yahoo, this is what we want
		}
		catch(Throwable t)
		{
			fail("Caught the wrong exception on the last call; circuit breaker did not open properly");
		}
	}
	
	@Test
	public void testResetsAfterTimeoutInterval() throws Throwable
	{
		// put it in open state
		aspect.tripBreaker();
		
		int timeout = 100;
		aspect.setRecoveryTimeout(timeout);
		
		// Expect successful completion on second call
		expect(mockPjp.proceed()).andReturn(returnValue);
		replay(mockPjp);
		
		try
		{
			aspect.applyCircuitBreaker(mockPjp);
		}
		catch(CircuitBreakerException e) // NOPMD
		{
			// this is expected; circuit is still open
		}
		Thread.sleep((long) (timeout*1.2));
		Object value = aspect.applyCircuitBreaker(mockPjp);
		verify(mockPjp);
		assertSame(value, returnValue);
		assertEquals("Wrong state", "ClosedState", aspect.getCurrentState());
	}
	
	@Test
	public void testHalfOpenGoesClosedIfItSucceeds() throws Throwable
	{
		// put it in the HalfOpen state
		aspect.attemptReset();

		// This is how many times to call after the reset 
		int count = 3;
		
		// Expect successful completion on second call
		int totalCalls = count + 1;
		expect(mockPjp.proceed()).andReturn(returnValue).times(totalCalls);
		replay(mockPjp);
		
		aspect.applyCircuitBreaker(mockPjp);  // this call succeeds and resets the breaker
		
		for(int i = 0; i < count; i++)
			aspect.applyCircuitBreaker(mockPjp);  // Now we are in closed state so it should keep working
		
		verify(mockPjp);
	}

	@Test
	public void testHalfOpenGoesOpenIfItFails() throws Throwable
	{
		// put it in the HalfOpen state
		aspect.attemptReset();

		expect(mockPjp.proceed()).andThrow(new RuntimeException());
		replay(mockPjp);
		
		try
		{
			aspect.applyCircuitBreaker(mockPjp);  // this call fails and should trip breaker
			fail("This is supposed to fail; the test is hosed");
		}
		catch(RuntimeException e1) // NOPMD
		{			
			// this is ok, just keep swimming...
		}
		
		try
		{
			aspect.applyCircuitBreaker(mockPjp);
			fail("Breaker didn't open again");
		}
		catch(CircuitBreakerException e)  // NOPMD
		{			
			// should be here
		}
		catch(Throwable t)
		{
			fail("Caught wrong exception, should be CircuitBreakerException " + t.getClass().getName());
		}
		
		verify(mockPjp);
	}
	
	@Test
	public void testResetGoesToClosedState()
	{
		aspect.tripBreaker();
		assertEquals("Tripped breaker should be open", "OpenState", aspect.getCurrentState());
		aspect.attemptReset();
		assertEquals("Attempt Reset breaker should be closed", "HalfOpenState", aspect.getCurrentState());
		aspect.reset();
		assertEquals("Reset breaker should be closed", "ClosedState", aspect.getCurrentState());
	}

	@Test
	public void testFailureCountResetsWhenBreakerDoes() throws Throwable
	{
		int threshold = 3;
		aspect.setFailureThreshold(threshold);
		expect(mockPjp.proceed()).andThrow(new RuntimeException()).times(threshold);
		replay(mockPjp);
		
		for(int i = 0; i < threshold; i++)
		{
			try
			{
				aspect.applyCircuitBreaker(mockPjp);
				assertEquals("Wrong error count", i, aspect.getCurrentFailureCount());
			}
			catch(Throwable e)	// NOPMD
			{
				// the first several calls should fail
			}
		}

		aspect.reset();
		assertEquals("Error count didn't reset", 0, aspect.getCurrentFailureCount());
	}
	

}
