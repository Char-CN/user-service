package org.blazer.userservice.cache;

import org.blazer.userservice.model.CacheModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.ehcache.EhCacheCacheManager;
import org.springframework.stereotype.Component;

import net.sf.ehcache.Cache;

/**
 * 该类用于获取net.sf.ehcache.Cache，从Spring管理的EhCache中获取。
 * 
 * @author hyy
 *
 */
@Component(value = "baseCache")
public abstract class BaseCache {

	@Autowired
	EhCacheCacheManager ehCacheManager;

	public abstract String getCacheName();

	public CacheModel getCalcCacheModel() {
		CacheModel cm = new CacheModel();
		cm.setCacheName(getCacheName());
		cm.setInMemorySize(getCache().getStatistics().getLocalHeapSizeInBytes() / 1024);
		cm.setOffHeapSize(getCache().getStatistics().getLocalOffHeapSizeInBytes() / 1024);
		cm.setOnDiskSize(getCache().getStatistics().getLocalDiskSizeInBytes() / 1024);
		return cm;
	}

	protected Cache getCache() {
		return ehCacheManager.getCacheManager().getCache(getCacheName());
	}

}
