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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SystemPreferenceUtil {

	private static Logger log = LoggerFactory.getLogger(SystemPreferenceUtil.class);

	private SystemPreferenceLoader loader = new SystemPreferenceLoader();

	public void storeUserSystemPreference(PreferenceService preferenceService, String module, String entity,
	        String[][] nameAndValueArray) throws Exception {
		for (int i = 0; i < nameAndValueArray.length; i++) {
			String name = nameAndValueArray[i][0];
			String stringValue = nameAndValueArray[i][1];
			Preference pref = preferenceService.retrieveUserPreference(SystemPreference.class.getSimpleName(),
			        module, entity, name);

			SystemPreference sysPref = null;
			if (pref == null) {
				sysPref = new SystemPreference();
				sysPref.setName(name);
				sysPref.setModule(module);
				sysPref.setEntity(entity);
				sysPref.setScope(ValidityScope.USER);
				sysPref.setPreferenceValue(stringValue);
				sysPref = loader.synchronizeDefinitionFromObject(sysPref);
				preferenceService.createUserPreference(sysPref);
			} else {
				if (pref instanceof SystemPreference) {
					sysPref = (SystemPreference) pref;
				} else {
					sysPref = loader.convert(pref);
				}
				if (!sysPref.getPreferenceValue().equalsIgnoreCase(stringValue)) {
					sysPref.setPreferenceValue(stringValue);
					sysPref = loader.synchronizeDefinitionFromObject(sysPref);
					preferenceService.modifyUserPreference(sysPref);
				}
			}
		}
	}

	public String retrieveUserSystemPreference(PreferenceService preferenceService, String module, String entity,
	        String preferenceName, boolean tryGlobal) {
		Preference pref = preferenceService.retrieveUserPreference(SystemPreference.class.getSimpleName(), module,
		        entity, preferenceName);

		if (pref == null) {
			if (tryGlobal) {
				// Try with global preference
				pref = GlobalPreferenceCatalog.retrieveGlobalPreference(
				        SystemPreference.class.getSimpleName(), module, entity, preferenceName);
			} else {
				return null;
			}
		}

		SystemPreference sysPref = null;
		if (pref != null) {
			if (pref instanceof SystemPreference) {
				sysPref = (SystemPreference) pref;
				return sysPref.getPreferenceValue();
			} else {
				try {
					sysPref = loader.convert(pref);
					return sysPref.getPreferenceValue();
				} catch (Exception e) {
					log.warn("Fail to convert system preference: " + pref.getDefinitionValue(), e);
				}
			}
		}

		return null;
	}
}
