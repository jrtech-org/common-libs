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
package org.jrtech.common.utils.validation;


import java.io.Serializable;
import java.util.ArrayList;

import org.jrtech.common.utils.translation.ITranslatable;

public enum SeverityLevel implements ITranslatable, Serializable {

	// @formatter:off
	INFO ("Information", "general.validation.severityLevel.info", 3), 
	WARNING ("Warning", "general.validation.severityLevel.warning", 5), 
	ERROR("Error", "general.validation.severityLevel.error", 7), 
	FATAL("Fatal", "general.validation.severityLevel.fatal", 10);
	// @formatter:on

	private final String defaultLabel;

	private final String labelKey;

	private final int code;

	private SeverityLevel(String defaultLabel, String labelKey, int code) {
		this.defaultLabel = defaultLabel;
		this.labelKey = labelKey;
		this.code = code;
	}

	@Override
	public String getLabel() {
		return defaultLabel;
	}

	@Override
	public String getLabelProperty() {
		return labelKey;
	}

	@Override
	public String getName() {
		return name();
	}

	public int getCode() {
		return code;
	}

	public static SeverityLevel fromLabelKey(String labelKey) {
		if (labelKey == null)
			throw new IllegalArgumentException("Label key is null");

		for (SeverityLevel sl : values()) {
			if (sl.labelKey.equals(labelKey)) {
				return sl;
			}
		}
		
		return null;
	}

	public static SeverityLevel fromCode(int code) {
		for (SeverityLevel sl : values()) {
			if (sl.code == code) {
				return sl;
			}
		}
		
		return null;
	}

	//@formatter:off
	/**
	 * @param severity
	 * @param operatorValue byte value of the operator that can be one of the following:<br>
	 * <li> -2 less than<br>
	 * <li> -1 less than or equal<br>
	 * <li>  0 equal<br>
	 * <li>  1 higher than or equal<br>
	 * <li>  2 higher than<br>
	 * @return
	 */
	// @formatter:on
	public static SeverityLevel[] getSeveritiesFor(SeverityLevel severity, byte operatorValue) {
		ArrayList<SeverityLevel> severities = new ArrayList<SeverityLevel>();
		for (SeverityLevel sl : SeverityLevel.values()) {
			if (operatorValue < -1 && sl.code < severity.code) {
				severities.add(sl);
			} else if (operatorValue == -1 && sl.code <= severity.code) {
				severities.add(sl);
			} else if (operatorValue == 0 && sl.code == severity.code) {
				severities.add(sl);
			} else if (operatorValue == 1 && sl.code >= severity.code) {
				severities.add(sl);
			} else if (operatorValue > 1 && sl.code > severity.code) {
				severities.add(sl);
			}
		}

		return severities.toArray(new SeverityLevel[severities.size()]);
	}

}
