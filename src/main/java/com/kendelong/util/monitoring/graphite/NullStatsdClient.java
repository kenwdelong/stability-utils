package com.kendelong.util.monitoring.graphite;

public class NullStatsdClient implements IStatsdClient
{

	@Override
	public boolean timing(String key, int value)
	{
		return false;
	}

	@Override
	public boolean timing(String key, int value, double sampleRate)
	{
		return false;
	}

	@Override
	public boolean decrement(String key)
	{
		return false;
	}

	@Override
	public boolean decrement(String key, int magnitude)
	{
		return false;
	}

	@Override
	public boolean decrement(String key, int magnitude, double sampleRate)
	{
		return false;
	}

	@Override
	public boolean decrement(String... keys)
	{
		return false;
	}

	@Override
	public boolean decrement(int magnitude, String... keys)
	{
		return false;
	}

	@Override
	public boolean decrement(int magnitude, double sampleRate, String... keys)
	{
		return false;
	}

	@Override
	public boolean increment(String key)
	{
		return false;
	}

	@Override
	public boolean increment(String key, int magnitude)
	{
		return false;
	}

	@Override
	public boolean increment(String key, int magnitude, double sampleRate)
	{
		return false;
	}

	@Override
	public boolean increment(int magnitude, double sampleRate, String... keys)
	{
		return false;
	}

}
