package com.kendelong.util.monitoring.webservice;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.kendelong.util.performance.MonitorPerformance;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@MonitorPerformance
public @interface WebServiceEndpoint
{

}
