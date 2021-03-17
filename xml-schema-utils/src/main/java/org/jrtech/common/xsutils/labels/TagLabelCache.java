package org.jrtech.common.xsutils.labels;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

public class TagLabelCache {

	public static LoadingCache<String, String> createCache() {
		CacheLoader<String, String> loader;
		loader = new CacheLoader<String, String>() {
			@Override
			public String load(String key) {
				return key.toLowerCase();
			}
		};

		return CacheBuilder.newBuilder().maximumSize(10000).build(loader);
	}
}
