package org.jrtech.common.xsutils;

import java.util.concurrent.TimeUnit;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

public class CacheUtil {

	/**
	 * A wrapper method for creating a cache.
	 * 
	 * @param maxCacheSize maximum numbers of cache entries
	 * @param duration duration to the expiry after access 
	 * @param timeUnit time unit of the duration to the expiry after access 
	 * @return a new cache that has value type according to the destination variable
	 */
	public static final <V> LoadingCache<String, V> createCache(int maxCacheSize, int duration, TimeUnit timeUnit) {
		return CacheBuilder.newBuilder().maximumSize(maxCacheSize).expireAfterAccess(duration, timeUnit)
				.build(new CacheLoader<String, V>() {
					@Override
					public V load(String key) {
						return null;
					}
				});
	}
	
//	/**
//	 * A wrapper method for retrieving cached value.
//	 * 
//	 * @param cache the cache to perform retrieval operation.
//	 * @param key a string value as key to search in the cache.
//	 * @return the value found or null when nothing is found in the cache.
//	 * @throws IllegalArgumentException when null cache is provided to the method. 
//	 */
//	public static final <V> V getValue(LoadingCache<String, V> cache, String key) throws IllegalArgumentException {
//		if (key == null) return null; // null-safe measure
//		
//		if (cache == null) throw new IllegalArgumentException("NULL cache is provided to the method. Please provide an instance of cache accordingly.");
//		
//		return cache.getIfPresent(key);
//	}
//	
//	/**
//	 * A wrapper method for checking the existence of key in a cache.
//	 * 
//	 * @param cache the cache to perform retrieval operation.
//	 * @param key a string value as key to search in the cache.
//	 * @return true -> the key does exist in the cache or <br>
//	 *         false -> the key does not exist in the cache
//	 * @throws IllegalArgumentException when null cache is provided to the method. 
//	 */
//	public static final boolean hasKey(LoadingCache<String, ?> cache, String key) throws IllegalArgumentException {
//		if (key == null) throw new IllegalArgumentException("Key of the cache to search cannot be NULL. Please provide a non NULL value accordingly!");
//		
//		if (cache == null) throw new IllegalArgumentException("NULL cache is provided to the method. Please provide an instance of cache accordingly!");
//		
//		return cache.asMap().containsKey(key);
//	}
}
