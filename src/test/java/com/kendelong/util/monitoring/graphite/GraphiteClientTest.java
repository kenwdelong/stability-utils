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
		gc.setStatsdHost("192.168.59.103");
		gc.setStatsdPort(8125);
		gc.setServerEnv("test");
		gc.init();
		
		System.out.println("Starting...");
		
		Random random = new Random();
		for(int i = 0; i < 10000; i++)
		{
			gc.increment("a.foo");
			gc.increment("b.foo");
			gc.count("theTesterCount", 1);
			gc.time("theTesterTime", (long) (300*(1 + random.nextDouble() - 0.5)));
			Thread.sleep(100);
			if(i%100 == 0) System.out.println("Count: " + i);
		}
		
		System.out.println("Done");
	}
	
	/*
	 * gc.increment("a.foo");
	 * gc.increment("b.foo");
	 * 
	 * a.foo.count = 100
	 * Grafana buckets are 10 seconds wide!
	 * 
	 * a.foo.rate = 10
	 * sumSeries(*.foo.rate) = 20
	 * 
	 * $ docker run -p 8000:80/tcp -p 8125:8125/udp -p 8126:8126/tcp --name grafana cooniur/grafana_graphite
	 * http://192.168.59.103:8000/#/dashboard/db/welcome?panelId=3&fullscreen&edit
	 * That IP is the IP of the boot2docker VM!  
	 * Shown when
	 *   - you start boot2docker
	 * 
	 * 
	 */
}
