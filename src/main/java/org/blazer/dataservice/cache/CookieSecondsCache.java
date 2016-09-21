package org.blazer.dataservice.cache;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;

@Component(value = "cookieSecondsCache")
public class CookieSecondsCache {

	private static final int DEFAULT_SECONDS = 60 * 30;

	private Map<String, Integer> cookieSecondsMap = new ConcurrentHashMap<String, Integer>();

	public void put(String key, Integer value) {
		cookieSecondsMap.put(key, value);
	}

	public Integer get(String key) {
		if (key == null) {
			return DEFAULT_SECONDS;
		}
		if (!cookieSecondsMap.containsKey(key)) {
			cookieSecondsMap.put(key, DEFAULT_SECONDS);
		}
		return cookieSecondsMap.get(key);
	}

}
