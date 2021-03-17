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
package org.jrtech.common.utils.operator;


import java.io.Serializable;

import org.jrtech.common.utils.translation.ITranslatable;

public enum DatetimeOperator implements ITranslatable, Serializable {
	AFTER("After", ComparisonOperator.GT,"dateTimeOperator.after"), 
	AFTER_EQUAL("On or After", ComparisonOperator.GE, "dateTimeOperator.afterequal"), 
	BEFORE("Before", ComparisonOperator.LT,"dateTimeOperator.before"), 
	BEFORE_EQUAL("On or Before", ComparisonOperator.LE, "dateTimeOperator.beforeequal"), 
	RANGE("On or Between", null, "dateTimeOperator.range"), // Range is handled as >= ? and <= ?
	EQUAL("On", ComparisonOperator.EQ, "dateTimeOperator.equal"); // FMPPRDT-2243 date equal to search
	
	private final String name;
	private ComparisonOperator value;
	private final String labelProperty;

	DatetimeOperator(String name, ComparisonOperator value, String labelProperty) {
		this.name = name;
		this.value = value;
		this.labelProperty = labelProperty;
	}

	@Override
	public String getLabel() {
		return name;
	}

	@Override
	public String getLabelProperty() {
		return labelProperty;
	}

	@Override
	public String getName() {
		return name();
	}
	
	public ComparisonOperator getValue(){
		return value;
	}
	
	public static DatetimeOperator fromValue(ComparisonOperator co) {
		for (DatetimeOperator item : values()) {
			if (item.getValue().equals(co)) return item;
		}
		return null;
	}
}
