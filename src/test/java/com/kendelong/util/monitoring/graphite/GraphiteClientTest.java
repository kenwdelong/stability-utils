package com.kendelong.util.monitoring.graphite;

import java.util.Random;

import org.junit.Ignore;
import org.junit.Test;

public class GraphiteClientTest
{

	@Test
	@Ignore
	public void test() throws Exception
	{
		GraphiteClient gc = new GraphiteClient();
		gc.setAppName("gc");
		gc.setStatsdHost("127.0.01");
		gc.setStatsdPort(8125);
		gc.setServerEnv("test");
		gc.init();
		
		System.out.println("Starting...");
		
		Random random = new Random();
		for(int i = 0; i < 10000; i++)
		{
			gc.increment("theTesterIncrement");
			gc.count("theTesterCount", 1);
			gc.time("theTesterTime", (long) (300*(1 + random.nextDouble() - 0.5)));
			Thread.sleep(100);
			if(i%100 == 0) System.out.println("Count: " + i);
		}
		
		System.out.println("Done");
	}

}
