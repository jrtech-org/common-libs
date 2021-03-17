package org.jrtech.common.xsutils;

import java.util.concurrent.TimeUnit;

import org.junit.Test;

import com.google.common.cache.LoadingCache;

public class TestCaching {

	private static LoadingCache<String, String> cache = null;

	@Test
	public void simpleStorage() {
		getCache().invalidateAll();
		
		System.out.println("Simple Storage");
		getCache().put("001", "John");
		getCache().put("002", "Susan");
		getCache().put("003", "Brian");

		String[] checkKeys = new String[] { "001", "002", "003", "004" };

		checkValues(checkKeys);

		System.out.println("Cache size: " + getCache().size());
	}

	@Test
	public void checkEviction() throws InterruptedException {
		getCache().invalidateAll();
		int sleepTime = 100;
		System.out.println("Check Eviction");
		getCache().put("005", "Jonson");
		Thread.sleep(sleepTime);
		getCache().put("006", "Brenda");
		Thread.sleep(sleepTime);
		getCache().put("007", "Amy");

		String[] checkKeys = new String[] { "005", "004", "007", "006", "002" };

		checkValues(checkKeys);
	}
	
	@Test
	public void checkingKeyExistence() throws InterruptedException {
		getCache().invalidateAll();
		int sleepTime = 100;
		System.out.println("Check Eviction");
		getCache().put("005", "Jonson");
		Thread.sleep(sleepTime);
		getCache().put("006", "Brenda");
		Thread.sleep(sleepTime);
		getCache().put("007", "Amy");

		String[] checkKeys = new String[] { "005", "004", "007", "006", "002" };

		checkValues(checkKeys);
	}


	private void checkValues(String[] checkKeys) {
		System.out.println("Cache size: " + getCache().size());

		for (String key : checkKeys) {
			try {
				String value = getCache().getIfPresent(key);
				
				System.out.println(key + " -> '" + value + "'");
			} catch (Exception e) {
				System.err.println("[ERROR]: " + e.getMessage());
			}
			
		}
	}

	private static LoadingCache<String, String> getCache() {
		if (cache == null) {
			cache = CacheUtil.createCache(100, 300, TimeUnit.MILLISECONDS);
		}

		return cache;
	}

}
