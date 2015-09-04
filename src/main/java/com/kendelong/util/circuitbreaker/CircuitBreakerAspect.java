package com.kendelong.util.circuitbreaker;

import java.text.DateFormat;
import java.util.Date;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.jmx.export.annotation.ManagedAttribute;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedResource;

import com.kendelong.util.monitoring.graphite.GraphiteClient;

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
 * Configuration is like
 * <pre>
 * {@code
	<bean class="com.kendelong.util.circuitbreaker.CircuitBreakerAspect" scope="prototype">
		<property name="graphiteClient" ref="graphiteClient"/>
	</bean>
			
	<bean id="aspectJmxExporter" class="com.kendelong.util.spring.JmxExportingAspectPostProcessor" lazy-init="false">
		<property name="mbeanExporter" ref="mbeanExporter"/>
		<property name="annotationToServiceNames">
			<map>
				<entry key="com.kendelong.util.circuitbreaker.CircuitBreakerAspect" value="circuitbreaker"/>
			</map>
		</property>
		<property name="jmxDomain" value="app.mystuff"/>
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
public class CircuitBreakerAspect implements Ordered
{	
	private final AtomicReference<ICircuitBreakerState> state = new AtomicReference<ICircuitBreakerState>();
	private final int DEFAULT_FAILURE_THRESHOLD = 3;
	private final ClosedState CLOSED_STATE = new ClosedState();
	private final OpenState OPEN_STATE = new OpenState();
	
	private final AtomicInteger totalNumberOfTrips = new AtomicInteger();
	private final AtomicReference<Date> timeOfLastTrip = new AtomicReference<Date>();
	
	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	
	private GraphiteClient graphiteClient;
	
	private final ThreadLocal<String> keys = new ThreadLocal<String>();
	
	private int order = 0;
	
	public CircuitBreakerAspect()
	{
		CLOSED_STATE.setFailureThreshold(DEFAULT_FAILURE_THRESHOLD);
		state.set(CLOSED_STATE);
	}
	
	@Around("@annotation(com.kendelong.util.circuitbreaker.CircuitBreakable)")
	public Object applyCircuitBreaker(ProceedingJoinPoint pjp) throws Throwable
	{
		if(graphiteClient != null)
		{
			String methodKey = "circuitbreaker." + getMethodKey(pjp);
			keys.set(methodKey);
			graphiteClient.increment(methodKey + ".accesses");
		}

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
		finally
		{
			keys.remove();
		}
		return result;
	}

	private String getMethodKey(ProceedingJoinPoint pjp)
	{
		String classKey = StringUtils.substringAfterLast(pjp.getSignature().getDeclaringTypeName(), ".");
		String methodName = pjp.getSignature().getName();
		String methodKey = classKey + "." + methodName;
		return methodKey;
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
		if(graphiteClient != null) graphiteClient.increment(keys.get() + ".trips");
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
		if(graphiteClient != null) graphiteClient.increment(keys.get() + ".resets");
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

	public GraphiteClient getGraphiteClient()
	{
		return graphiteClient;
	}

	public void setGraphiteClient(GraphiteClient graphiteClient)
	{
		this.graphiteClient = graphiteClient;
	}

	@Override
	public int getOrder()
	{
		return order;
	}
	
	public void setOrder(int theOrder)
	{
		order = theOrder;
	}

}
