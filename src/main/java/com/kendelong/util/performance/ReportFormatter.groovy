package com.kendelong.util.performance

import groovy.xml.MarkupBuilder;

class ReportFormatter
{
	String formatReport(Map<String, PerformanceMonitor> monitors)
	{
		Map<String, Map<String, PerformanceMonitor>> mapped = monitors.groupBy { it.key.contains('.') ? 'method' : 'bean' }
		def methodByService = mapped.method?.groupBy { it.key.split(/\./)[0] }
		if(!methodByService) return '<div>No Data</div>'
		StringWriter writer = new StringWriter()
		MarkupBuilder html = new MarkupBuilder(writer)
		html.div {
			h2('Service Level Monitors')
			table(border: 2, 'class':'tablesorter') {
				thead {
					tr {
						th('Name')
						th('Minimum')
						th('Maximum')
						th('Average')
						th('Requests/min')
						th('Exceptions')
						th('Number of Accesses')
						th('Cumulative Time')
					}
				}
				tbody {
					mapped.bean.each {
						key, value ->
						tr {
							td(key)
							td(value.minimumResponseTime)
							td(value.maximumResponseTime)
							td(value.averageResponseTime)
							td(value.accessesPerMinute)
							td(value.numberOfExceptions)
							td(value.numberOfAccesses)
							td(value.cumulativeTime)
						}
					}
				}
			}
			h2('Method Level Monitors')
			methodByService.each {
				service, methodMap ->
				h3(service)
				table(border: 2, 'class':'tablesorter') {
					thead {
						tr {
							th('Name')
							th('Minimum')
							th('Maximum')
							th('Average')
							th('Requests/min')
							th('Exceptions')
							th('Number of Accesses')
							th('Cumulative Time')
						}
					}
					tbody {
						methodMap.each {
							key, value ->
							tr {
								td(key)
								td(value.minimumResponseTime)
								td(value.maximumResponseTime)
								td(value.averageResponseTime)
								td(value.accessesPerMinute)
								td(value.numberOfExceptions)
								td(value.numberOfAccesses)
								td(value.cumulativeTime)
							}
						}
					}
				}
			}
		}
		return writer.toString() + '\n' + getSortingCode()
	}
	
	static void main(def args)
	{
		def n = ['entryService', 'scaleService', 'entryService.foo', 'entryService.bar', 'scaleService.foo', 'scaleService.bar' ]
		def m = [:]
		n.each { m[it] = new PerformanceMonitor() }
		def f = new ReportFormatter()
		def s = f.formatReport(m)
		println s
	}
	
	private String getSortingCode()
	{
		def sortCode = '''<script type="text/javascript" src="https://code.jquery.com/jquery-2.1.0.min.js"></script> 
<script type="text/javascript" src="/webassets/jquery.tablesorter.js"></script> 
<script type="text/javascript">
$(document).ready(function() 
{ 
	$(".tablesorter").tablesorter(); 
} 
); 
</script>
'''
		return sortCode
	}
}
