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


public class SimpleValidationMessage implements ValidationMessage {

	private static final long serialVersionUID = 3517277727737589390L;

    private String key;

	private String defaultDescription;

	private int parameterCount;

	public SimpleValidationMessage(String key, String defaultDescription, int parameterCount) {
		super();
		this.key = key;
		this.defaultDescription = defaultDescription;
		this.parameterCount = parameterCount;
	}

	public String getMessageKey() {
		return key;
	}

	@Override
	public String getDefaultDescription() {
		return defaultDescription;
	}

	@Override
	public int getParameterCount() {
		return parameterCount;
	}

	@Override
    public int hashCode() {
	    final int prime = 31;
	    int result = 1;
	    result = prime * result + ((defaultDescription == null) ? 0 : defaultDescription.hashCode());
	    result = prime * result + ((key == null) ? 0 : key.hashCode());
	    result = prime * result + parameterCount;
	    return result;
    }

	@Override
    public boolean equals(Object obj) {
	    if (this == obj)
		    return true;
	    if (obj == null)
		    return false;
	    if (getClass() != obj.getClass())
		    return false;
	    SimpleValidationMessage other = (SimpleValidationMessage) obj;
	    if (defaultDescription == null) {
		    if (other.defaultDescription != null)
			    return false;
	    } else if (!defaultDescription.equals(other.defaultDescription))
		    return false;
	    if (key == null) {
		    if (other.key != null)
			    return false;
	    } else if (!key.equals(other.key))
		    return false;
	    if (parameterCount != other.parameterCount)
		    return false;
	    return true;
    }
}
