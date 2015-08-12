package com.kendelong.util.performance;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class PerformanceMonitor
{
	private AtomicInteger numberOfAccesses = new AtomicInteger();
	private AtomicInteger numberOfSuccesses = new AtomicInteger();
	private AtomicInteger numberOfExceptions = new AtomicInteger();
	private AtomicLong sumOfAllSuccesses = new AtomicLong();
	private AtomicLong maximum = new AtomicLong();
	private AtomicLong minimum = new AtomicLong(Long.MAX_VALUE);
	private AtomicLong startTime = new AtomicLong();
	
	public PerformanceMonitor()
	{
		long startTimeMillis = getTime();
		startTime.set(startTimeMillis);
	}

	protected PerformanceMonitor(long startTimeMillis)
	{
		startTime.set(startTimeMillis);
	}

	protected long getTime()
	{
		return System.currentTimeMillis();
	}

	public int getNumberOfAccesses()
	{
		return numberOfAccesses.get();
	}

	public void addTiming(long val)
	{
		numberOfAccesses.incrementAndGet();	
		numberOfSuccesses.incrementAndGet();
		sumOfAllSuccesses.addAndGet(val);
		if(val > maximum.get()) maximum.set(val);
		if(val < minimum.get()) minimum.set(val);
	}

	public void addException()
	{
		numberOfAccesses.incrementAndGet();
		numberOfExceptions.incrementAndGet();
	}

	public double getAverageResponseTime()
	{
		int successes = numberOfSuccesses.get();
		if(successes == 0) return 0;
		return 1.0*sumOfAllSuccesses.get()/successes;
	}

	public double getMaximumResponseTime()
	{
		return maximum.get();
	}

	public double getMinimumResponseTime()
	{
		long time = minimum.get();
		if(time == Long.MAX_VALUE) time = 0;
		return time;
	}
	
	public double getAccessesPerSecond()
	{
		long now = getTime();
		long delta = now - startTime.get();
		if(delta == 0) return 0;
		return 1000.0*getNumberOfAccesses()/delta;
	}
	
	public double getAccessesPerMinute()
	{
		return getAccessesPerSecond()/60.0;
	}

	public double getAccessesPerHour()
	{
		return getAccessesPerMinute()/60.0;
	}
	
	public int getNumberOfExceptions()
	{
		return numberOfExceptions.get();
	}
	
	public double getCumulativeTime()
	{
		return getAverageResponseTime()*getNumberOfAccesses();
	}
}
