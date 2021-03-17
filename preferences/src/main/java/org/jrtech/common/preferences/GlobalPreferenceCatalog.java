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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class GlobalPreferenceCatalog {

	private static String applicationName = null;

	private static ConcurrentMap<String, ConcurrentMap<String, Preference>> preferenceCatalog = new ConcurrentHashMap<String, ConcurrentMap<String, Preference>>();

	public static <T extends Preference> List<T> filterPreferenceListByModule(List<T> prefList, String module) {
		List<T> bestMatchList = new CopyOnWriteArrayList<T>();
		List<T> patternMatchList = new CopyOnWriteArrayList<T>();
		List<T> normalMatchList = new CopyOnWriteArrayList<T>();
		List<T> lowMatchList = new CopyOnWriteArrayList<T>();

		for (T preference : prefList) {
			if ("*".equals(preference.getModule())) {
				normalMatchList.add(preference);
			} else if ("".equals(preference.getModule())) {
				lowMatchList.add(preference);
			} else if (module != null && module.matches(preference.getModule())) {
				patternMatchList.add(preference);
			} else if (module != null && module.equals(preference.getModule())) {
				bestMatchList.add(preference);
			}
		}
		
		bestMatchList.addAll(patternMatchList);
		bestMatchList.addAll(normalMatchList);
		bestMatchList.addAll(lowMatchList);

		return bestMatchList;
	}

	public static <T extends Preference> List<T> filterPreferenceListByModuleEntity(List<T> prefList, String module,
	        String entity) {
		List<T> preFilteredPrefList = filterPreferenceListByModule(prefList, module);

		List<T> bestMatchList = new CopyOnWriteArrayList<T>();
		List<T> patternMatchList = new CopyOnWriteArrayList<T>();
		List<T> normalMatchList = new CopyOnWriteArrayList<T>();
		List<T> lowMatchList = new CopyOnWriteArrayList<T>();

		for (T preference : preFilteredPrefList) {
			if ("*".equals(preference.getEntity())) {
				normalMatchList.add(preference);
			} else if ("".equals(preference.getEntity())) {
				lowMatchList.add(preference);
			} else if (entity != null && entity.matches(preference.getEntity())) {
				patternMatchList.add(preference);
			} else if (entity != null && entity.equals(preference.getEntity())) {
				bestMatchList.add(preference);
			}
		}
		
		bestMatchList.addAll(patternMatchList);
		bestMatchList.addAll(normalMatchList);
		bestMatchList.addAll(lowMatchList);

		return bestMatchList;
	}

	public static String getApplicationName() {
		return applicationName == null ? "": applicationName;
	}

	@SuppressWarnings("unchecked")
    private static <T extends Preference> T getGlobalPreference(ConcurrentMap<String, Preference> typeCatalog,
	        String module, String entity, String name) {
		List<T> filteredList = filterPreferenceListByModuleEntity(new ArrayList<T>((Collection<? extends T>) typeCatalog.values()), module, entity);

		for (T preference : filteredList) {
			if (preference.getName().equals(name)) {
				return (T) preference.clone();
			}
		}

		return null;
	}

	@SuppressWarnings("unchecked")
    public static <T extends Preference> List<T> retrieveGlobalPreference() {
		List<T> list = new CopyOnWriteArrayList<T>();
		
		Collection<ConcurrentMap<String, Preference>> typeCatalogSet = preferenceCatalog.values();
		for (ConcurrentMap<String, Preference> typeCatalog : typeCatalogSet) {
	        list.addAll((Collection<? extends T>) typeCatalog.values());
        }

		return list;
	}

    public static <T extends Preference> List<T> retrieveGlobalPreference(Class<T> preferenceType) {
		return retrieveGlobalPreference(preferenceType.getSimpleName());
	}

    public static <T extends Preference> List<T> retrieveGlobalPreference(Class<T> preferenceType, String module) {
		return retrieveGlobalPreference(preferenceType.getSimpleName(), module);
	}

    public static <T extends Preference> List<T> retrieveGlobalPreference(Class<T> preferenceType, String module,
	        String entity) {
		return retrieveGlobalPreference(preferenceType.getSimpleName(), module, entity);
	}

	public static <T extends Preference> T retrieveGlobalPreference(Class<T> preferenceType, String module,
	        String entity, String name) {
		return retrieveGlobalPreference(preferenceType.getSimpleName(), module, entity, name);
	}

	@SuppressWarnings("unchecked")
    public static <T extends Preference> List<T> retrieveGlobalPreference(String preferenceType) {
		ConcurrentMap<String, Preference> typeCatalog = preferenceCatalog.get(preferenceType);

		if (typeCatalog == null) {
			typeCatalog = new ConcurrentHashMap<String, Preference>();
			preferenceCatalog.put(preferenceType, typeCatalog);
		}

		return new CopyOnWriteArrayList<T>((Collection<? extends T>) typeCatalog.values());
	}
	
    @SuppressWarnings("unchecked")
    public static <T extends Preference> List<T> retrieveGlobalPreference(String preferenceType, String module) {
		ConcurrentMap<String, Preference> typeCatalog = preferenceCatalog.get(preferenceType);

		if (typeCatalog == null) {
			typeCatalog = new ConcurrentHashMap<String, Preference>();
			preferenceCatalog.put(preferenceType, typeCatalog);
		}

		return filterPreferenceListByModule(new ArrayList<T>((Collection<? extends T>) typeCatalog.values()), module);
	}

    @SuppressWarnings("unchecked")
    public static <T extends Preference> List<T> retrieveGlobalPreference(String preferenceType, String module,
	        String entity) {
		ConcurrentMap<String, Preference> typeCatalog = preferenceCatalog.get(preferenceType);

		if (typeCatalog == null) {
			typeCatalog = new ConcurrentHashMap<String, Preference>();
			preferenceCatalog.put(preferenceType, typeCatalog);
		}

		return filterPreferenceListByModuleEntity(new ArrayList<T>((Collection<? extends T>) typeCatalog.values()), module, entity);
	}

    public static <T extends Preference> T retrieveGlobalPreference(String preferenceType, String module,
	        String entity, String name) {
		ConcurrentMap<String, Preference> typeCatalog = preferenceCatalog.get(preferenceType);

		if (typeCatalog == null) {
			typeCatalog = new ConcurrentHashMap<String, Preference>();
			preferenceCatalog.put(preferenceType, typeCatalog);
		}

		return getGlobalPreference(typeCatalog, module, entity, name);
	}

	public static String retrieveGlobalSystemPreferenceValue(String module, String entity, String name) {
		SystemPreference sysPref = retrieveGlobalPreference(SystemPreference.class, module, entity, name);

		if (sysPref == null)
			return null;

		return sysPref.getPreferenceValue();
	}

	public synchronized static void setApplicationName(String applicationName) {
		GlobalPreferenceCatalog.applicationName = applicationName;
	}

	public static <T extends Preference> void setGlobalPreference(Class<? extends Preference> preferenceType,
	        T globalPreference) {
		ConcurrentMap<String, Preference> typeCatalog = preferenceCatalog.get(preferenceType.getSimpleName());

		if (typeCatalog == null) {
			typeCatalog = new ConcurrentHashMap<String, Preference>();
			preferenceCatalog.put(preferenceType.getSimpleName(), typeCatalog);
		}

		typeCatalog.put(globalPreference.getLogicalUniqueIdentification(), globalPreference);
	}

}
