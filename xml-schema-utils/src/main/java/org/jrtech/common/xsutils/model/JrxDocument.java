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
package org.jrtech.common.xsutils.model;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class JrxDocument implements Serializable {

    private static final long serialVersionUID = 5135409263102549272L;
    
    private JrxElement rootElement;
    private Document xmlDocument;
    private Map<Element, JrxElement> elements;

    public JrxDocument() {
        super();
        elements = new HashMap<Element, JrxElement>();
    }
	
    public JrxElement getRootElement() {
        return rootElement;
    }
	
    public void setRootElement(JrxElement rootElement) {
        this.rootElement = rootElement;
    }

	public void setXmlDocument(Document xmlDocument) {
		this.xmlDocument = xmlDocument;
	}

	public Document getXmlDocument() {
		return xmlDocument;
	}
	
    @Override
    public int hashCode() {
        final int prime = 31; 
        int totalHashCode = 1;

        totalHashCode = prime * totalHashCode + ((rootElement == null) ? 0 : rootElement.hashCode());
		
        return totalHashCode;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;

		if (!(obj instanceof JrxDocument))
			return false;

		final JrxDocument other = (JrxDocument) obj;
		if (rootElement == null) {
			if (other.rootElement != null)
				return false;
		} else if (rootElement.hashCode() != other.rootElement.hashCode()) {
			return false;
		}

		return true;
	}

	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();

		sb.append("rootElement: ").append(rootElement);

		return sb.toString();
	}
	
	public void put(Element xmlElement, JrxElement jrxElement) {
		elements.put(xmlElement, jrxElement);
	}
	
	public void remove(Element xmlElement) {
		elements.remove(xmlElement);
	}
	
	public JrxElement get(Element xmlElement) {
		return elements.get(xmlElement);
	}
	
	public Collection<JrxElement> getElements() {
		return elements.values();
	}
	
	public Set<Entry<Element, JrxElement>> getElementEntries() {
		return elements.entrySet();
	}
	
	public void setNamespacePrefix(String prefix) {
		rootElement.setNamespacePrefix(prefix);
	}

}
