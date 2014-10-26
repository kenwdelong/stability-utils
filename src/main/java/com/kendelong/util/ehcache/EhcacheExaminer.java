package com.kendelong.util.ehcache;

import java.text.NumberFormat;
import java.util.Iterator;
import java.util.List;

import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Ehcache;

import org.springframework.jmx.export.annotation.ManagedAttribute;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedOperationParameter;
import org.springframework.jmx.export.annotation.ManagedOperationParameters;
import org.springframework.jmx.export.annotation.ManagedResource;

@ManagedResource(objectName="net.sf.ehcache:name=CacheContentsExaminer",
				description="Looking inside the EhCaches")
public class EhcacheExaminer 
{
	@ManagedOperation(description="Show the keys for the items in the given cache")
	@ManagedOperationParameters
	({
		@ManagedOperationParameter(name="cacheManagerName", description="Name of the cache manager (pick from above list)"),
		@ManagedOperationParameter(name="cacheName", description="Name of the cache (pick from above list)")
	})
	public String listKeysFor(String cacheManagerName, String cacheName)
	{
		List<?> keys = null;
		boolean managerFound = false;
		for(CacheManager cacheManager: CacheManager.ALL_CACHE_MANAGERS)
		{
			if(cacheManager.getName().equals(cacheManagerName))
			{
				managerFound = true;
				try
				{
					Ehcache cache = cacheManager.getEhcache(cacheName);
					if(cache == null)
					{
						return "Cache " + cacheName + " not found";
					}
					keys = cache.getKeys();
					break;
				}
				catch(Exception e)
				{
					return e.toString();
				}
			}
		}
		if(!managerFound)
		{
			return "CacheManager " + cacheManagerName + " not found";
		}
		
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
	public String examineCacheContentsWithStringKey(String cacheManagerName, String cacheName, String key)
	{
		Object o = null;
		Ehcache cache = findCache(cacheManagerName, cacheName);
		if(cache != null)
			o = cache.get(key);
		else
			return "Cache " + cacheName + " not found.";
		if(o != null)
			return o.toString();
		else
			return "null";
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
