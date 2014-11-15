package com.kendelong.util.monitoring.webservice;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.kendelong.util.performance.MonitorPerformance;

/**
 * Drop this annotation, along with @MonitorPerformance, on a class that calls out to an external web service.  This will cause the 
 * bean to be registered in a JMX domain that includes "webservice.client", and for any data sent to Graphite (if it's configured)
 * to also be under a similar namespace.  In this way, you have an easy way to keep track of all your webservice clients
 * automatically - they won't be "forgotten".
 * 
 * @author Ken
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@MonitorPerformance
public @interface WebServiceClient
{

}
