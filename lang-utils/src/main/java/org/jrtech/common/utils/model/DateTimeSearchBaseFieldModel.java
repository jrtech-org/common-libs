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
