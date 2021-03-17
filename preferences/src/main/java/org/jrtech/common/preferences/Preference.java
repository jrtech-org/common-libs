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

import java.io.Serializable;
import java.util.Map.Entry;
import java.util.Properties;

import org.jrtech.common.xmlutils.XmlUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class Preference implements Serializable, Comparable<Preference>, Cloneable {
	
	private static final long serialVersionUID = 6531531532788710315L;

    private static Logger log = LoggerFactory.getLogger(Preference.class);

	private String id;
	private String name;
	private String type;
	private String module;
	private String entity;
	private String definitionValue;
	private ValidityScope scope;

	public Preference() {
		super();
		scope = ValidityScope.USER;
		extendedProperties = new Properties();
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getType() {
		return type;
	}

	public String getModule() {
		return module;
	}

	public void setModule(String module) {
		this.module = module;
	}

	public String getEntity() {
		return entity;
	}

	public void setEntity(String entity) {
		this.entity = entity;
	}

	public String getDefinitionValue() {
		return definitionValue;
	}

	public void setDefinitionValue(String definitionValue) {
		this.definitionValue = definitionValue;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getId() {
		return id;
	}

	public ValidityScope getScope() {
		return scope;
	}

	public void setScope(ValidityScope scope) {
		this.scope = scope;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int totalHashCode = 1;

		totalHashCode = prime * totalHashCode + ((name == null) ? 0 : name.hashCode());
		totalHashCode = prime * totalHashCode + ((type == null) ? 0 : type.hashCode());
		totalHashCode = prime * totalHashCode + ((module == null) ? 0 : module.hashCode());
		totalHashCode = prime * totalHashCode + ((entity == null) ? 0 : entity.hashCode());
		totalHashCode = prime * totalHashCode + ((definitionValue == null) ? 0 : definitionValue.hashCode());
		totalHashCode = prime * totalHashCode + ((scope == null) ? 0 : scope.hashCode());

		return totalHashCode;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;

		if (!(obj instanceof Preference))
			return false;

		final Preference other = (Preference) obj;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name)) {
			return false;
		}
		if (getType() == null) {
			if (other.getType() != null)
				return false;
		} else if (!getType().equals(other.getType())) {
			return false;
		}
		if (module == null) {
			if (other.module != null)
				return false;
		} else if (!module.equals(other.module)) {
			return false;
		}
		if (entity == null) {
			if (other.entity != null)
				return false;
		} else if (!entity.equals(other.entity)) {
			return false;
		}
		if (definitionValue == null) {
			if (other.definitionValue != null)
				return false;
		} else if (!definitionValue.equals(other.definitionValue)) {
			return false;
		}
		if (scope == null) {
			if (other.scope != null)
				return false;
		} else if (!scope.equals(other.scope)) {
			return false;
		}

		return true;
	}

	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();

		sb.append("name: ").append(name).append("\n");
		sb.append("type: ").append(getType()).append("\n");
		sb.append("module: ").append(module).append("\n");
		sb.append("entity: ").append(entity).append("\n");
		sb.append("definitionValue: ").append(definitionValue).append("\n");
		sb.append("scope: ").append(scope).append("\n");

		return sb.toString();
	}

	private Properties extendedProperties;
	
	public boolean hasExtendedProperty(String propertyName) {
		return extendedProperties.keySet().contains(propertyName);
	}
	
	public String getExtendedPropertyValue(String propertyName) {
		return extendedProperties.getProperty(propertyName);
	}
	
	public void setExtendedPropertyValue(String propertyName, String value) {
		extendedProperties.put(propertyName, value);
	}
	
	public Properties getExtendedProperties() {
		return extendedProperties;
	}

	public void setExtendedProperties(Properties extendedProperties) {
		this.extendedProperties = extendedProperties;
	}

	@Override
	public int compareTo(Preference other) {
		int compareValue = 0;
		if (other.scope != null && scope != null) {
			compareValue = scope.compareTo(other.scope) * 1000;
		} else if (scope != null) {
			compareValue = 1000;
		} else {
			compareValue = -1000;
		}

		if (other.getType() != null && getType() != null) {
			compareValue = compareValue + getType().compareTo(other.getType()) * 10;
		} else if (getType() != null) {
			compareValue = compareValue + 10;
		}
		
		if (other.name != null && name != null) {
			compareValue = compareValue + name.compareTo(other.name);
		} else if (name != null) {
			compareValue = compareValue + 1;
		}
		return compareValue;
	}

	public String getLogicalUniqueIdentification() {
		return scope.name() + "/" + type + "/" + module + (module == null || module.equals("") ? ""
		        : ".") + entity + (entity == null || entity.equals("") ? "" : ".") + name;
	}

	public void synchronizeObjectToDefinitionValue() {
		PreferenceLoader loader = getPreferenceLoader();
		Document xmlDoc;
        try {
	        xmlDoc = loader.export(this);
	        Element xmlRootElement = (Element) XmlUtils.getNodeByXPath(xmlDoc, "//" + PreferenceLoader.TAG_PREFERENCE);
	        this.definitionValue = XmlUtils.nodeToString(xmlRootElement);
        } catch (Exception e) {
        	// Shall not happen
	        log.error("Fail to export preference: " + toString(), e);
        }
	}

	protected PreferenceLoader getPreferenceLoader() {
	    return new PreferenceLoader();
    }
	
	protected Preference copyTo(Preference preference) throws IncompatiblePreferenceTypeException {
    	if (preference == null) throw new IncompatiblePreferenceTypeException(getType(), null);
    	
    	if (!getType().equals(preference.getType())) throw new IncompatiblePreferenceTypeException(getType(), preference.getType());
    	
    	if (!(getType().equals(preference.getType()))) throw new IncompatiblePreferenceTypeException(this, preference);

		preference.setDefinitionValue(definitionValue);
		preference.setEntity(entity);
		if (extendedProperties != null)
			for (Entry<?, ?> entry : extendedProperties.entrySet()) {
				preference.setExtendedPropertyValue((String) entry.getKey(), (String) entry.getValue());
			}
		preference.setId(id);
		preference.setModule(module);
		preference.setName(name);
		preference.setScope(scope);
		preference.setType(type);
		
	    return preference;
	}
	
	@Override
	public Object clone() {
		Preference cloneObject = null;
        try {
	        cloneObject = (Preference) super.clone();
        } catch (CloneNotSupportedException e) {
	        try {
	            cloneObject = getClass().newInstance();
            } catch (Exception e1) {
            	log.error("Fail to clone preference: " + toString(), e);
            }
        }
	    try {
	        return copyTo(cloneObject);
        } catch (IncompatiblePreferenceTypeException e) {
	        return null;
        }
	}

	// CUSTOM-CODE End: Custom method(s) for Preference
}
