Stability Utilities
===================
This is a set of utility classes and libraries that help with making servers operate safely under high load.  Most of these ideas come from 
Michael Nygard's book _Release It_, although a couple are my own invention. They are meant for Spring application running on the JVM (Java,
Groovy, etc).

All of these interceptors expose their state to JMX. This exposes the internal operations of the application to the outside world, which
means it can be inspected and monitored.  This is a good thing.

This artifact is available on Maven Central.

	<dependency>
	    <groupId>com.github.kenwdelong</groupId>
	    <artifactId>stability-utils</artifactId>
	    <version>3.0.3</version>
	</dependency>

## Releases
### HEAD
- TBD

### 3.0.3 (April 1, 2024)
- Add explicit path to the "bean" method in `JmxController` because the monitor page was broken.

### 3.0.2 (Oct. 4, 2023)
- There was a deployment error with 3.0.1, so I incremented to 3.0.2
- Change the order on the AOP aspects (see below).
- Update dependency version management to Spring Boot 3.1.4

### 3.0.0 (June 27, 2023)
- Update to Spring 6, which involves the package change from `javax.servlet` -> `jakarta.servlet`
- Use the Spring Boot parent POM for dependency version management
- Removal of EhCache JMX functionality (at least for now)

### 2.1.1 (May 19, 2023)
- Update `GraphiteClient` so that you have the option of just using the first part of the hostname

### 2.1.0 (March 6, 2023)
- Update retry interceptor to parameterize the annotation with the delay time and max attempts

### 2.0.1 (Dec. 16, 2022)
- Remove Log4j support (`GraphiteAppender` and `LogConfigurer` - LMK if you want it back)
- Put `stop()` methods on the `LogbackGraphiteAppender` and `GraphiteClient` to stop the `NonBlockingStatsDClient` (it has a thread)

### 2.0.0 (Dec. 16, 2022)
- Update to Java 17
- Update dependencies to match Spring Boot 2.7.4
    - Spring 5.3.23
    - Groovy 3.0.13
    - Httpclient 4.5.13
- Update to GmavenPlus 2.1.0

### 1.7.6 (Feb. 4, 2021)
- Include the date on the circuit breaker time of last trip

### 1.7.5 (Oct. 27, 2020)
- Re-release

### 1.7.4 (Oct. 27, 2020)
- Botched release
- Update dependencies to match Spring Boot 2.3.4
    - Spring 5.2.9
    - Groovy 2.5.13
    - Httpclient 4.5.12
- Update to GMavenPlus 1.10.1    
- Change project to compile with Java 11

### 1.7.3 (Jan. 8, 2019)
- Fix bug where, when lenient SSL was used, HTTP requests were no longer supported. 

### 1.7.2 (Oct. 8, 2018)
- Fix bug where, on the monitor page, the non-performance MBeans (CircuitBreakers, RetryInterceptors, and ConcurrencyThrottles) were not visible if the bean was marked as `@WebServiceEndpoint` or `@WebServiceClient`. Only the Performance Monitor MBeans are treated specially on this page. 

### 1.7.1 (Oct. 5, 2018)
- Update `HttpConnectionService` to handle a null Entity in the response

### 1.7.0 (Sept. 27, 2018)
- Update dependencies to be like Spring Boot 2.0.5
    - Update to httpclient 4.5.6
    - Update to Spring 5.0.9
