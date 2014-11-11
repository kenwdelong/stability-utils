package com.kendelong.util.monitoring.webservice;

import static org.junit.Assert.*;

import org.junit.Test;

public class ExternalNameElementComputerTest
{

	@Test
	public void testNormalClassLeftAlone()
	{
		String cn = "com.kendelong.Foo";
		ExternalNameElementComputer computer = new ExternalNameElementComputer();
		assertEquals(cn, computer.cleanClassName(cn));
	}

	@Test
	public void givenEnhancedClassName_returnsClean()
	{
		String cn = "com.kendelong.BrokenService$$EnhancerBySpringCGLIB$$c9a8cfbb";
		ExternalNameElementComputer computer = new ExternalNameElementComputer();
		assertEquals("com.kendelong.BrokenService", computer.cleanClassName(cn));
	}

}
