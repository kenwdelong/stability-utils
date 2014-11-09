package com.kendelong.util.monitoring.graphite;

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
		gc.setStatsdHost("localhost");
		gc.setStatsdPort(8125);
		gc.setServerEnv("test");
		gc.init();
		
		System.out.println("Starting...");
		
		for(int i = 0; i < 100; i++)
		{
			gc.increment("theTester");
			Thread.sleep(100);
		}
		
		System.out.println("Done");
	}

}
