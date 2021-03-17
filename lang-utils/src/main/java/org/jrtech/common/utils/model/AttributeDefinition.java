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

import org.jrtech.common.utils.exception.InvalidDataTypeException;

public class AttributeDefinition<T> implements Serializable {

	private static final long serialVersionUID = -6466252508884366580L;
    private String name;
	private Class<T> dataType;
	private T value;

	public AttributeDefinition(String name, Class<T> dataType) {
		this(name, dataType, null);
	}

    public AttributeDefinition(String name, Class<T> dataType, T value) {
		super();
		this.name = name;
		this.dataType = dataType;
		this.value = value;
	}

	public String getName() {
		return name;
	}

	public Class<?> getDataType() {
		return dataType;
	}

	public T getValue() {
		return value;
	}

	public void setValue(T value) throws InvalidDataTypeException {
		this.value = value;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int totalHashCode = 1;

		totalHashCode = prime * totalHashCode + ((name == null) ? 0 : name.hashCode());
		totalHashCode = prime * totalHashCode + ((dataType == null) ? 0 : dataType.hashCode());
		totalHashCode = prime * totalHashCode + ((value == null) ? 0 : value.hashCode());

		return totalHashCode;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;

		if (!(obj instanceof AttributeDefinition))
			return false;

		final AttributeDefinition<?> other = (AttributeDefinition<?>) obj;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name)) {
			return false;
		}
		if (dataType == null) {
			if (other.dataType != null)
				return false;
		} else if (!dataType.equals(other.dataType)) {
			return false;
		}
		if (value == null) {
			if (other.value != null)
				return false;
		} else if (!value.equals(other.value)) {
			return false;
		}

		return true;
	}

	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();

		sb.append("name: ").append(name).append("\n");
		sb.append("dataType: ").append(dataType).append("\n");
		sb.append("value: ").append(value).append("\n");

		return sb.toString();
	}

}
