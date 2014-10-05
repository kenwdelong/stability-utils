package com.kendelong.util.circuitbreaker;

import java.text.DateFormat;
import java.util.Date;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jmx.export.annotation.ManagedAttribute;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedResource;

import com.kendelong.util.spring.JmxExportingAspectPostProcessor;

/**
 * This is the main Circuit Breaker class.  It is a stateful object, the state variable
 * refers to the current state re the GoF State Pattern.  This is modeled after the 
 * CB in "Release It" by Michael Nygard.
 * 
 * The CB allows all calls to go through as long as they are succeeding. However, if the remote
 * service starts to fail, the breaker will trip and go open, and not allow any calls.  After
 * a bit, it will try the remote service again, and if it has recovered it will start sending
 * work again.  Otherwise, it goes back to Open state.
 * 
 * Note that a unique instance is created for each proxied service.
 * 
 * To configure with "old style AOP" which lets you export each breaker as an MBean, use something like this:
 * 
 * <pre>
 * {@code
  	<bean id="circuitBreakerJmxExporter" class="com.kendelong.util.spring.JmxExportingBeanNameAutoproxyCreator" lazy-init="false">
		<property name="jmxInterceptorClass" value="com.kendelong.util.circuitbreaker.CircuitBreakerAspect" />
		<property name="exporter" ref="callableJmxExporter" />
		<property name="prefix" value="myapp.admin.circuitbreaker" />
		<property name="serviceName" value="circuitBreaker" />
		<property name="graphiteClient" ref="graphiteClient" />
		<property name="beanNames">
			<list>
				<value>MyService</value>
				<value>MyService2</value>
			</list>
		</property>
		<property name="extraProperties">
			<map>
				<entry key="failureThreshold" value="15"/>
				<entry key="recoveryTimeout" value="30000"/>
			</map>
		</property>
	</bean>
	}
	</pre>
 * 
 * See the javadoc for the individual states for deeper explanation.
 * 
 * {@link OpenState}
 * {@link ClosedState}
 * {@link HalfOpenState}
 *
 * @author kdelong
 */
@Aspect
@ManagedResource(description="Circuit Breaker for protecting ourselves against badly behaving remote services")
public class CircuitBreakerAspect implements MethodInterceptor
{	
	private final AtomicReference<ICircuitBreakerState> state = new AtomicReference<ICircuitBreakerState>();
	private final int DEFAULT_FAILURE_THRESHOLD = 3;
	private final ClosedState CLOSED_STATE = new ClosedState();
	private final OpenState OPEN_STATE = new OpenState();
	
	private final AtomicInteger totalNumberOfTrips = new AtomicInteger();
	private final AtomicReference<Date> timeOfLastTrip = new AtomicReference<Date>();
	
	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	
	public CircuitBreakerAspect()
	{
		CLOSED_STATE.setFailureThreshold(DEFAULT_FAILURE_THRESHOLD);
		state.set(CLOSED_STATE);
	}
	
	/**
	 * This one works with AspectJ annotations, but there's no way to export the aspect
	 * to JMX - many of the beans are not eligible for postprocessing (see {@link JmxExportingAspectPostProcessor})
	 * 
	 * @param pjp
	 * @return
	 * @throws Throwable
	 */
	@Around("@annotation(com.kendelong.util.circuitbreaker.CircuitBreakable)")
	public Object applyCircuitBreaker(ProceedingJoinPoint pjp) throws Throwable
	{
		Object result = null;
		try
		{
			getState().preInvoke(this);
			result = pjp.proceed();
			getState().postInvoke(this);
		}
		catch(Throwable t)
		{
			getState().onError(this, t);
			throw t;
		}
		return result;
	}

