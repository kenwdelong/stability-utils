Stability Utilities
===================
This is a set of utility classes and libraries that help with making servers operate safely under high load.  Most of these ideas come from 
Michael Nygard's book _Release It_, although a couple are my own invention. They are meant for Spring application running on the JVM (Java,
Groovy, etc).

All of these interceptors expose their state to JMX. This exposes the internal operations of the application to the outside world, which
means it can be inspected and monitored.  This is a good thing.

# Performance Monitoring
Package **com.kendelong.util.performance**.  This is a simple Spring interceptor which wraps the joinpoints specified by the pointcuts in
 _PerformanceMonitoringAspect_ 
and adds monitoring of calls, min/max/avg execution time, and exceptions.  The results are exported via JMX as HTML tables (see my jmx-console
project, also in Github at https://github.com/kenwdelong/jmx-console).  The HTML is generated by the Groovy class _ReportFormatter_.

By default, it wraps and exports any beans with names like `*Controller` or `*Service`.  You can also monitor other beans by adding the 
`MonitorPerformance` aspect on the class.

## Configuration

You need to configure the bean in the Spring context as a prototype:

	<bean class="com.kendelong.util.performance.PerformanceMonitoringAspect" scope="prototype"/>

Then, in order to see the data (no use collecting data if you can't see it!), export the bean to JMX:

	<context:mbean-export/>    
	<bean id="asectJmxExporter" class="com.kendelong.util.spring.JmxExportingAspectPostProcessor" lazy-init="false">
		<property name="mbeanExporter" ref="mbeanExporter"/>
		<property name="annotationToServiceNames">
			<map>
				<entry key="com.kendelong.util.performance.PerformanceMonitoringAspect" value="performance" />
			</map>
		</property>
		<property name="jmxDomain" value="app.mystuff"/>
	</bean>
	
This will export each monitor to the JMX OName of *app.mystuff:service=performance,bean=yourSpringBeanName*.

The configuration is the same for the other beans described below.

# Circuit Breakers

This component protects downstream services from traffic when under duress.  If there are a certain number (*failureThreshold*) of errors in a row (default 3), the 
circuit breaker will open.  After a certain amount of time (*recoveryTimeout*) (default 60 seconds), the aspect will try the next call. If it succeeds, everything
goes back to normal; if not, it waits another 60 seconds. That way, we back off while the downstream system recovers, instead of pounding it
with retry attempts while it is suffering.

The aspect uses the GoF State Pattern for the implementation.

Note that in Spring AOP, only one aspect instance proxies the entire bean, even if more than one method is annotated. So the state of the 
component will be shared across method calls.

Configuration is the same as for the Performance Monitor above.

# Concurrency Throttle

This component limits the number of threads parked on a given resource.  For example, if you have a remote service call that normally takes
200 ms, and is called 5 times/second, you'd expect on average about one thread in that component. If there are 20 threads hanging out there,
something is wrong.  Allowing more threads in to hang up, consume system resources, delay user responses and harass the downstream system is
just throwing gas on the fire.  This component will throw a runtime exception if the number of concurrent threads exceeds a maximum number
(*threadLimit*).

Annotate methods to be throttled with the `ConcurrencyThrottle` annotation.

Configuration is the same as for the Performance Monitor above.

Note that in Spring AOP, only one aspect instance proxies the entire bean, even if more than one method is annotated. So the state of the 
component will be shared across method calls.

# Retry Interceptor

Sometimes an operation that fails should simply be retried. The classic example of this is an optimistic concurrency control exception. Assuming
the other thread is done updating the record, you may simply want to try your update again. With this aspect, you can supply a list of 
exception names that you want to catch and retry.  Up to a certain number (*maxRetries*) retry attempts will be made.

Annotate methods to be retried with the `RetryableOperation` annotation.

Configuration is the same as for the Performance Monitor above.

Note that in Spring AOP, only one aspect instance proxies the entire bean, even if more than one method is annotated. So the state of the 
component will be shared across method calls.

# Graphite Monitoring

Graphite is a real-time charting tool developed at Etsy. It's easy to use and configure. Statsd is a node app that is generally used to front
Graphite and bucket up some of the calls.

Add the `GraphiteClient` to your application context. Use it like you would use a logger; at appropriate places in the code type

	graphiteClient.increment("myapp.logins");
	
Now if you check your Graphite server, you'll have a graph called "myapp.logins" with a realtime graph. It's that simple, it's like logging
to a graph instead of a log file.

Bucket names are prefixed with strings that you set in the Spring configuration:

	<bean class="com.kendelong.util.monitoring.graphite.GraphiteClient">
		<property name="serverEnv" value="prod" />
		<property name="appName" value="website" />
		<property name="serverType" value="tomcat" />
		<property name="host" value="my.statsd.host" />
		<property name="port" value="my.statsd.port" />
	</bean>
	
This will yield a prefix of `prod.website.tomcat.hostname`.  So if you type

	graphiteClient.increment("login.success");
	
the final bucket is  `prod.website.tomcat.hostname.login.success`.

There is also a log4j (v1) appender that writes the log levels to a graphite bucket. You need to configure it in the Spring application context:

	<bean class="com.kendelong.util.monitoring.graphite.GraphiteAppender">
		<property name="graphiteClient" ref="graphiteClient"/>
	</bean>
	
as well as in your log4j configuration

	<appender name="GRAPHITE" class="com.kendelong.util.monitoring.graphite.GraphiteAppender"/>
	<root>
		...
		<appender-ref ref="GRAPHITE"/>
	</root>
   
With this configuration, if your application writes a ERROR log message to the logs, you will also get a count in the Graphite bucket of
`prod.website.tomcat.hostname.logs.ERROR`. This is crazy useful for realtime log monitoring; assuming your logs are clean (!) you can watch
the ERROR graph like a hawk, and also look for things like increased log activity in general (an attack?), DEBUG logs in production, etc.

# JMX Utilities

## Log Configurer
This is a simple MBean that allows you to get/set log levels through the JMX console. Convenient to turn things on and off at runtime.  Works
with log4j version 1.

Just instantiate it:

	<bean class="com.kendelong.util.jmx.LogConfigurer"/>

The MBean name is *logging:service=logger*.

## Statistics Provider
Kind of legacy code, but perhaps useful. A small framework for instrumenting Spring beans with success/failure/error metrics. The bean to be 
instrumented should create an internal final instance of `StatisticsProvider` and also implement `IStatisticsProvider`, delegating all methods
therein to the provider instance. Then you can create an empty subclass of `StatisticsProviderMBeanBase`, feed it the instrumented bean as
*provider*, and export that MBean to JMX (generally by adding a `ManagedResource` annotation).

It's a bit clunky, as it was developed back in the Spring 2.x days, but included here for reference, and maybe it will help someone.

# HTTP Connection Service
A wrapper for Apache httpclient.  Hopefully it makes the simple operations easy, while giving sophisticated control over timeouts and other
important attributes.  Httpclient configuration is not exactly the most intuitive that you might run across... 

This code was originally written for httpclient 3.1, then ported to 4.1.  Now it's upgraded for 4.3.  The APIs are quite different in these
versions.  This latest work has not yet been battle-tested. I've tested what I could (socket timeout, retrieveConnectionTimeout) on my dev laptop.

## Configuration

You need to instantiate the service and the connection pool manager:

	<bean class="com.kendelong.util.http.HttpConnectionService">
		<property name="httpClientStrategy" ref="pooledHttpStrategy"/>
	</bean>
	
	<bean id="pooledHttpStrategy" class="com.kendelong.util.http.PooledHttpClientStrategy">
		<property name="connectionTimeoutInMs" value="1"/>
		<property name="socketTimeoutInMs" value="6000"/>
		<property name="retrieveConnectionTimeoutInMs" value="1000"/>
		<property name="maxConnectionsPerHost" value="2"/>
		<property name="maxTotalConnections" value="2"/>
	</bean>

If you want just a single-threaded, single-connection source (just for testing) use the SimpleHttpClientStrategy instead of the Pooled one.

# EhCache Utilities

There are a couple of classes for helping with EhCache.  

First the `EhcacheJmxBootstrapper` registers the EhCache caches with JMX. The 
`ManagementService` provided with EhCache needs the `CacheManager`, but when the `CacheManager` is instantiated and managed by Hibernate,
I don't see how to get the reference to it for the `ManagementService` constructor.  So this bean runs as a Spring bean with a `@PostConstruct`
method that registers all the caches.

The other class is `EhcacheExaminer`, which is another MBean that provides visibility into the contents of the EhCaches. Under dire 
circumstances you may need to delve into the internals of the caches.

EhCache registers the MBeans under net.sf.ehcache in JMX.  That is also where the `EhcacheExaminer` bean is.

## Configuration

Just create the beans:

	<context:mbean-server/>    
	<bean class="com.kendelong.util.ehcache.EhcacheJmxBootstrapper" autowire="byType"/>
	<bean class="com.kendelong.util.ehcache.EhcacheExaminer"/>

You can customize what gets exported from the bootstrapper by setting the boolean properties on the bean (not shown here). 