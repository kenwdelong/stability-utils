package com.kendelong.util.jmx.web.controller;

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

import com.kendelong.util.jmx.web.service.MbeanDataRetriever;

@RestController
@RequestMapping("/admin/monitor")
public class JmxController
{
	@Autowired
	private MbeanDataRetriever dataRetriever;
	
	@RequestMapping()
	public String getMainPage()
	{
		String page = this.getClass().getClassLoader().getResourceAsStream("com/kendelong/util/jmx/index.html").text;
	}
	
	@RequestMapping("/beans")
	public Object getBeanList()
	{
		dataRetriever.getBeansData()
	}

	@RequestMapping("/bean/{name:.+}")
	public Object getBeanData(@PathVariable String name)
	{
		return dataRetriever.getMethodData(name)
	}
}
