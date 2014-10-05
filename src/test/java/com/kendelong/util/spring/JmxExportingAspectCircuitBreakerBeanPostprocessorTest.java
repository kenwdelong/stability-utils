package com.kendelong.util.spring;

import org.aopalliance.aop.Advice;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.aop.Advisor;
import org.springframework.aop.aspectj.AbstractAspectJAdvice;
import org.springframework.aop.framework.Advised;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.kendelong.util.circuitbreaker.CircuitBreakerAspect;
import com.kendelong.util.circuitbreaker.MyService;

import static org.junit.Assert.*;

@ContextConfiguration("classpath:com/kendelong/util/circuitbreaker/circuitbreaker-test-context.xml")
@RunWith(SpringJUnit4ClassRunner.class)
public class JmxExportingAspectCircuitBreakerBeanPostprocessorTest
{
	@Autowired
	@Qualifier("myService")
	private MyService svc;
	
	@Autowired
	@Qualifier("myService2")
	private MyService svc2;
	
	@Test
	public void testTwoDifferentServicesGetTwoDifferentPojos()
	{
		Object pojo = extractPojo(svc);
		Object pojo2 = extractPojo(svc2);
		assertNotSame(pojo, pojo2);
	}
	
	@Test
	public void testThatTwoAccessGivesTheSameBean()
	{
		Object pojo = extractPojo(svc);
		Object pojo2 = extractPojo(svc);
		assertSame(pojo, pojo2);
	}
	
	@Test
	public void testThatTheStateIsKeptCorrectlyWithTheBean()
	{
		CircuitBreakerAspect pojo = (CircuitBreakerAspect) extractPojo(svc);
		CircuitBreakerAspect pojo2 = (CircuitBreakerAspect) extractPojo(svc2);
		
		invokeServiceMethod(svc);
		invokeServiceMethod(svc);

		assertEquals("Wrong number of errors on svc", 2, pojo.getCurrentFailureCount());
		assertEquals("Svc 2 got incremented", 0, pojo2.getCurrentFailureCount());
				
		invokeServiceMethod(svc2);
		assertEquals("Svc 2 didn't increment", 1, pojo2.getCurrentFailureCount());
		assertEquals("Svc wrongly incremented", 2, pojo.getCurrentFailureCount());
	}

	private void invokeServiceMethod(MyService svc)
	{
		try
		{
			svc.serviceMethod();
		}
		catch(RuntimeException e) // NOPMD
		{
		}
	}
	
	private Object extractPojo(MyService svc)
	{
		Advised advisedBean = (Advised) svc;
		Advisor[] advisors = advisedBean.getAdvisors();
		for(Advisor advisor : advisors)
		{
			Advice advice = advisor.getAdvice();
			if(advice instanceof AbstractAspectJAdvice)
			{
				AbstractAspectJAdvice aaja = (AbstractAspectJAdvice) advice;
				Object target = aaja.getAspectInstanceFactory().getAspectInstance();
				return target;
			}
		}
		return null;
	}

}
