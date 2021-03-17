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

/**
 * It is used by a validating XML parser to handle errors and to resolve entities (schemata).
 * 
 */
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

public class SAXParserHandler implements ErrorHandler, EntityResolver {

    private static Logger log = LoggerFactory.getLogger(SAXParserHandler.class);

    private Map<String, String> entityMap;

    // parser warning
    public void warning(SAXParseException exception) throws SAXException {
        // Bring things to a crashing halt
        log.debug("**Parsing Warning**" + "  Line:    " + exception.getLineNumber() + " " + "  URI:     "
                + exception.getSystemId() + " " + "  Message: " + exception.getMessage());
        throw new SAXException("Warning encountered");
    }

    // parser error
    public void error(SAXParseException exception) throws SAXException {
        // Bring things to a crashing halt
        log.debug("**Parsing Error**" + "  Line:    " + exception.getLineNumber() + " " + "  URI:     "
                + exception.getSystemId() + " " + "  Message: " + exception.getMessage());
        throw new SAXException("Error encountered");
    }

    // parser fatal error
    public void fatalError(SAXParseException exception) throws SAXException {
        // Bring things to a crashing halt
        log.debug("**Parsing Fatal Error**" + "  Line:    " + exception.getLineNumber() + " " + "  URI:     "
                + exception.getSystemId() + " " + "  Message: " + exception.getMessage());
        throw new SAXException("Fatal Error encountered");
    }

    // set the entity vector for validation
    // entityMap is of the form:
    // ("Test1.xsd", "META-INF/schema/Test1.xsd")
    public void setEntityMap(Map<String, String> entityMap) {
        this.entityMap = entityMap;
    }

    // implementation of EntityResolver.resolveEntity
    public InputSource resolveEntity(String publicId, String systemId) {
        log.debug("publicId = " + publicId + "; systemId = " + systemId);

        // loop throug entity vector to find matching key
        for (String key : entityMap.keySet()) {
            if (systemId.endsWith(key)) {
                String value = entityMap.get(key);
                log.debug("Key: " + key + " maps to " + value);
                // load the entity as a resource
                return new InputSource(getClass().getClassLoader().getResource(value).toString());
            }
        }
        return null;
    }
}
