package com.kendelong.util.jmx.statistics;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;

/**
 * A base implementation for adding simple statistics to a class with JMX.
 * 
 * The instrumented class should create a private final instance of this
 * class.  Then, it should call the increment*() methods when it detects a
 * success, failure, or error (every call results in on of these three).  The instrumented
 * class should also implement IStatisticsProvider and delegate all the methods to 
 * contained instance of this class.  Finally, an MBean (which can extend
 * StatisticsProvierMBeanBase for convenience) wraps the instrumented class.
 * 
 * Another possible setup would be to instantiate this class as a Spring
 * Bean, and inject it into both the instrumented class and the MBean.  This 
 * would be infeasible if the instrumented class is not a singleton.
 * 
 * Definitions:
 *   success - duh
 *   failure - the invocation of the service succeeded, but the service indicates
 *   that it was unable to fulfill the request.
 *   error - an exception is detected: something is broken.
 *
 * @author kdelong
 */
public class StatisticsProvider implements IStatisticsProvider
{
	private final AtomicLong successes = new AtomicLong();
	private final AtomicLong failures = new AtomicLong();
	private final AtomicLong errors = new AtomicLong();
	private final AtomicReference<Date> startTime = new AtomicReference<Date>();
	private final Map<String, AtomicLong> errorHistogram = new ConcurrentHashMap<String, AtomicLong>();
	
	public StatisticsProvider()
	{
		startTime.set(new Date());
	}
	
	@Override
	public double getAverageRequestsPerSecond()
	{
		long now = System.currentTimeMillis();
		long start = startTime.get().getTime();
		long elapsedTime = now - start;
		if(elapsedTime == 0)
			return 0;
		return 1000.0*getTotalNumberOfAccesses()/elapsedTime; 
	}

	@Override
	public long getNumberOfErrors()
	{
		return errors.get();
	}

	@Override
	public long getNumberOfFailures()
	{
		return failures.get();
	}

	@Override
	public long getNumberOfSuccesses()
	{
		return successes.get();
	}

	@Override
	public long getTotalNumberOfAccesses()
	{
		return getNumberOfSuccesses() + getNumberOfFailures() + getNumberOfErrors();
	}

	@Override
	public Map<String, AtomicLong> getErrorHistogram()
	{
		return errorHistogram;
	}

	@Override
	public void resetStatistics()
	{
		successes.set(0);
		failures.set(0);
		errors.set(0);
		startTime.set(new Date());
		errorHistogram.clear();
	}

	
	// Internal API for client class
	
	public void incrementErrors()
	{
		errors.incrementAndGet();
	}

	public void incrementFailures()
	{
		failures.incrementAndGet();
	}

	public void incrementSuccess()
	{
		successes.incrementAndGet();
	}

	public synchronized void logValidationFailures(BindingResult errors)
	{
		for (ObjectError error : (List<ObjectError>)errors.getAllErrors())
		{
			String errorName = error.getCode();
			if (errorHistogram.containsKey(errorName))
			{
				// error exists in map -- update!
				AtomicLong count = errorHistogram.get(errorName);
				count.addAndGet(1L);
			}
			else
			{
				// add error type to map
				AtomicLong count = new AtomicLong();
				count.set(1L);
				errorHistogram.put(errorName, count);
			}
		}
	}
	
	/**
	 * Increment an error in the validation map.  We can use the validationError map to 
	 * record validation failures, but also to create histograms of any sort of error
	 * condition.
	 * 
	 * Synchronized to be sure the creation of the atomics are done correctly.  They should
	 * add a "GetOrCreate" atomic method to the map impl.
	 * @param errorName The name of the error
	 */
	public synchronized void addError(String errorName)
	{
		AtomicLong bucket = errorHistogram.get(errorName);
		if(bucket == null)
		{
			bucket = new AtomicLong();
			errorHistogram.put(errorName, bucket);
		}
		bucket.incrementAndGet();
	}
}
