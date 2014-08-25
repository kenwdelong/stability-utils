package com.kendelong.util.performance;

import static org.junit.Assert.*;

import org.junit.Test;

public class PerformanceMonitorTest
{
	private PerformanceMonitor monitor = new PerformanceMonitor();
	
	@Test
	public void givenBlank_whenOneTimeEntered_thenItHasOneAccess()
	{
		monitor.addTiming(12);
		assertEquals(1, monitor.getNumberOfAccesses());
	}

	@Test
	public void givenBlank_whenThreeTimesEntered_thenItHasThreeAccesses()
	{
		monitor.addTiming(12);
		monitor.addTiming(12);
		monitor.addTiming(12);
		assertEquals(3, monitor.getNumberOfAccesses());
	}
	
	@Test
	public void givenBlank_whenOneExceptionEntered_thenItHasOneAccess()
	{
		monitor.addException();
		assertEquals(1, monitor.getNumberOfAccesses());
	}

	@Test
	public void givenBlank_whenOneTimeEntered_thenItHasThatAverage()
	{
		long value = 12;
		monitor.addTiming(value);
		assertEquals(value, monitor.getAverageResponseTime(), 0.0001);
	}

	@Test
	public void givenBlank_whenThreeTimesEntered_thenAverageIsCorrect()
	{
		monitor.addTiming(12);
		monitor.addTiming(9);
		monitor.addTiming(17);
		assertEquals(12.6667, monitor.getAverageResponseTime(), 0.0001);
	}

	@Test
	public void givenBlank_whenThreeTimesEntered_thenMaximumIsCorrect()
	{
		monitor.addTiming(12);
		monitor.addTiming(9);
		monitor.addTiming(14);
		assertEquals(14, monitor.getMaximumResponseTime(), 0.0001);
	}

	@Test
	public void givenBlank_whenThreeTimesEntered_thenMinimumIsCorrect()
	{
		monitor.addTiming(12);
		monitor.addTiming(9);
		monitor.addTiming(14);
		assertEquals(9, monitor.getMinimumResponseTime(), 0.0001);
	}
	
	@Test
	public void testAccessesPerSecond()
	{
		class SettablePerformanceMonitor extends PerformanceMonitor
		{
			public SettablePerformanceMonitor(long startTime)
			{
				super(startTime);
			}
			private long time;
			@Override
			protected long getTime()
			{
				return time;
			}
			public void setTime(long t)
			{
				time = t;
			}
		};
		long startTime = 1000;
		SettablePerformanceMonitor monitor = new SettablePerformanceMonitor(startTime);
		for(int i = 0; i < 10; i++) monitor.addTiming(42);
		long stopTime = startTime + 1_000_000;
		monitor.setTime(stopTime);
		assertEquals(0.00001, monitor.getAccessesPerSecond(), 0.00000001);
	}

}
