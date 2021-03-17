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
package org.jrtech.common.utils.query;


import java.io.Serializable;

public class AttributeOrder implements Serializable {

    private static final long serialVersionUID = -4310472108839201597L;

    private String columnName;
	
	private SortOrder sortOrder;

	public AttributeOrder(String columnName) {
		this(columnName, SortOrder.ASCENDING);
	}

	public AttributeOrder(String columnName, SortOrder sortOrder) {
		this.columnName = columnName;
		this.sortOrder = sortOrder;
	}

	public String getColumnName() {
		return columnName;
	}

	public void setColumnName(String columnName) {
		this.columnName = columnName;
	}

	public SortOrder getSortOrder() {
		return sortOrder;
	}

	public void setSortOrder(SortOrder sortOrder) {
		this.sortOrder = sortOrder;
	}

	public enum SortOrder {
		ASCENDING("asc"), 
		DESCENDING("des");

		private final String value;

		SortOrder(String value) {
			this.value = value;
		}

		SortOrder() {
			this("asc");
		}

		public static SortOrder fromCode(String value) {
			return fromCode(value, null);
		}

		public static SortOrder fromCode(String value, SortOrder defaultValue) {
			if (value.equalsIgnoreCase("ASC"))
				return ASCENDING;
			if (value.equalsIgnoreCase("DES"))
				return DESCENDING;

			return defaultValue;
		}

		public static SortOrder fromString(String value) {
			return fromString(value, null);
		}

		public static SortOrder fromString(String value, SortOrder defaultValue) {
			if (value.equalsIgnoreCase("Ascending"))
				return ASCENDING;
			if (value.equalsIgnoreCase("Descending"))
				return DESCENDING;

			return defaultValue;
		}

		public String toCode() {
			return value;
		}

		@Override
		public String toString() {
			if (0 == value.compareToIgnoreCase("DES"))
				return "Descending";

			return "Ascending";
		}
	}
	}
