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

public enum ValidityScope {
	GLOBAL(0), 
	ORGANIZATION_UNIT(1), 
	USER(2);

	private final int value;

	ValidityScope(int value) {
		this.value = value;
	}

	public static ValidityScope fromInt(int value) {
		if (value == 0)
			return GLOBAL;
		if (value == 1)
			return ORGANIZATION_UNIT;
		if (value == 2)
			return USER;

		return null;
	}

	public static ValidityScope fromString(String value) {
		if (value.equalsIgnoreCase("Global"))
			return GLOBAL;
		if (value.equalsIgnoreCase("OrganizationUnit"))
			return ORGANIZATION_UNIT;
		if (value.equalsIgnoreCase("User"))
			return USER;

		return null;
	}

	public int toInt() {
		return value;
	}

	@Override
	public String toString() {
		if (value == 0)
			return "Global";
		if (value == 1)
			return "OrganizationUnit";
		if (value == 2)
			return "User";

		return "";
	}
}
