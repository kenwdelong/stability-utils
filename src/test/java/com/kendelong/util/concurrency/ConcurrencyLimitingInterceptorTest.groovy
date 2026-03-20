package com.kendelong.util.concurrency;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.fail;

import org.aopalliance.intercept.MethodInvocation;
import org.aspectj.lang.ProceedingJoinPoint;
import org.junit.Test;
import org.aspectj.lang.Signature

import com.kendelong.util.concurrency.ConcurrencyLimitExceededException;
import com.kendelong.util.concurrency.ConcurrencyLimitingAspect;

public class ConcurrencyLimitingInterceptorTest
{
	private Signature signature = { return 'methodName' } as Signature

	@Test(expected=ConcurrencyLimitExceededException.class)
	public void testZeroLimitBlowsUp() throws Throwable
	{
		ConcurrencyThrottle annotation = [ threadLimit: { 0 }, annotationType: { ConcurrencyThrottle.class } ] as ConcurrencyThrottle
		ConcurrencyLimitingAspect interceptor = new ConcurrencyLimitingAspect();
		def pjp = { signature } as ProceedingJoinPoint
		interceptor.applyConcurrencyThrottle(pjp, annotation);
		fail("It should have blown up");
	}
	
	@Test
	public void testFiniteLimitAllowsCall() throws Throwable
	{
		ConcurrencyThrottle annotation = [ threadLimit: { 20 }, annotationType: { ConcurrencyThrottle.class } ] as ConcurrencyThrottle
		ProceedingJoinPoint mi = createMock(ProceedingJoinPoint.class);
		expect(mi.getSignature()).andReturn(signature)
		expect(mi.proceed()).andReturn(null);
		replay(mi);
		
		ConcurrencyLimitingAspect interceptor = new ConcurrencyLimitingAspect();
		interceptor.applyConcurrencyThrottle(mi, annotation);
		verify(mi);
	}
	
	// Testing concurrency is really hard...left as an exercise for the reader.
}
