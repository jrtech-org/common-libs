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
package org.jrtech.common.xsutils;

import com.sun.xml.xsom.XSTerm;

public class UnhandledGroupTermException extends Exception {

	private static final long serialVersionUID = 8109598141130330476L;
	
    private XSTerm xsTerm;
	
	public UnhandledGroupTermException(XSTerm xsTerm) {
		super("Unhandled Model Group Term: " + xsTerm.getClass().getSimpleName());
		this.xsTerm = xsTerm;
	}

	public void setXsTerm(XSTerm xsTerm) {
		this.xsTerm = xsTerm;
	}

	public XSTerm getXsTerm() {
		return xsTerm;
	}

}
