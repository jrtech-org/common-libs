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
package org.jrtech.common.preferences;

public class SystemPreference extends Preference {

	private static final long serialVersionUID = 7517906043928061576L;
	
    private String preferenceValue;

	public SystemPreference() {
		super();
		super.setType(getType());
	}

	public void setPreferenceValue(String preferenceValue) {
		this.preferenceValue = preferenceValue;
	}

	public String getPreferenceValue() {
		return preferenceValue;
	}

	@Override
	public String getType() {
		return getClass().getSimpleName();
	}

	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();

		sb.append(super.toString());
		sb.append("value: ").append(preferenceValue).append("\n");

		return sb.toString();
	}

	@Override
	protected PreferenceLoader getPreferenceLoader() {
	    return new SystemPreferenceLoader();
	}
	
	@Override
	protected Preference copyTo(Preference preference) throws IncompatiblePreferenceTypeException {
		SystemPreference concretePreference = (SystemPreference) super.copyTo(preference);
	    concretePreference.preferenceValue = preferenceValue;
	    
	    return concretePreference;
	}
}
