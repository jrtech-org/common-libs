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

import java.io.InputStream;
import java.util.List;
import java.util.Stack;

import javax.xml.XMLConstants;

import org.jrtech.common.xmlutils.XmlUtils;
import org.junit.Test;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import org.junit.Assert;

public class TestCompleteXmlNamespace {

    @Test
    public void completeNamespace1() throws Exception {
        InputStream is = getClass().getResourceAsStream("/input/invalid-namespace1.xml");
        Document xmlDoc = XmlUtils.createDocument(is);
        // System.out.println("Before: " + XMLUtil.documentToString(xmlDoc));
        completeNamespace(xmlDoc.getDocumentElement());
        System.out.println("After: " + XmlUtils.documentToString(xmlDoc));
    }

    private void completeNamespace(Element xmlElement) {
        completeNamespace(xmlElement, "", new Stack<String>());
    }
    
    private void completeNamespace(Element xmlElement, String currentNamespacePrefix, Stack<String> namespacePrefixStack) {
        String tagName = xmlElement.getTagName();
        String prefix = xmlElement.getPrefix();

        if (prefix == null) {
            if (tagName.indexOf(':') > 0) {
                prefix = tagName.substring(0, tagName.indexOf(':'));
            }
        }

        boolean newNamespace = false;
        if (!prefix.equals(currentNamespacePrefix)) {
            String namespaceAttribute = XMLConstants.XMLNS_ATTRIBUTE + ":" + prefix;
            Attr xmlAttr = xmlElement.getAttributeNode(namespaceAttribute);
            if (xmlAttr == null) {
                xmlElement.setAttribute(namespaceAttribute, "urn:swift:snl:ns." + prefix);
            }

            newNamespace = true;
            if (currentNamespacePrefix != null && !currentNamespacePrefix.equals(""))
                namespacePrefixStack.push(currentNamespacePrefix);
        }

        try {
        	List<Element> xmlChildElementList = XmlUtils.getChildElementList(xmlElement);
            for (Element xmlChildElement : xmlChildElementList) {
                completeNamespace(xmlChildElement, prefix, namespacePrefixStack);
            }
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail("Exception is retrieving child elements!");
        }

        if (newNamespace) {
            if (!namespacePrefixStack.isEmpty())
                namespacePrefixStack.pop();
        }
    }
}
