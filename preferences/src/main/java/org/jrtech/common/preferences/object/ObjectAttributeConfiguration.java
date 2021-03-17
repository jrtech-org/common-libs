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

import java.io.Serializable;

public class ObjectAttributeConfiguration implements Serializable {

    private static final long serialVersionUID = -3561921955205671075L;
    
    private String labelKey;
    private String defaultLabel;
    private String name;
    private String retrievingMethod;
    private String seleniumLabel;
    private String translator;
    private boolean inline;
    
    public ObjectAttributeConfiguration() {
    	super();
    }
    
    public ObjectAttributeConfiguration(String defaultLabel, String name, String retrievingMethod) {
    	this(defaultLabel, name, retrievingMethod, name, null);
    }
	
    public ObjectAttributeConfiguration(String labelKey, String defaultLabel, String name, String retrievingMethod) {
    	this(labelKey, defaultLabel, name, retrievingMethod, true);
    }
	
    public ObjectAttributeConfiguration(String labelKey, String defaultLabel, String name, String retrievingMethod, boolean inline) {
    	this(labelKey, defaultLabel, name, retrievingMethod, name, null, inline);
    }
	
    public ObjectAttributeConfiguration(String defaultLabel, String name, String retrievingMethod, String seleniumLabel, String translator) {
    	this(defaultLabel, defaultLabel, name, retrievingMethod, seleniumLabel, translator, true);
    }
    
    public ObjectAttributeConfiguration(String labelKey, String defaultLabel, String name, String retrievingMethod, String seleniumLabel, String translator) {
    	this(labelKey, defaultLabel, name, retrievingMethod, seleniumLabel, translator, true);
    }
    
    public ObjectAttributeConfiguration(String labelKey, String defaultLabel, String name, String retrievingMethod, String seleniumLabel, String translator, boolean inline) {
    	super();
    	this.labelKey = labelKey;
    	this.defaultLabel = defaultLabel;
    	this.name = name;
    	this.retrievingMethod = retrievingMethod;
    	this.seleniumLabel = seleniumLabel;
    	this.translator = translator;
    	this.inline = inline;
    }
	
    public String getLabel() {
        return getDefaultLabel();
    }
    
    public String getDefaultLabel() {
	    return defaultLabel;
    }
    
    public void setDefaultLabel(String defaultLabel) {
	    this.defaultLabel = defaultLabel;
    }
	
    public void setLabel(String label) {
    	setDefaultLabel(label);
    }
	
    public String getName() {
        return name;
    }
	
    public void setName(String name) {
        this.name = name;
        if (defaultLabel == null && name != null && name.length() > 1) {
        	defaultLabel = name.substring(0, 1).toUpperCase() + name.substring(1);
        } else if (defaultLabel == null) {
        	defaultLabel = "";
        }
    }
    
    public String getRetrievingMethod() {
        return retrievingMethod;
    }
	
    public void setRetrievingMethod(String retrievingMethod) {
        this.retrievingMethod = retrievingMethod;
    }
	
    public void setSeleniumLabel(String seleniumLabel) {
		this.seleniumLabel = seleniumLabel;
	}

	public String getSeleniumLabel() {
		return seleniumLabel;
	}

	public String getTranslator() {
		return translator;
	}

	public void setTranslator(String translator) {
		this.translator = translator;
	}
	
	public void setLabelKey(String labelKey) {
	    this.labelKey = labelKey;
    }
	
	public String getLabelKey() {
	    return labelKey;
    }
	
	public void setInline(boolean inline) {
	    this.inline = inline;
    }
	
	public boolean isInline() {
	    return inline;
    }

	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append(name).append(" (").append(translator).append(")");
		
	    return sb.toString();
	}
}
