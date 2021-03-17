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
package org.jrtech.common.utils.model;


import java.io.Serializable;

public class DateTimeSearchBaseFieldModel implements Serializable {

	private static final long serialVersionUID = -4653321945723990297L;

    private String labelKey;
	
	private String defaultLabel;
	
	private String fieldName;
	
	private DateTimeSearchBaseFieldType fieldType;
	
	public DateTimeSearchBaseFieldModel(String labelKey, String defaultLabel, String fieldName, DateTimeSearchBaseFieldType fieldType) {
		this.labelKey = labelKey;
		this.defaultLabel = defaultLabel;
		this.fieldName = fieldName;
		this.fieldType = fieldType;
    }
	
	public String getLabelKey() {
	    return labelKey;
    }
	
	public String getDefaultLabel() {
	    return defaultLabel;
    }
	
	public String getFieldName() {
	    return fieldName;
    }
	
	public DateTimeSearchBaseFieldType getFieldType() {
	    return fieldType;
    }
	
	public enum DateTimeSearchBaseFieldType {
		DATE_AND_TIME,
		DATE_ONLY,
		TIME_ONLY
	}
}
