package com.kendelong.util.monitoring.webservice;

import static org.junit.Assert.*;

import org.junit.Test;

public class ExternalNameElementComputerTest
{
	private	ExternalNameElementComputer computer = new ExternalNameElementComputer();

	@Test
	public void testNormalClassLeftAlone()
	{
		String cn = "com.kendelong.Foo";
		assertEquals(cn, computer.cleanClassName(cn));
	}

	@Test
	public void givenEnhancedClassName_returnsClean()
	{
		String cn = "com.kendelong.BrokenService$$EnhancerBySpringCGLIB$$c9a8cfbb";
		assertEquals("com.kendelong.BrokenService", computer.cleanClassName(cn));
	}
	
	@Test
	public void givenClassWitheWebserviceAnnation_theNameIsCorrect()
	{
		String name = computer.computeExternalNameElement(AnnotationTest.class);
		assertEquals("client", name);
	}

}
