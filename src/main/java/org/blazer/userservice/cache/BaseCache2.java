package org.blazer.userservice.cache;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;

@Component(value = "baseCache2")
public abstract class BaseCache2 {

	@Autowired
	CacheManager cacheManager;

	public abstract String getCacheName();

	protected Cache getCache() {
		return cacheManager.getCache(getCacheName());
	}

}
