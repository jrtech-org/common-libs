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
import java.text.MessageFormat;
import java.util.Locale;

public class ValidationItem<E, M extends ValidationMessage> implements Serializable {

	private static final long serialVersionUID = -6978692668998122295L;

    // Element with error
	private E element;

	private M message;

	// Parameters to bind while building the localized error description
	private Object[] parameters;

	public ValidationItem(E element, M message) {
		this(element, message, (Object[]) null);
	}

	public ValidationItem(E element, M message, Object... parameters) {
		super();
		this.element = element;
		this.message = message;
		this.parameters = parameters;
	}

	public E getElement() {
		return element;
	}

	public void setElement(E element) {
		this.element = element;
	}

	public M getMessage() {
		return message;
	}

	public void setMessage(M message) {
		this.message = message;
	}

	public Object[] getParameters() {
		return parameters;
	}

	public void setParameters(Object... parameters) {
		this.parameters = parameters;
	}

	public String getDefaultMessage() {
		return MessageFormat.format(message.getDefaultDescription(), parameters);
	}

	public String getDefaultMessage(Locale locale) {
		MessageFormat mf = new MessageFormat(message.getDefaultDescription(), locale);
		return mf.format(parameters, new StringBuffer(), null).toString();
	}

	@Override
	public String toString() {
	    return element + " -> " + message;
	}

}
