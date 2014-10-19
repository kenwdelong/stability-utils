package com.kendelong.util.monitoring.graphite;

public interface IStatsdClient {

	public abstract boolean timing(String key, int value);

	public abstract boolean timing(String key, int value, double sampleRate);

	public abstract boolean decrement(String key);

	public abstract boolean decrement(String key, int magnitude);

	public abstract boolean decrement(String key, int magnitude,
			double sampleRate);

	public abstract boolean decrement(String... keys);

	public abstract boolean decrement(int magnitude, String... keys);

	public abstract boolean decrement(int magnitude, double sampleRate,
			String... keys);

	public abstract boolean increment(String key);

	public abstract boolean increment(String key, int magnitude);

	public abstract boolean increment(String key, int magnitude,
			double sampleRate);

	public abstract boolean increment(int magnitude, double sampleRate,
			String... keys);

}