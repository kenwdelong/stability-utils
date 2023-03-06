package com.kendelong.util.retry;

import static org.junit.Assert.assertEquals;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.reflect.SourceLocation;
import org.aspectj.runtime.internal.AroundClosure;
import org.junit.Before;
import org.junit.Test;


public class RetryInterceptorTest
{
	private RetryInterceptor interceptor;
	private ProceedingJoinPoint pjp;
	private RetryableOperation ann;

	@Before
	public void setUp()
	{
		interceptor = new RetryInterceptor();
		pjp = getJoinpoint();
		ann = [msToFirstRetry: { 100 }, maxRetries: { 2 }] as RetryableOperation
	}

	@Test
	public void testThrowsException()
	{
		interceptor.getExceptionClassesToRetry().add(UnsupportedOperationException.class);
		try
		{
			interceptor.doConcurrentOperation(pjp, ann);
		}
		catch(Throwable e)
		{
		}
		assertEquals(2, interceptor.getRetriedOperations());
		assertEquals(1, interceptor.getNumberOfAccesses());
	}
	
	@Test
	public void testUnknownException()
	{
		interceptor.getExceptionClassesToRetry().add(IllegalStateException.class);
		try
		{
			interceptor.doConcurrentOperation(pjp, ann);
		}
		catch(Throwable e)
		{
		}
		assertEquals(0, interceptor.getRetriedOperations());
		assertEquals(1, interceptor.getNumberOfAccesses());
	}

	@Test
	public void testThrowsExceptionWithClassForName() throws ClassNotFoundException
	{
		interceptor.getExceptionClassesToRetry().add((Class<? extends Exception>) Class.forName("java.lang.UnsupportedOperationException"));
		try
		{
			interceptor.doConcurrentOperation(pjp, ann);
		}
		catch(Throwable e)
		{
		}
		assertEquals(2, interceptor.getRetriedOperations());
		assertEquals(1, interceptor.getNumberOfAccesses());
	}
	

	protected ProceedingJoinPoint getJoinpoint()
	{
		return new MyPJP();
	}
	
	class MyPJP extends ProceedingJoinPoint
	{
		@Override
		public Object proceed() throws Throwable
		{
			throw new UnsupportedOperationException();
		}

		@Override
		public Object proceed(Object[] args) throws Throwable
		{
			throw new UnsupportedOperationException();
		}
		
		@Override
		public String toShortString()
		{
			return null;
		}
		
		@Override
		public String toLongString()
		{
			return null;
		}
		
		@Override
		public Object getThis()
		{
			return null;
		}
		
		@Override
		public Object getTarget()
		{
			return null;
		}
		
		@Override
		public StaticPart getStaticPart()
		{
			return null;
		}
		
		@Override
		public SourceLocation getSourceLocation()
		{
			return null;
		}
		
		@Override
		public Signature getSignature()
		{
			return new MySignature()
		}
		
		@Override
		public String getKind()
		{
			return null;
		}
		
		@Override
		public Object[] getArgs()
		{
			return null;
		}
		
		@Override
		public void set$AroundClosure(AroundClosure arc)
		{
		}
		
	};

	class MySignature extends  Signature
	{
		
		@Override
		public String toShortString()
		{
			return "foo";
		}
		
		@Override
		public String toLongString()
		{
			return null;
		}
		
		@Override
		public String getName()
		{
			return null;
		}
		
		@Override
		public int getModifiers()
		{
			return 0;
		}
		
		@Override
		public String getDeclaringTypeName()
		{
			return null;
		}
		
		@Override
		public Class<?> getDeclaringType()
		{
			return null;
		}
	};

}
