package com.kendelong.util.ehcache;

import java.lang.reflect.Method;
import java.text.NumberFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jmx.export.annotation.ManagedAttribute;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedOperationParameter;
import org.springframework.jmx.export.annotation.ManagedOperationParameters;
import org.springframework.jmx.export.annotation.ManagedResource;

import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Element;

/**
 * Allows one to use JMX to examine the contents of the Ehcaches in the JVM.  Meant for display in an HTML page.
 * 
 * @author Ken
 */
@ManagedResource(objectName="net.sf.ehcache:name=CacheContentsExaminer",
				description="Looking inside the EhCaches")
public class EhcacheExaminer 
{
	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	@ManagedOperation(description="Show the keys for the items in the given cache")
	@ManagedOperationParameters
	({
		@ManagedOperationParameter(name="cacheManagerName", description="Name of the cache manager (pick from above list)"),
		@ManagedOperationParameter(name="cacheName", description="Name of the cache (pick from above list)")
	})
	public String listKeysFor(String cacheManagerName, String cacheName)
	{
		List<?> keys = null;
		
		Ehcache cache = findCache(cacheManagerName, cacheName);
		if(cache == null)
		{
			logger.warn("Could not find cache [{}] in cacheManager [{}]", cacheName, cacheManagerName);
			return "Cache not found";
		}
		keys = cache.getKeys();
		
		StringBuilder keyNames = new StringBuilder();
		if(keys != null)
		{
			for(Object key : keys)
			{
				String s = key.toString();
				keyNames.append(s + "\n");
			}
		}
		return keyNames.toString();
	}

	@ManagedAttribute(description="The cache managers and (indented) their associated caches.")
	public String getAllCacheManagers()
	{
		StringBuilder names = new StringBuilder();
		for(CacheManager cacheManager: CacheManager.ALL_CACHE_MANAGERS)
		{
			names.append("<P>" + cacheManager.getName() + "<BR/>");
			String[] cacheNames = cacheManager.getCacheNames();
			for(String cacheName : cacheNames)
			{
				names.append(" - " + cacheName + "<BR/>");
			}
		}
		
		return names.toString();
	}
	
	@ManagedOperation(description="This will show the size of each cache, but it does so by serializing the content" +
			"and measuring the size of the resulting byte array.  For a full cache, this method could take a minute" +
			"or so of processing, so never call it in prod unless it's a low traffic time")
	public String showCacheMemorySizesDontUseInProd()
	{
		long total = 0;
		String format = "%,9d";
		StringBuilder sb = new StringBuilder();
		sb.append("<script src=\"/js/sorttable.js\"></script>");
		sb.append("<script src=\"/webassets/js/sorttable.js\"></script>");
		sb.append("<table border=\"2\" class=\"sortable\"><tbody><tr><th>Cache Name</th><th>Manager</th><th>Number</th><th>Cache Size</th></tr>");
		for(CacheManager cacheManager: CacheManager.ALL_CACHE_MANAGERS)
		{
			String managerName = cacheManager.getName();
			String[] cacheNames = cacheManager.getCacheNames();
			for(int i = 0; i < cacheNames.length; i++)
			{
				String cacheName = cacheNames[i];
				Ehcache cache = cacheManager.getEhcache(cacheName);
				int numElements = cache.getSize();
				long size = cache.calculateInMemorySize();
				String s = String.format(format, size);
				sb.append("<tr><td>").append(cacheName).append("</td><td>").append(managerName)
					.append("</td><td>").append(numElements)
					.append("</td><td style=\"text-align: right;\">").append(s).append("</td></tr>");
				total += size;
			}
		}
		sb.append("</tbody></table>");
		sb.append("\nTotal: ").append(NumberFormat.getIntegerInstance().format(total));
		sb.append(" ").append("bytes");
		return sb.toString();
	}
	
	@ManagedOperation(description="Show the item in the cache for the key")
	@ManagedOperationParameters
	({
		@ManagedOperationParameter(name="cacheManagerName", description="Name of the cache manager (pick from above list)"),
		@ManagedOperationParameter(name="cacheName", description="Name of the cache (pick from above list)"),
		@ManagedOperationParameter(name="key", description="The key for the item (use listKeysFor() above)")
	})
	public String examineCacheContentsWithStringKey(String cacheManagerName, String cacheName, String keyName)
	{
		Element element = null;
		Ehcache cache = findCache(cacheManagerName, cacheName);
		
		if(cache == null)
		{
			return "Cache " + cacheName + " not found.";
		}

		// Keys are not always Strings!!  So we have to use this wonky way of locating the actual key we want
		@SuppressWarnings("unchecked")
		Object key = cache.getKeys().stream().filter(k -> keyName.equals(k.toString())).findFirst().orElse(null);
		if(key != null)
		{
			element = cache.get(key);
		}
		else
		{
			return "Key [" + keyName + "] could not be located";
		}
		
		if(element == null) return "null object returned from cache";
		
		StringBuilder builder = new StringBuilder();
		builder.append("Creation time: ").append(new Date(element.getCreationTime())).append("<br/>")
		.append("Last access time: ").append(new Date(element.getLastAccessTime())).append("<br/>")
			.append("Expiration time: ").append(new Date(element.getExpirationTime())).append("<br/>")
			.append("value: ").append(element.getObjectValue().toString()).append("<br/>");
		
		try
		{
			// When used with hibernate (in 4.3.11, anyway) the values are wrapped in AbstractReadWriteEhcacheAccessStrategy$Item classed
			// Those hold CacheEntries, and so on and so on. This helps get out a litte more information
			Object value = element.getObjectValue();
			Method getValueMethod = value.getClass().getMethod("getValue", new Class<?>[]{} );	
			Object o = getValueMethod.invoke(value, new Object[]{});
			builder.append("Inner value: ").append(o.toString()).append("<br/>");
		}
		catch(Exception e)
		{
			logger.warn("Problem unwrapping Ehcache elements: " + e.getMessage());
		}
		
		return builder.toString();
	}

	private Ehcache findCache(String cacheManagerName, String cacheName)
	{
		Ehcache cache = null;
		List<CacheManager> cacheManagers = CacheManager.ALL_CACHE_MANAGERS;
		Iterator<CacheManager> iter = cacheManagers.iterator();
		while(iter.hasNext())
		{
			CacheManager cacheManager = (CacheManager) iter.next();
			if(cacheManager.getName().equals(cacheManagerName))
			{
				cache = cacheManager.getEhcache(cacheName);
				break;
			}
		}		
		return cache;
	}

	@ManagedOperation(description="This will loop through all the caches attached to all the cache managers on this" +
			"machine, and call cache.removeAll() on each cache.  Cache.removeAll() calls are broadcast throughout the " +
			"cluster, so this button should clear all caches in all machings in the cluster. Use with caution: this means " +
			"every server will be pummelling the database to get fresh data at the same time.")
	public void clearAllCaches()
	{
		for(CacheManager cacheManager: CacheManager.ALL_CACHE_MANAGERS)
		{
			for(String cacheName : cacheManager.getCacheNames())
			{
				Ehcache cache = cacheManager.getEhcache(cacheName);
				cache.removeAll();
			}
		}
		
	}

}
