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
