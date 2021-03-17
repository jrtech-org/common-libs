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

import org.jrtech.common.utils.validation.ValidationMessage;
import org.jrtech.common.utils.validation.ValidationResultItem;
import org.jrtech.common.xsutils.model.JrxTerm;

/**
 * A Validation Error for Xml Processing.
 */
public class XmlValidationError extends ValidationResultItem<JrxTerm<?>, ValidationMessage> {

	private static final long serialVersionUID = 6012940777048105394L;

    public XmlValidationError(JrxTerm<?> element, ValidationMessage message) {
		super(element, message);
	}
	
	public XmlValidationError(JrxTerm<?> element, ValidationMessage message, Object... parameters) {
		super(element, message, parameters);
	}

}
