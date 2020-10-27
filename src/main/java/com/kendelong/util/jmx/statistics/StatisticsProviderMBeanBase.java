package com.kendelong.util.jmx.statistics;

import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import org.springframework.jmx.export.annotation.ManagedAttribute;
import org.springframework.jmx.export.annotation.ManagedOperation;

public class StatisticsProviderMBeanBase implements IStatisticsProvider
{
	private IStatisticsProvider provider;

	@Override
	@ManagedAttribute(description="The average number of requests or accesses per second since this object was created")
	public double getAverageRequestsPerSecond()
	{
		return provider.getAverageRequestsPerSecond();
	}

	@Override
	@ManagedAttribute(description="The number of exceptions, or remote return codes indicating exceptions on the remote system")
	public long getNumberOfErrors()
	{
		return provider.getNumberOfErrors();
	}

	@Override
	@ManagedAttribute(description="The number of failed operations - non-exceptions but where the operation failed to have the desired effect")
	public long getNumberOfFailures()
	{
		return provider.getNumberOfFailures();
	}

	@Override
	@ManagedAttribute(description="The number of successful invocations")
	public long getNumberOfSuccesses()
	{
		return provider.getNumberOfSuccesses();
	}

	@Override
	@ManagedAttribute
	public long getTotalNumberOfAccesses()
	{
		return provider.getTotalNumberOfAccesses();
	}

	@Override
	@ManagedOperation(description="Reset statistics to zero")
	public void resetStatistics()
	{
		provider.resetStatistics();
	}

	@ManagedAttribute(description="The table of errors data")
	public String getErrorHistogramAsHtml() 
	{
		Map<String, AtomicLong> errorMap = getErrorHistogram();
		String headingTitle = "validation error";
		return convertMapToTable(errorMap, headingTitle);
	}

	protected String convertMapToTable(Map<String, AtomicLong> errorMap, String headingTitle)
	{
		StringBuilder sb = new StringBuilder();
		sb.append("<table border=\"1\" style=\"font-size: 80%;\"><tr><th>" + headingTitle + "</th><th>count</th></tr>");
		for (Map.Entry<String, AtomicLong> entry : errorMap.entrySet())
		{
			sb.append("<tr><td>");
			sb.append(entry.getKey());
			sb.append("</td><td>");
			sb.append(entry.getValue());
			sb.append("</td></tr>");
		}
		sb.append("</table>");
		return sb.toString();
	}

	public IStatisticsProvider getProvider()
	{
		return provider;
	}

	public void setProvider(IStatisticsProvider provider)
	{
		this.provider = provider;
	}

	@Override
	public Map<String, AtomicLong> getErrorHistogram() 
	{
		return provider.getErrorHistogram();
	}
}
