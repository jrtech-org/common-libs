/*
 * Copyright (c) 2016-2026 Jumin Rubin
 * LinkedIn: https://www.linkedin.com/in/juminrubin/
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jrtech.common.hazelcast;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.map.IMap;
import com.hazelcast.query.Predicate;

/**
 * The class <code>HazelcastOperationHelper</code> provides utilities necessary to work with Hazelcast IMap as caching.
 * 
 */
public class HazelcastOperationHelper {

	public static final String KEY_SEGMENT_SEPARATOR = "/";

	/**
	 * Store a value to cache with the given key as index. <br>
	 * 
	 * @param cacheNameName
	 *            cache name to operate
	 * @param key
	 *            cache key
	 * @param value
	 */
	public static <K, V> void storeToCache(String cacheName, K key, V value) {
		IMap<K, V> cache = getCacheByName(cacheName);
		cache.put(key, value);
	}

	/**
	 * Store a value to cache with the given key as index. <br>
	 * 
	 * @param cacheNameName
	 *            cache name to operate
	 * @param key
	 *            cache key
	 * @param value
	 * @param timeToLiveSeconds
	 */
	public static <K, V> void storeToCache(String cacheName, K key, V value, long timeToLiveSeconds) {
		IMap<K, V> cache = getCacheByName(cacheName);
		cache.put(key, value, timeToLiveSeconds, TimeUnit.SECONDS);
	}

	/**
	 * Store a value to cache with the given key as index. <br>
	 * 
	 * @param cacheNameName
	 *            cache name to operate
	 * @param key
	 *            cache key
	 * @param value
	 */
	public static <K, V> V storeToCacheIfAbsent(String cacheName, K key, V value) {
		IMap<K, V> cache = getCacheByName(cacheName);
		return cache.putIfAbsent(key, value);
	}

	/**
	 * Store a value to cache with the given key as index. <br>
	 * 
	 * @param cacheNameName
	 *            cache name to operate
	 * @param key
	 *            cache key
	 * @param value
	 * @param timeToLiveSeconds
	 */
	public static <K, V> V storeToCacheIfAbsent(String cacheName, K key, V value, long timeToLiveSeconds) {
		IMap<K, V> cache = getCacheByName(cacheName);
		return cache.putIfAbsent(key, value, timeToLiveSeconds, TimeUnit.SECONDS);
	}

	/**
	 * Update cache value for the given key. <br>
	 * 
	 * @param cacheName
	 *            cache name to operate
	 * @param key
	 * @param value
	 * @return copy of the new object value
	 */
	public static <K, V> V updateCache(String cacheName, K key, V value) {
		IMap<K, V> cache = getCacheByName(cacheName);
		cache.put(key, value);

		return value;
	}

	/**
	 * Update cache value for the given key. <br>
	 * 
	 * @param cacheName
	 *            cache name to operate
	 * @param key
	 * @param value
	 * @param timeToLiveSeconds
	 * @return copy of the new object value
	 */
	public static <K, V> V updateCache(String cacheName, K key, V value, long timeToLiveSeconds) {
		IMap<K, V> cache = getCacheByName(cacheName);
		cache.put(key, value, timeToLiveSeconds, TimeUnit.SECONDS);

		return value;
	}

	/**
	 * Remove a value from cache for the given key. <br>
	 * 
	 * @param cacheName
	 *            cache name to operate
	 * @param key
	 */
	public static <K, V> void removeFromCache(String cacheName, K key) {
		IMap<K, V> cache = getCacheByName(cacheName);
		V value = cache.get(key);
		if (value != null) {
			cache.remove(key);
		}
	}

	/**
	 * Remove values from cache for the given keys.
	 * 
	 * @param cacheName
	 *            cache name to operate
	 * @param keys
	 */
	public static <K, V> void removeFromCacheBulk(String cacheName, Collection<K> keys) {
		IMap<K, V> cache = getCacheByName(cacheName);
		for (K key : keys) {
			V value = cache.get(key);
			if (value != null) {
				cache.remove(key);
			}
		}
	}
	
	/**
	 * Remove values from cache for given predicate.
	 * 
	 * @param cacheName
	 * @param predicate
	 */
	public static <K, V> void removeFromCache(String cacheName, Predicate<K, V> predicate) {
		Set<K> keys = queryCacheKey(cacheName, predicate);
		removeFromCacheBulk(cacheName, keys);
	}

	/**
	 * Clear values from cache
	 * 
	 * @param cacheName
	 */
	public static void clearCache(String cacheName) {
		IMap<Object, Object> cache = getCacheByName(cacheName);
		cache.clear();
	}

	/**
	 * Retrieve value from the cache for the given key. <br>
	 * 
	 * @param cacheName
	 *            cache name to operate
	 * @param key
	 * @return copy of the object value
	 */
	public static <K, V> V retrieveCacheValue(String cacheName, K key) {
		IMap<K, V> cache = getCacheByName(cacheName);
		if (cache.isEmpty())
			return null;

		return (V) cache.get(key);
	}

	/**
	 * Check if a cache contains the given key.
	 * 
	 * @param cacheName
	 * @param key
	 * @return
	 */
	public static boolean containsKey(String cacheName, Object key) {
		Map<Object, Object> cache = getCacheByName(cacheName);
		return cache.containsKey(key);
	}

	/**
	 * Retrieve all values from a cache.
	 * 
	 * @param cacheName
	 * @return
	 */
	public static <K, V> List<V> retrieveAllCacheValues(String cacheName) {
		IMap<K, V> cache = getCacheByName(cacheName);

		return new ArrayList<V>(cache.values());
	}

	/**
	 * Retrieve all keys from a cache.
	 * 
	 * @param cacheName
	 * @return
	 */
	public static <K, V> List<K> retrieveAllCacheKeys(String cacheName) {
		IMap<K, V> cache = getCacheByName(cacheName);

		return new ArrayList<K>(cache.keySet());
	}

	/**
	 * Retrieve cache by cache name
	 * 
	 * @param cacheName
	 * @return
	 */
	public static final <K, V> IMap<K, V> getCacheByName(String cacheName) {
		HazelcastInstance hzInstance = HazelcastInstanceHelper.getHazelcastInstance();
		if (hzInstance == null) {
			throw new NoHazelcastInstanceAvailableException();
		}
		return hzInstance.getMap(cacheName);
	}

	/**
	 * Count the number of items in the cache.
	 * 
	 * @param cacheName
	 * @return
	 */
	public static int countCacheEntries(String cacheName) {
		return getCacheByName(cacheName).size();
	}

	/**
	 * Check is cache with the given name exist.
	 * 
	 * @param cacheName
	 * @return
	 */
	public static boolean isCacheExist(String cacheName) {
		HazelcastInstance hzInstance = HazelcastInstanceHelper.getHazelcastInstance();
		return hzInstance.getConfig().getMapConfigs().containsKey(cacheName);
	}

	/**
	 * Query data in cache by searching on the values.
	 * 
	 * @param cacheName
	 * @param predicate
	 * @return
	 */
    public static <K, V> Set<V> queryCache(String cacheName, Predicate<K, V> predicate) {
		final IMap<K, V> cache = getCacheByName(cacheName);
		return (Set<V>) cache.values(predicate);
	}

	/**
	 * Query data in cache by searching on the key.
	 * 
	 * @param cacheName
	 * @param predicate
	 * @return
	 */
	public static <K, V> Set<K> queryCacheKey(String cacheName, Predicate<K, V> predicate) {
		final IMap<K, V> cache = getCacheByName(cacheName);
		return (Set<K>) cache.keySet(predicate);
	}
}