	/* This is for use with Spring's regular AOP, so we can do JMX exporting.  We could try to consolidate it with the
	 * above AspectJ method, by writing a "closure" that we'd pass to the template method.  But it's a lot of 
	 * trouble and indirection.  So it's back to copy and paste...
	 * 
	 * There's no unit tests for this; we rely on the unit tests for the AspectJ method.
	 * 
	 * @see org.aopalliance.intercept.MethodInterceptor#invoke(org.aopalliance.intercept.MethodInvocation)
	 */
	public Object invoke(MethodInvocation invocation) throws Throwable
	{
		Object result = null;
		try
		{
			getState().preInvoke(this);
			result = invocation.proceed();
			getState().postInvoke(this);
		}
		catch(Throwable t)
		{
			getState().onError(this, t);
			throw t;
		}
		return result;
	}


	
	private ICircuitBreakerState getState()
	{
		return state.get();
	}

	@ManagedAttribute()
	public void setFailureThreshold(int threshold)
	{
		CLOSED_STATE.setFailureThreshold(threshold);
	}

	@ManagedAttribute(description="Number of sucessive failure before we trip the breaker")
	public int getFailureThreshold()
	{
		return CLOSED_STATE.getFailureThreshold();
	}

	@ManagedOperation(description="Open the circuit breaker (disallow calls to remote service)")
	public void tripBreaker()
	{
		OPEN_STATE.trip();
		state.set(OPEN_STATE);
		timeOfLastTrip.set(new Date());
		logger.warn("Circuit breaker tripped; going to OpenState");
		totalNumberOfTrips.incrementAndGet();
	}

	@ManagedAttribute()
	public void setRecoveryTimeout(int timeout)
	{
		OPEN_STATE.setRecoveryTimeout(timeout);
	}
	
	@ManagedAttribute(description="Number of milliseconds to wait before we try the remote service again")
	public int getRecoveryTimeout()
	{
		return OPEN_STATE.getRecoveryTimeout();
	}

	@ManagedOperation(description="Move to half-open state; try the remote service tentatively")
	public void attemptReset()
	{
		// there's no state to maintain in this one, so just create a new object
		// there's not going to be that many of them
		state.set(new HalfOpenState());
		logger.info("Attempting reset; going HalfOpen");
	}

	@ManagedOperation(description="Reset the breaker and go closed (start using the remote service again)")
	public void reset()
	{
		CLOSED_STATE.resetFailureCount();
		state.set(CLOSED_STATE);
		logger.info("Circuit breaker reset; all is happy again");
	}
	
	@ManagedAttribute(description="Number of current failures in the closed state")
	public int getCurrentFailureCount()
	{
		return CLOSED_STATE.getCurrentFailureCount();
	}
	
	@ManagedAttribute(description="When in open state, the number of milliseconds until we try sending another request to the remote service")
	public long getTimeToNextRetry()
	{
		if(state.get() == OPEN_STATE)
			return OPEN_STATE.getTimeToNextRetry();
		else
			return 0;
	}
	
	@ManagedAttribute(description="Total times the circuit breaker has tripped")
	public int getTotalNumberOfTrips()
	{
		return totalNumberOfTrips.get();
	}
	
	@ManagedAttribute(description="Current state of the circuit breaker. Closed State is normal, Open means errors.")
	public String getCurrentState()
	{
		return state.get().getClass().getSimpleName();
	}
	
	@ManagedOperation(description="Reset the number of trips on this breaker to zero")
	public void resetStatistics()
	{
		totalNumberOfTrips.set(0);
		timeOfLastTrip.set(null);
	}
	
	@ManagedAttribute(description="Time of last trip")
	public String getTimeOfLastTrip()
	{
		Date time = timeOfLastTrip.get();
		if(time != null)
		{
			DateFormat df = DateFormat.getTimeInstance(DateFormat.FULL);
			return df.format(time);
		}
		else
		{
			return "";
		}		
	}
	
	@ManagedAttribute(description="Time since the last trip, in seconds")
	public long getTimeSinceLastTripInSeconds()
	{
		Date time = timeOfLastTrip.get();
		if(time != null)
		{
			Date now = new Date();
			return (now.getTime() - time.getTime())/1000;
		}
		else
		{
			return -1;
		}
	}

}
