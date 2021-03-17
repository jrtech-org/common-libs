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
package org.jrtech.common.xmlutils;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import org.apache.commons.lang3.StringUtils;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class SAXPrettyPrinterHandler extends DefaultHandler {

	public static final String SPACES = "          ";

	private Stack<Element> elementStack = new Stack<Element>();

	private Map<String, Element> elementMap = new Hashtable<String, Element>();

	private StringBuffer formattedXml = new StringBuffer();

	private String indent = "";

	private int indentSize = 4;

	private boolean spaceOnTerminalElementClosing = true;

	public SAXPrettyPrinterHandler() {
		this(4, true);
	}

	public SAXPrettyPrinterHandler(int indentSize) {
		this(indentSize, true);
	}

	public SAXPrettyPrinterHandler(int indentSize, boolean spaceOnTerminalElementClosing) {
		super();
		if (indentSize > 10) indentSize = 10; // Max 10
		this.indentSize = indentSize;
		this.spaceOnTerminalElementClosing = spaceOnTerminalElementClosing;
	}

	@Override
	public void startDocument() throws SAXException {
		formattedXml = new StringBuffer();
		elementMap = new Hashtable<String, Element>();
		elementStack = new Stack<Element>();
	}

	@Override
	public void characters(char[] ch, int start, int length) throws SAXException {
		String textValue = new String(ch, start, length);
		Element element = elementStack.peek();
		element.value += textValue;
		element.value = StringUtils.trimToEmpty(element.value);
	}

	@Override
	public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
		Element element = new Element(qName);
		Element parentElement = null;
		if (!elementStack.isEmpty()) {
			parentElement = elementStack.peek();
			element.setParent(parentElement);
			formattedXml.append("\n").append(indent);
		}
		elementStack.push(element);
		elementMap.put(element.getScopedName(), element);
		formattedXml.append("<").append(qName);
		int i = 0;
		while (i < attributes.getLength()) {
			Attribute attr = new Attribute(element, attributes.getQName(i), attributes.getValue(i));
			formattedXml.append(" ").append(attr.name).append("=\"").append(attr.value).append("\"");
			i++;
		}

		formattedXml.append(">");

		indent += SPACES.substring(0, indentSize);
	}

	@Override
	public void endElement(String uri, String localName, String qName) throws SAXException {
		Element element = elementStack.pop();

		String textContent = element.value;
		if (textContent.length() > 0) {
			if (textContent.indexOf('\n') > 0) {
				// Preserve spaces
				textContent = StringUtils.replace(textContent, " ", "&nbsp;");
			}
			formattedXml.append(element.value);
		}

		if (indent.length() > indentSize) {
			indent = indent.substring(0, indent.length() - indentSize);
		} else {
			indent = "";
		}

		if (element.childElementList.isEmpty() && "".equals(element.value)) {
			formattedXml.setLength(formattedXml.length() - 1);
			if (spaceOnTerminalElementClosing)
				formattedXml.append(" ");
			formattedXml.append("/>");
		} else {
			if (!element.childElementList.isEmpty() || textContent.indexOf('\n') > 0) {
				formattedXml.append("\n").append(indent);
			}
			formattedXml.append("</").append(qName).append(">");
		}
	}

	public String getFormattedXml() {
		return formattedXml.toString();
	}

	static class Element {
		private String name;
		private Element parent = null;
		private List<Element> childElementList;
		private List<Attribute> attributeList;
		private String value = "";

		Element(String name) {
			this.name = name;
			childElementList = new ArrayList<Element>();
			attributeList = new ArrayList<Attribute>();
		}

		public void setParent(Element parent) {
			this.parent = parent;
			if (parent != null)
				parent.childElementList.add(this);
		}

		public String getScopedName() {
			return (parent == null ? "" : parent.name + ".") + name;
		}
	}

	static class Attribute {
		private String name;
		private Element element = null;
		private String value = "";

		Attribute(Element element, String name, String value) {
			this.element = element;
			this.name = name;
			this.value = value;
			element.attributeList.add(this);
		}

		public String getScopedName() {
			return element.name + "." + name;
		}
	}
}
