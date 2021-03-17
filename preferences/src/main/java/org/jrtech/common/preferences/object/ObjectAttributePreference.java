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
package org.jrtech.common.preferences.object;

import java.util.ArrayList;
import java.util.List;

import org.jrtech.common.preferences.IncompatiblePreferenceTypeException;
import org.jrtech.common.preferences.Preference;
import org.jrtech.common.preferences.PreferenceLoader;

public class ObjectAttributePreference extends Preference {

	private static final long serialVersionUID = -9171768732998891530L;
	
	private List<ObjectAttributeConfiguration> attributeConfigList;
	
	public ObjectAttributePreference() {
	    super();
	    attributeConfigList = new ArrayList<ObjectAttributeConfiguration>();
    }

	public List<ObjectAttributeConfiguration> getAttributeConfigList() {
	    return attributeConfigList;
    }

	public void setAttributeConfigList(List<ObjectAttributeConfiguration> attributeConfigList) {
	    this.attributeConfigList = attributeConfigList;
    }
	
	@Override
	protected PreferenceLoader getPreferenceLoader() {
	    return new ObjectAttributePreferenceLoader();
	}

	@Override
	protected Preference copyTo(Preference preference) throws IncompatiblePreferenceTypeException {
		ObjectAttributePreference concretePreference = (ObjectAttributePreference) super.copyTo(preference);
	    concretePreference.attributeConfigList = new ArrayList<ObjectAttributeConfiguration>(attributeConfigList);
	    
	    return concretePreference;
	}
}
