package com.kendelong.util.jmx.statistics;

import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

public interface IStatisticsProvider
{
	// These methods will generally be exposed in JMX
	public long getNumberOfSuccesses();
	public long getNumberOfFailures();
	public long getNumberOfErrors();
	public long getTotalNumberOfAccesses();
	public void resetStatistics();
	public double getAverageRequestsPerSecond();	
	public Map<String, AtomicLong> getErrorHistogram();
}
