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

import java.util.List;

import org.jrtech.common.preferences.AbstractPreferenceLoaderAndConverter;
import org.jrtech.common.preferences.Preference;
import org.jrtech.common.xmlutils.XmlUtils;
import org.w3c.dom.Element;

public class ObjectAttributePreferenceLoader extends AbstractPreferenceLoaderAndConverter<ObjectAttributePreference> {

	public static final String TAG_OBJECT_ATTRIBUTE = "objectAttribute";

	public static final String ATTRIBUTE_LABEL = "label";
	public static final String ATTRIBUTE_RETRIEVING_METHOD = "retrievingMethod";
	public static final String ATTRIBUTE_SELENIUM = "selenium";
	public static final String ATTRIBUTE_TRANSLATOR = "translator";

	static {
		registerConcretePreferenceLoader(ObjectAttributePreference.class.getSimpleName(), ObjectAttributePreferenceLoader.class);
	}
	
	@Override
	protected String getType() {
		return ObjectAttributePreference.class.getSimpleName();
	}

	@Override
	protected ObjectAttributePreference newInstance() {
	    return new ObjectAttributePreference();
	}
	
	@Override
	protected void copySpecificAttributeFromXml(Element xmlPreferenceNode, ObjectAttributePreference preference) {
	    // no specific attribute
	}

	@Override
	protected void copySpecificAttributeToXml(ObjectAttributePreference preference, Element xmlPreferenceNode) {
	    // no specific attribute
	}
	
	@Override
	protected Element xmlFromPreference(Element xmlParentNode, Preference preference) throws Exception {
		Element xmlPreferenceNode = super.xmlFromPreference(xmlParentNode, preference);

		for (ObjectAttributeConfiguration attrConfig : ((ObjectAttributePreference) preference).getAttributeConfigList()) {
	        Element xmlAttrConfigElement = XmlUtils.createSubElement(xmlPreferenceNode, TAG_OBJECT_ATTRIBUTE);
	        xmlAttrConfigElement.setAttribute(ATTRIBUTE_LABEL, attrConfig.getLabel());
	        xmlAttrConfigElement.setAttribute(ATTRIBUTE_NAME, attrConfig.getName());
	        xmlAttrConfigElement.setAttribute(ATTRIBUTE_RETRIEVING_METHOD, attrConfig.getRetrievingMethod());
	        xmlAttrConfigElement.setAttribute(ATTRIBUTE_SELENIUM, attrConfig.getSeleniumLabel());
	        xmlAttrConfigElement.setAttribute(ATTRIBUTE_TRANSLATOR, attrConfig.getTranslator());
        }

		return xmlPreferenceNode;
	}

	@Override
	protected ObjectAttributePreference xmlToPreference(Element xmlPreferenceNode) throws Exception {
		ObjectAttributePreference preference = super.xmlToPreference(xmlPreferenceNode);
		
		if (preference == null) return null;

		List<Element> xmlObjectAttributeElementList = XmlUtils.getChildElementListByTagName(xmlPreferenceNode,
		        TAG_OBJECT_ATTRIBUTE);
		for (Element xmlObjectAttributeElement : xmlObjectAttributeElementList) {
			ObjectAttributeConfiguration attrConfig = new ObjectAttributeConfiguration();

			attrConfig.setName(xmlObjectAttributeElement.getAttribute(ATTRIBUTE_NAME));
			String label = xmlObjectAttributeElement.getAttribute(ATTRIBUTE_LABEL);
			if (!label.equals("")) {
				attrConfig.setLabel(label);
			}

			attrConfig.setRetrievingMethod(xmlObjectAttributeElement.getAttribute(ATTRIBUTE_RETRIEVING_METHOD));

			attrConfig.setSeleniumLabel(xmlObjectAttributeElement.getAttribute(ATTRIBUTE_SELENIUM));
			attrConfig.setTranslator(xmlObjectAttributeElement.getAttribute(ATTRIBUTE_TRANSLATOR));

			preference.getAttributeConfigList().add(attrConfig);
		}

		return preference;
	}

}
