package org.jrtech.common.xsutils;

import java.util.concurrent.TimeUnit;

import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.cache.LoadingCache;


public class TestCaching {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(TestCaching.class);

	private static LoadingCache<String, String> cache = null;

	@Test
	public void simpleStorage() {
		getCache().invalidateAll();
		
		String[][] values = new String[][] { {"001", "John"}, {"002", "Susan"}, {"003", "Brian"} };
		
		LOGGER.info("Simple Storage");
		for (String[] value : values) {
			getCache().put(value[0], value[1]);
		}

		String[][] checkValues = new String[values.length + 1][2]; 
		System.arraycopy(values, 0, checkValues, 0, values.length);
		checkValues[checkValues.length - 1] = new String[] {"004", null};

		checkValues(checkValues);

		LOGGER.info("Cache size: " + getCache().size());
	}

	@Test
	public void checkEviction() throws InterruptedException {
		getCache().invalidateAll();		// Clear cache
		int sleepTime = 100;
		LOGGER.info("Check Eviction");
		
		String[][] values = new String[][] { {"005", "Jonson"}, {"006", "Brenda"}, {"007", "Amy"} };
		
		for (String[] value : values) {
			getCache().put(value[0], value[1]);
			Thread.sleep(sleepTime);
		}
		
		String[][] checkValues = new String[values.length + 2][2];
		System.arraycopy(values, 0, checkValues, 1, values.length);
		checkValues[0] = new String[] {"005", null}; // Cache entry has been evicted because of sleep over 3 x 100ms
		checkValues[1] = new String[] {"004", null};
		checkValues[checkValues.length - 1] = new String[] {"002", null};

		checkValues(checkValues);
	}
	
	@Test
	public void checkingKeyExistenceAfterEviction() throws InterruptedException {
		getCache().invalidateAll();
		int sleepTime = 100;
		LOGGER.info("Check Eviction");
		
		String[][] values = new String[][] { {"005", "Jonson"}, {"006", "Brenda"}, {"007", "Amy"} };
		
		for (String[] value : values) {
			getCache().put(value[0], value[1]);
			Thread.sleep(sleepTime);
		}
		
		String[][] checkValues = new String[values.length + 2][2];
		System.arraycopy(values, 0, checkValues, 1, values.length);
		checkValues[0] = new String[] {"005", null}; // Cache entry has been evicted because of sleep over 3 x 100ms
		checkValues[1] = new String[] {"004", null};
		checkValues[checkValues.length - 1] = new String[] {"002", null};

		for (String[] checkValueEntry : checkValues) {
			String checkKey = checkValueEntry[0];
			boolean expectedFlag = checkValueEntry[1] != null;
			boolean actualFlag = CacheUtil.hasKey(getCache(), checkKey);
			Assert.assertTrue("Existance of key: [" + checkKey + "] should be: ["+ expectedFlag +"], but ["+ actualFlag +"]", expectedFlag == actualFlag);
		}
	}

	private void checkValues(String[][] checkValues) {
		LOGGER.info("Cache size: " + getCache().size());

		for (String[] checkValueEntry : checkValues) {
			String checkKey = checkValueEntry[0];
			String expectedValue = checkValueEntry[1];
			try {
				String actualValue = getCache().getIfPresent(checkKey);
				Assert.assertEquals("Check value failure. [EXPECTED/ACTUAL]: [" + expectedValue + "/" + actualValue + "]", expectedValue, actualValue);
				LOGGER.info(checkKey + " -> '" + actualValue + "'");
			} catch (Exception e) {
				LOGGER.error("[ERROR]: " + e.getMessage());
				Assert.fail();
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
