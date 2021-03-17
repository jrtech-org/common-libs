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

import org.w3c.dom.Element;

public class SystemPreferenceLoader extends AbstractPreferenceLoaderAndConverter<SystemPreference> {

	public static final String ATTRIBUTE_VALUE = "value";

	static {
		registerConcretePreferenceLoader(SystemPreference.class.getSimpleName(), SystemPreferenceLoader.class);
	}
	
	@Override
	protected String getType() {
		return SystemPreference.class.getSimpleName();
	}
	
	@Override
    protected SystemPreference newInstance() {
	    return new SystemPreference();
    }

	@Override
    protected void copySpecificAttributeFromXml(Element xmlPreferenceNode, SystemPreference preference) {
		preference.setPreferenceValue(xmlPreferenceNode.getAttribute(ATTRIBUTE_VALUE));
    }

	@Override
    protected void copySpecificAttributeToXml(SystemPreference preference, Element xmlPreferenceNode) {
		xmlPreferenceNode.setAttribute(ATTRIBUTE_VALUE, ((SystemPreference) preference).getPreferenceValue());
    }

}
