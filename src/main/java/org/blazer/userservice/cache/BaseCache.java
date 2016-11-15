package org.blazer.userservice.cache;

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

	protected Cache getCache() {
		return ehCacheManager.getCacheManager().getCache(getCacheName());
	}

}
