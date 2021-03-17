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
package org.jrtech.common.utils;



public class MethodPathParser {
	
    private static final String METHOD_NAME_EXP = "[a-zA-Z0-9_]+";
	
	public static final String METHOD_PATH_EXP = METHOD_NAME_EXP + "([/.]" + METHOD_NAME_EXP + ")*";
	
	public static MethodPathParser getInstance() {
		return new MethodPathParser();
	}

	public String getMethodName(String methodPathExpression) throws InvalidMethodPathExpression {
		String methodName = "";

		// remove left leading slash character
		while (methodPathExpression.startsWith("/") || methodPathExpression.startsWith(".")) {
			methodPathExpression = methodPathExpression.substring(1);
		}
		while (methodPathExpression.endsWith("/") || methodPathExpression.endsWith(".")) {
			methodPathExpression = methodPathExpression.substring(0, methodPathExpression.length() - 1);
		}

		if (methodPathExpression.matches(METHOD_PATH_EXP)) {
			int pos = methodPathExpression.indexOf('/');
			if (pos > 0) {
				methodName = methodPathExpression.substring(0, pos);
			} else {
			    pos = methodPathExpression.indexOf('.');
	            if (pos > 0) {
	                methodName = methodPathExpression.substring(0, pos);
	            } else {
	                methodName = methodPathExpression;
	            }
			}
		} else {
			throw new InvalidMethodPathExpression(methodPathExpression);
		}

		return methodName;
	}

	public String chopMethodNameFromMethodPathExpression(String methodPathExpression, String methodName) throws InvalidMethodPathExpression {
		// remove left leading slash character
		while (methodPathExpression.startsWith("/") || methodPathExpression.startsWith(".")) {
			methodPathExpression = methodPathExpression.substring(1);
		}
		while (methodPathExpression.endsWith("/") || methodPathExpression.endsWith(".")) {
			methodPathExpression = methodPathExpression.substring(0, methodPathExpression.length() - 1);
		}

		if (!methodName.equals("") && methodPathExpression.startsWith(methodName)) {
			return methodPathExpression.substring(methodName.length());
		}

		return "";
	}
	
	public boolean isValid(String methodPathExpression) {
		return methodPathExpression.matches(METHOD_PATH_EXP);
	}
}
