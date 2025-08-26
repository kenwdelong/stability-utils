package com.kendelong.util.monitoring.webservice;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.kendelong.util.performance.MonitorPerformance;

/**
 * Drop this annotation, along with @MonitorPerformance, on a class that serves as a web service endpoint (typically a controller).  This will cause the 
 * bean to be registered in a JMX domain that includes "webservice.endpoint", and for any data sent to Graphite (if it's configured)
 * to also be under a similar namespace.  In this way, you have an easy way to keep track of all your webservices that you've implemented
 * automatically - they won't be "forgotten".
 * 
 * @author Ken
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@MonitorPerformance
@Inherited
public @interface WebServiceEndpoint
{

}
