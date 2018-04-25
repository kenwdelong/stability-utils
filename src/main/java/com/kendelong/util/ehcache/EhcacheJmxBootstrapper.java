package com.kendelong.util.ehcache;

import javax.annotation.PostConstruct;
import javax.management.MBeanServer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.sf.ehcache.CacheManager;
import net.sf.ehcache.management.ManagementService;


/**
 * This class is used to register all the EhCache level-2 caches with JMX
 * so that cache statistics etc. are available in the JMX console.
 * EhCache has this ManagementService class that needs to be called via
 * a static method, and that method needs a CacheManager.  The CacheManager
 * class has a static list of all the CacheManagers, so you need to register
 * them all one-by-one.  Weird API.
 * @author kdelong
 *
 */
// I think this could/should be a BeanFactoryPostProcessor
public class EhcacheJmxBootstrapper
{
	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	
	private MBeanServer mbeanServer;
	private boolean registerManager = true;
	private boolean registerCaches = true;
	private boolean registerConfigurations = false;
	private boolean registerStatistics = true;
	
	@PostConstruct
	public void init()
	{
		// The CacheManager has a static List of all CM instances.
		for(CacheManager cacheMgr : CacheManager.ALL_CACHE_MANAGERS)
		{
			ManagementService.registerMBeans(cacheMgr, mbeanServer, registerManager, registerCaches, registerConfigurations, registerStatistics);
			logger.info("Registering EhCache CacheManager with MBean server " + cacheMgr.getName());
		}
	}

	public MBeanServer getMbeanServer()
	{
		return mbeanServer;
	}

	public void setMbeanServer(MBeanServer mbeanServer)
	{
		this.mbeanServer = mbeanServer;
	}

	public boolean isRegisterManager()
	{
		return registerManager;
	}

	public void setRegisterManager(boolean registerManager)
	{
		this.registerManager = registerManager;
	}

	public boolean isRegisterCaches()
	{
		return registerCaches;
	}

	public void setRegisterCaches(boolean registerCaches)
	{
		this.registerCaches = registerCaches;
	}

	public boolean isRegisterConfigurations()
	{
		return registerConfigurations;
	}

	public void setRegisterConfigurations(boolean registerConfigurations)
	{
		this.registerConfigurations = registerConfigurations;
	}

	public boolean isRegisterStatistics()
	{
		return registerStatistics;
	}

	public void setRegisterStatistics(boolean registerStatistics)
	{
		this.registerStatistics = registerStatistics;
	}
	
}
