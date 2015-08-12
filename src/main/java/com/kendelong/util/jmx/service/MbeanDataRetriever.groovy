package com.kendelong.util.jmx.service;

import java.math.MathContext

import javax.management.MBeanAttributeInfo;
import javax.management.MBeanServer
import javax.management.ObjectInstance
import javax.management.ObjectName

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component

import com.kendelong.util.performance.PerformanceMonitor

@Component
public class MbeanDataRetriever
{
	@Value('${stability.baseDomain}')
	private String baseDomain
	
	private def perfDomains = ['performance', 'webservice.client.performance', 'webservice.endpoint.performance']
	
	@Autowired
	private MBeanServer mbeanServer
	
	public Object getBeansData()
	{
		def result = [:]
		perfDomains.each {
			domain ->
			def performance = getPerformanceMbeans("${baseDomain}.${domain}:*", { key, value -> !key.contains('.') })
			result[domain] = performance
		}
		result['circuitBreakers'] = getCircuitBreakerMbeans("${baseDomain}.circuitbreaker:*")
		result['concurrencyThrottles'] = getConcurrencyThrottleMbeans("${baseDomain}.concurrencyThrottle:*")
		result['retries'] = getRetryMbeans("${baseDomain}.retriedOperations:*")

		return result
	}
	
	public def getMethodData(String beanName)
	{
		return getPerformanceMbeans(beanName, { key, value -> key.contains('.') } )
	}
	
	private def getPerformanceMbeans(String domain, def filter)
	{
		def sigFigs = new MathContext(4)
		ObjectName domainOname = new ObjectName(domain)
		Set<ObjectInstance> objectInstances = mbeanServer.queryMBeans(domainOname, null)
		def data = objectInstances.collect {
			ObjectInstance objectInstance ->
			ObjectName oname = objectInstance.name
			Map<String, PerformanceMonitor> monitors = mbeanServer.getAttribute(oname, 'Monitors')?.findAll { key, value -> filter(key, value) }
			if(!monitors) return
			def datalist = monitors.collect {
				name, monitor ->
				def myData = [:]
				myData['name'] = name
				myData['oname'] = oname.toString()
				myData['min'] = monitor.getMinimumResponseTime()
				myData['max'] = monitor.getMaximumResponseTime()
				myData['avg'] = new BigDecimal(monitor.getAverageResponseTime()).round(sigFigs).doubleValue()
				myData['rpm'] = new BigDecimal(monitor.getAccessesPerMinute()).round(sigFigs).doubleValue()
				myData['ex'] = monitor.getNumberOfExceptions()
				myData['numAccess'] = monitor.getNumberOfAccesses()
				myData['cumulative'] = new BigDecimal(monitor.getCumulativeTime()).round(sigFigs).doubleValue()
				return myData
			}
			return datalist
		}
		return data.flatten().findAll{ it != null }
	}
	
	private def getCircuitBreakerMbeans(String domain)
	{
		ObjectName domainOname = new ObjectName(domain)
		Set<ObjectInstance> objectInstances = mbeanServer.queryMBeans(domainOname, null)
		def data = objectInstances.collect {
			ObjectInstance objectInstance ->
			ObjectName oname = objectInstance.name
			def myData = [:]
			myData['name'] = oname.getKeyProperty('bean')
			myData['oname'] = oname.toString()
			myData['failureCount'] = mbeanServer.getAttribute(oname, 'CurrentFailureCount')
			myData['state'] = mbeanServer.getAttribute(oname, 'CurrentState')
			myData['threshold'] = mbeanServer.getAttribute(oname, 'FailureThreshold')
			myData['recovery'] = mbeanServer.getAttribute(oname, 'RecoveryTimeout')
			myData['lastTripTime'] = mbeanServer.getAttribute(oname, 'TimeOfLastTrip')
			myData['timeSinceLastTrip'] = mbeanServer.getAttribute(oname, 'TimeSinceLastTripInSeconds')
			myData['timeToNextRetry'] = mbeanServer.getAttribute(oname, 'TimeToNextRetry')
			myData['totalNumTrips'] = mbeanServer.getAttribute(oname, 'TotalNumberOfTrips')
			return myData
		}
		return data
	}

	private def getConcurrencyThrottleMbeans(String domain)
	{
		ObjectName domainOname = new ObjectName(domain)
		Set<ObjectInstance> objectInstances = mbeanServer.queryMBeans(domainOname, null)
		def data = objectInstances.collect {
			ObjectInstance objectInstance ->
			ObjectName oname = objectInstance.name
			def myData = [:]
			myData['name'] = oname.getKeyProperty('bean')
			myData['oname'] = oname.toString()
			myData['threadCount'] = mbeanServer.getAttribute(oname, 'ThreadCount')
			myData['threadLimit'] = mbeanServer.getAttribute(oname, 'ThreadLimit')
			myData['tripCount'] = mbeanServer.getAttribute(oname, 'TripCount')
			return myData
		}
		return data
	}
	
	private def getRetryMbeans(String domain)
	{
		ObjectName domainOname = new ObjectName(domain)
		Set<ObjectInstance> objectInstances = mbeanServer.queryMBeans(domainOname, null)
		def data = objectInstances.collect {
			ObjectInstance objectInstance ->
			ObjectName oname = objectInstance.name
			def myData = [:]
			myData['name'] = oname.getKeyProperty('bean')
			myData['oname'] = oname.toString()
			myData['maxRetries'] = mbeanServer.getAttribute(oname, 'MaxRetries')
			myData['numAccesses'] = mbeanServer.getAttribute(oname, 'NumberOfAccesses')
			myData['retriedOperations'] = mbeanServer.getAttribute(oname, 'RetriedOperations')
			myData['failedOperations'] = mbeanServer.getAttribute(oname, 'FailedOperations')
			def failedMethods = mbeanServer.getAttribute(oname, 'FailedMethodsData')
			myData['failedMethods'] = failedMethods.collect { [name: it.key, failures: it.value.get()] }
			return myData
		}
		return data
	}
}
