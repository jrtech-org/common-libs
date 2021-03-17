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

import java.util.Map;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import com.hazelcast.core.HazelcastInstance;

public class TestHazelcastInstanceFactory {
	
	@BeforeClass
	public static void initClass() {
		System.setProperty(HazelcastInstanceHelper.HAZELCAST_MODE, HazelcastInstanceHelper.MODE.TEST.name());
	}
	
	@Test
	public void storeMap() {
		HazelcastInstance hzInstance = HazelcastInstanceHelper.getHazelcastInstance();
		Assert.assertNotNull(hzInstance);
		Map<Object, Object> cache = hzInstance.getMap("TETS_MAP");
		cache.put(new Integer(1), "John Doe");
		cache.put(new Integer(2), "Jacky Doe");
		cache.put(new Integer(3), "Jane Doe");
	}

	@Test
	public void showMap() {
		HazelcastInstance hzInstance = HazelcastInstanceHelper.getHazelcastInstance();
		Assert.assertNotNull(hzInstance);
		Map<Object, Object> cache = hzInstance.getMap("TETS_MAP");
		System.out.println("Cache size: " + cache.size());
	}

	@AfterClass
	public static void destroyClass() {
		HazelcastInstanceHelper.getHazelcastInstance().shutdown();
	}
}
