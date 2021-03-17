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
package org.jrtech.common.preferences;

import java.io.IOException;
import java.io.StringReader;
import java.util.Iterator;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.apache.commons.lang3.ClassUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PreferenceConverterFactory {

	public static final String CONVERTER_FACTORY_DEFINITION_FILE = "preferenceConverterDefinitionFile";

	private static Logger log = LoggerFactory.getLogger(PreferenceConverterFactory.class);

	private static ConcurrentMap<String, Class<PreferenceConverter<? extends Preference>>> converterMap = new ConcurrentHashMap<String, Class<PreferenceConverter<? extends Preference>>>();

	public static void init(String converterDefs) {
		Properties props = new Properties();
		try {
			props.load(new StringReader(converterDefs));
			init(props);
		} catch (IOException e) {
			log.error("Fail to initialize factory.", e);
		}
	}

	@SuppressWarnings("unchecked")
	public static void init(Properties converterDefs) {
		for (Iterator<String> it = converterDefs.stringPropertyNames().iterator(); it.hasNext();) {
		    String preferenceType = it.next();
			try {
				Class<? extends PreferenceConverter<? extends Preference>> prefConverterClass = (Class<? extends PreferenceConverter<? extends Preference>>) ClassUtils
				        .getClass(converterDefs.getProperty(preferenceType));
				converterMap.put(preferenceType, (Class<PreferenceConverter<? extends Preference>>) prefConverterClass);
			} catch (Exception e) {
				log.error("Fail to load converter definition for preference type: '" + preferenceType + "'", e);
			}
		}
	}

	public static PreferenceConverter<? extends Preference> getConverter(String preferenceType) {
		Class<? extends PreferenceConverter<? extends Preference>> prefConverterClass = converterMap.get(preferenceType);
		
		if (prefConverterClass == null) return null;
		
		try {
	        return prefConverterClass.newInstance();
        } catch (Exception e) {
        	log.error("Fail to get converter for preference type: '" + preferenceType + "'", e);
        }
		
		return null;
	}

}