- Change Groovy compilation to use GMaven instead of Groovy Eclipse Compiler
- Remove deprecated methods from `HttpConnectionService`
    - Change the lenient SSL factory implementation
    - Change to the new stale connection check method (note: this might be a breaking change if you set this manually, see `validateAfterInactivityMs` in `PooledHttpClientStrategy` and [Commons Httpclient docs](https://hc.apache.org/httpcomponents-client-ga/httpclient/apidocs/org/apache/http/impl/conn/PoolingHttpClientConnectionManager.html#getValidateAfterInactivity())
- Remove the `soLinger` setting in `HttpConnectionService` - I don't think it's a good idea to set it the way it was. 

### 1.6.1 (April 19, 2018)
- format the numbers on the monitor web page a little nicer to make for easier comparisons

### 1.6.0 (July 24, 2017)
- add a request object to stop the proliferation of request signatures in HttpConnectionService

### 1.5.0 (July 12, 2017)
- add ability to do PUTs and PATCHes to HttpConnectionService
- add ability to disable chunked transfer encoding to HttpConnectionService
- updated dependencies

### 1.4.1 (Dec 15, 2016)
- Have `GraphiteAppender` log the prefix it's using for graphite buckets

### 1.4.0 (November 28, 2016)
- hard-code the order into the `@Order` annotation (see below)
- updated dependencies

### 1.3.10 (June 7, 2016)
- make it slightly easier to subclass the controller to customize the path

### 1.3.9 (March 21, 2016)
- fixed EhcacheExaminer to actually display the individual cache entries

### 1.3.8 (March 17, 2016)
- top o' the mornin' to ya!
- added escalating delay to retry interceptor
- added logging of retried calls to retry interceptor
- refactored some code in EhcacheExaminer

### 1.3.7 (February 15, 2016)
- turn off (by default) the sending of method-level data to Graphite from the performance monitor. To re-enable, see the documentation section below.

### 1.3.6 (December 29, 2015)
- add headers to `HttpConnectionService`

### 1.3.5 (November 25, 2015)
- add ability to the the thread limit on `ConcurrencyThrottle` on a bean-by-bean basis

### 1.3.4 (September 21, 2015)
- add ability to reset all the monitors on an interceptor

### 1.3.3 (September 15, 2015)
- fix ridiculous bug in calculating requests per minute and hour

### 1.3.2 (September 4, 2015)
- added Spring's Ordered interface so that users can control the order of the interceptors

### 1.3.1 (August 12, 2015)
- add a web page to view the state of all the stability monitors (see screen shot below)
- made all includes https

### 1.2.2 (July 23, 2015)
- alter PooledHttpClientStrategy to reuse the threadsafe HttpClient instance and add stale connection checking 

### 1.2.1 (May 28, 2015)
- alter GraphiteClient to replace dots in the hostname with dashes, so they don't get interpreted as graphite buckets. 

### 1.2.0 (May 9, 2015)
- added support for logback with the LogbackGraphiteAppender bean.

### 1.1.1 (Feb 1, 2015)
- fixed error on re-exporting JMX beans
- reduced log level for performance monitor to trace
- removed auto-proxying of beans named `*Service`, use `@MonitorPerformance` annotation
- fix HttpService JMX bean
- add exposure of connection pool stats to HttpService JMX bean

### 1.1.0
- Java 8, Groovy 2.3, Spring 4.1

### 1.0.0
- Java 7, Groovy 2.1, Spring 4.0

# Performance Monitoring
Package **com.kendelong.util.performance**.  This is a simple Spring interceptor which wraps the joinpoints specified by the pointcuts in
 _PerformanceMonitoringAspect_ 
and adds monitoring of calls, min/max/avg execution time, and exceptions.  The results are exported via JMX as HTML tables (see my jmx-console
project, also in Github at https://github.com/kenwdelong/jmx-console).  The HTML is generated by the Groovy class _ReportFormatter_.

By default, it wraps and exports any beans with names like `*Controller`.  You can also monitor other beans by adding the 
`MonitorPerformance` aspect on the class.  As of 1.1.1, we no longer auto-proxy beans like `*Service` because there are so many services
running around, the proxying might be too fine-grained (like Springs converter services, e.g.).

## Graphite

The aspect will send its data to Graphite if you give it a GraphiteClient reference (see below).  Up until 1.3.6, each bean sent the number of accesses and the 
execution time for the bean as a whole as well as for each method on the bean.  Beware - this can result in a ton of traffic to your graphite server, 
because StatsD will send data for each counter and timer it has in memory every 10 seconds (by default). So every method that was ever called will have a 
counter and a timer, and it will be written to your Whipser storage area every 10 seconds. At least on an AWS EBS mount, that causes a lot of IOPS, and forces
you to provision much larger volumes than needed for the size of the data.

Therefore, As of 1.3.7, we no longer send method data by default.  The sending of Controller method data is controlled by a new attribute on the aspect 
called `sendControllerMethodDataToGraphite`. It is `false` by default. For beans that are annotated with `@MonitorPerformance`, the annotation
has a new attribute called `sendMethodDataToGraphite`, which is also false by default. This can be used to turn on the method-level data on a
bean-by-bean basis

## Configuration

You need to configure the bean in the Spring context as a prototype (both properties are optional):

	<bean class="com.kendelong.util.performance.PerformanceMonitoringAspect" scope="prototype">
		<property name="graphiteClient" ref="graphiteClient"/>
		<property name="sendControllerMethodDataToGraphite" value="false"/>
	</bean>

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

# WebServices

Have you ever implemented a controller that serves as a web service endpoint for an external client, or written client code that calls
out to an external web service (maybe through http-client), and then forgotten about them?  Or years later, your boss asks for a list of all
the external dependencies to the app, and you have to comb through the code looking for suspects?  Then these annotations are for you.

`@WebServiceEndpoint` - drop this annotation on any controller that that is picked up by the performance monitor, and when it registers the
controller in JMX or sends data to Graphite, it will add "webservice.endpoint" to the JMX OName and the Graphite bin.  In this way, all your endpoints
"register" with both systems, so that when the boss asks for the list, you just run to the JMX Console https://github.com/kenwdelong/jmx-console
and get the list.

`@WebServiceClient` - add this annotation on any class that calls out to external web services, for a similar effect to the `@WebServiceEndpoint` annotation. 

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

Annotate methods to be throttled with the `ConcurrencyThrottle` annotation.  As of 1.3.5, the annotation can take an integer attribute
called `threadLimit` (default 20).  The only way to set the limit is when a thread goes thru the aspect, so at startup it will have the default
value until you call the component.  Also, do not annotate two methods on the same bean with different thread limits - they will fight, each
setting the limit on each call, leading to unpredictable behavior.

Configuration is the same as for the Performance Monitor above.

Note that in Spring AOP, only one aspect instance proxies the entire bean, even if more than one method is annotated. So the state of the 
component will be shared across method calls.

# Retry Interceptor

Sometimes an operation that fails should simply be retried. The classic example of this is an optimistic concurrency control exception. Assuming
the other thread is done updating the record, you may simply want to try your update again. With this aspect, you can supply a list of 
exception names that you want to catch and retry.  Up to a certain number (*maxRetries*) retry attempts will be made.

Annotate methods to be retried with the `RetryableOperation` annotation.

The interceptor will pause `retryBaseDelayInMs`*retryNumber.  `retryBaseDelayInMs` is 100 by default, so the first retry will wait 100 ms before
invoking, the second retry will wait 200 ms, etc.  If you have a high number for *maxRetries* you might want to make `retryBaseDelayInMs` smaller so
that you don't tie up your thread for long periods of time. [added in 1.3.8]

Configuration is the same as for the Performance Monitor above.

Note that in Spring AOP, only one aspect instance proxies the entire bean, even if more than one method is annotated. So the state of the 
component will be shared across method calls.

# Ordering of Aspects (introduced in 1.4.0)
The ordering of the aspects in this library is important. As many of the aspects carry per-joinpoint state, they need to be instantiated as prototype
scope in Spring. However, I (just noticed)[https://stackoverflow.com/questions/40768177/spring-aop-prototype-scoped-aspects-are-firing-out-of-order] that the order is not being respected for prototype aspect instances.  I've submitted a (bug)[https://jira.spring.io/browse/SPR-14959] with Spring.  Until that can get fixed, we need to take a different approach here.  In this case,
the `@Order` annoation _is_ respected.  So I need to hard-code the order values into the annotations.  This is not ideal, as it might conflict
with other order values in the including project. But there's not much else I can see to do at the moment.

The orders are, from highest priority (first interceptor encountered):
- CircuitBreaker (order=100) - if you are going to fail fast, then fail _fast_
- RetryInterceptor (order=200) - clear out all the state below before retrying
- ConcurrencyThrottle (order=300)
- PerformanceMonitor (order=400) - this is debatable, you might want to include all the retries, or the time spent waiting at the throttle, as part of the invocation time. For the time being, you are stuck (you can always fork this and change the value yourself).

After this, I suggest you set values on your own project as such:
- TransactionInterceptor: order = 500 (e.g., `@EnableTransactionManagement(order=500)` in your JavaConfig) - if you are retrying, you want the transaction cleared, or if you are waiting at the throttle you do not want a transaction open!
- MethodSecurityInterceptor: order = 600 (e.g., `<global-method-security pre-post-annotations="enabled" order="600">` in XML config) - my method security accesses the database to check permissions, I'd like that to run in the same transaction (and Hibernate session) as the actual operation.

If/when the Spring bug is fixed, I will remove the hard-coded `@Order` annoations.

## Update of Oct 4, 2023
I changed the orders to 
- RetryInterceptor (order=100) - clear out all the state below before retrying. Add `com.kendelong.util.concurrency.ConcurrencyLimitExceededException` if you want to retry threads that arrive in a crowd. _Do not_ add `com.kendelong.util.circuitbreaker.CircuitBreakerException` or you will possibly never reset your breakers!
- ConcurrencyThrottle (order=150) - This guys aggressively throws exceptions after the thread limit is reached, causing problems in interaction with the other two interceptors.
- CircuitBreaker (order=200) - Move this down, because you don't want concurrency exceptions triggering the breaker. Otherwise a storm of threads in _your_ code could trip it, but it's supposed to be watching for problems in the _remote_ system.
- PerformanceMonitor (order=250) - same

If this ordering is not what you want, I'm afraid there's no recourse except to fork the project and change the values in the `@Ordered` annotations.

# Graphite Monitoring

Graphite is a real-time charting tool developed at Etsy. It's easy to use and configure. Statsd is a node app that is generally used to front
Graphite and bucket up some of the calls.  This project contains support for Graphite integration with log4j and logback.

## log4j

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

## logback
Configure exactly the same as above, but use LogbackGraphiteAppender.

# JMX Utilities

## Log Configurer
This is a simple MBean that allows you to get/set log levels through the JMX console. Convenient to turn things on and off at runtime.  Works
with log4j version 1.  There is no support for logback, because logback has native support for this with the `<jmxConfigurator/>` tag.

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

There is an MBean you can export `PooledHttpClientStrategyAdmin` that will allow you to change the timeout settings at runtime.

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

EhCache registers the MBeans under *net.sf.ehcache* in JMX.  That is also where the `EhcacheExaminer` bean is.

## Configuration

Just create the beans:

	<context:mbean-server/>    
	<bean class="com.kendelong.util.ehcache.EhcacheJmxBootstrapper" autowire="byType"/>
	<bean class="com.kendelong.util.ehcache.EhcacheExaminer"/>

You can customize what gets exported from the bootstrapper by setting the boolean properties on the bean (not shown here).

# Web Interface 
## Added in 1.3.0

If you have a JMX viewer like JConsole or my [JMX Console](https://github.com/kenwdelong/jmx-console) you can always view the MBeans, but always one-by-one.  In version 1.3.0 a single web page was added that will allow you to see all of them at a glance.

## Configuration

There are only two classes, `com.kendelong.util.jmx.web.controller.JmxController` and `com.kendelong.util.jmx.web.service.MbeanDataRetriever`.  For default configuration, just add those packages to your Spring component-scanning path.  If you are using only one application context, like Spring Boot, you can just add `com.kendelong.util.jmx.web`.  If you have a traditional app with a root and a web context, you can add the controller to the web context and the `MbeanDataRetriever` to the root context (or add them both to the web context).  If you do this, the web page will be exposed on the URL `/admin/monitor`.

There is one parameter to specify in your properties: `stability.baseDomain`.  This is the prefix for the JMX domain used to register the MBeans, and should be the same value that you inject into your `com.kendelong.util.spring.JmxExportingAspectPostProcessor`'s `jmxDomain` property. If you are using two different contexts, you can use the same value for `jmxDomain` for both.

There are other configuration options as well:

- If you want to change the URL, you can only instantiate the `MbeanDataRetriever`, and write your own controller.  You can look at the `JmxController`, it's dirt simple and delegates everything.  Just create your own controller and autowire in the `MbeanDataRetriever`.
- Or, if you don't want to, or can't, add component scanning to the packages mentioned, you can use a standard bean definition to load either or both of the beans.
- Or, you can create your own controller and just instantiate the `MbeanDataRetriever`. Note that you'll have to provide the `MBeanServer` and `baseDomain` properties.

## Screen Shot

![screenshot](https://raw.github.com/kenwdelong/stability-utils/master/misc/Console.jpg)