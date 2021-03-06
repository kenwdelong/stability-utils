package com.kendelong.util.concurrency;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.fail;

import org.aopalliance.intercept.MethodInvocation;
import org.aspectj.lang.ProceedingJoinPoint;
import org.junit.Test;

import com.kendelong.util.concurrency.ConcurrencyLimitExceededException;
import com.kendelong.util.concurrency.ConcurrencyLimitingAspect;

public class ConcurrencyLimitingInterceptorTest
{

	@Test(expected=ConcurrencyLimitExceededException.class)
	public void testZeroLimitBlowsUp() throws Throwable
	{
		ConcurrencyThrottle annotation = [ threadLimit: { 0 }, annotationType: { ConcurrencyThrottle.class } ] as ConcurrencyThrottle
		ConcurrencyLimitingAspect interceptor = new ConcurrencyLimitingAspect();
		interceptor.setThreadLimit(0);
		def pjp = {} as ProceedingJoinPoint
		interceptor.applyConcurrencyThrottle(pjp, annotation);
		fail("It should have blown up");
	}
	
	@Test
	public void testFiniteLimitAllowsCall() throws Throwable
	{
		ConcurrencyThrottle annotation = [ threadLimit: { 20 }, annotationType: { ConcurrencyThrottle.class } ] as ConcurrencyThrottle
		ProceedingJoinPoint mi = createMock(ProceedingJoinPoint.class);
		expect(mi.proceed()).andReturn(null);
		replay(mi);
		
		ConcurrencyLimitingAspect interceptor = new ConcurrencyLimitingAspect();
		interceptor.applyConcurrencyThrottle(mi, annotation);
		verify(mi);
	}
	
	// Testing concurrency is really hard...left as an exercise for the reader.
}
