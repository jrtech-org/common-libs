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
package org.jrtech.common.xsutils;

import java.util.concurrent.ConcurrentSkipListSet;

import org.jrtech.common.utils.validation.ValidationMessage;

/**
 * A Validation Message of Xml Processing.
 */
public class XmlValidationMessage implements ValidationMessage, Comparable<XmlValidationMessage> {
	
    private static final long serialVersionUID = 2556810309459048062L;
    
    private static final ConcurrentSkipListSet<XmlValidationMessage> INSTANCES = new ConcurrentSkipListSet<>();
	
    // @formatter:off
	// Errors on node
	public static final XmlValidationMessage ERROR_ATTRIBUTE_MANDATORY = new XmlValidationMessage("jrx.error.attribute.mandatory", "Attribute {0} from node {1} is mandatory but empty.", 2);
	public static final XmlValidationMessage ERROR_NODE_CHOICE_NO_SELECTION = new XmlValidationMessage("jrx.error.node.choiceNoSelection", "Choice node {0} from node {1} is mandatory but no selection is defined empty.", 2);
	public static final XmlValidationMessage ERROR_NODE_INVALID_CHILD = new XmlValidationMessage("jrx.error.node.invalidChild", "Node {0} is not a valid child for node {1}.", 2);
	public static final XmlValidationMessage ERROR_NODE_MANDATORY = new XmlValidationMessage("jrx.error.node.mandatory", "Node {0} is mandatory but empty.", 1);
	public static final XmlValidationMessage ERROR_NODE_OCCURANCE = new XmlValidationMessage("jrx.error.node.occurance", "Only {0} occurences of {1} allowed, but {2} found.", 3);
	
	// Errors on node value
	public static final XmlValidationMessage ERROR_INVALID_CONTENT_ALLOWED_VALUE = new XmlValidationMessage("jrx.error.invalidContent.allowedValue", "Invalid content ''{0}''. Allowed values are [{1}].", 2);
	public static final XmlValidationMessage ERROR_INVALID_CONTENT_PATTERN = new XmlValidationMessage("jrx.error.invalidContent.pattern", "Illegal value ''{0}'' found. Valid pattern expression for the field is {1}.", 2);
	public static final XmlValidationMessage ERROR_INVALID_CONTENT_MIN_LENGTH = new XmlValidationMessage("jrx.error.invalidContent.minLength", "Value ''{0}'' does not comply with constraint of minimum ''{1}'' character(s).", 2);
    public static final XmlValidationMessage ERROR_INVALID_CONTENT_MAX_LENGTH = new XmlValidationMessage("jrx.error.invalidContent.maxLength", "Value ''{0}'' does not comply with constraint of maximum ''{1}'' character(s).", 2);
    public static final XmlValidationMessage ERROR_INVALID_CONTENT_MIN_MAX_LENGTH = new XmlValidationMessage("jrx.error.invalidContent.minMaxLength", "Value ''{0}'' does not comply with constraint of minimum ''{1}'' and maximum ''{2}'' character(s).", 3);
	public static final XmlValidationMessage ERROR_INVALID_CONTENT_SMALL_VALUE = new XmlValidationMessage("jrx.error.invalidContent.smallValue", "Value should be greater than ''{0}''.", 1);
	public static final XmlValidationMessage ERROR_INVALID_CONTENT_DECIMAL_PLACE = new XmlValidationMessage("jrx.error.invalidContent.decimalPlace", "Only ''{0}'' decimal place(s) allowed.", 1);
	// @formatter:on

	private String key = null;

	private String defaultDescription = null;
	
	private int parameterCount = 0;

	private XmlValidationMessage(String key, String defaultDescription, int parameterCount) {
		this.key = key;
		this.defaultDescription = defaultDescription;
		this.parameterCount = parameterCount;
		
		INSTANCES.add(this);
	}

	public String getDefaultDescription() {
		return defaultDescription;
	}

	public static XmlValidationMessage fromKey(String key) {
		if (key == null)
			return null;

		for (XmlValidationMessage entity : INSTANCES) {
			if (entity.key.equals(key)) {
				return entity;
			}
		}
		return null;
	}

	@Override
    public String getMessageKey() {
	    return key;
    }

	@Override
    public int getParameterCount() {
	    return parameterCount;
    }

    @Override
    public int compareTo(XmlValidationMessage o) {
        if (key == null) {
            return "".compareTo(o == null ? "" : o.key);
        }
        return key.compareTo(o == null ? "" : o.key);
    }

}
