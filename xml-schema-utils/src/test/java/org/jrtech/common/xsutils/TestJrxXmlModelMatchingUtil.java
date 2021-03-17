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

import java.net.URL;

import org.jrtech.common.xsutils.model.JrxDocument;
import org.jrtech.common.xsutils.model.JrxElement;
import org.jrtech.common.xmlutils.XmlUtils;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;

import com.sun.xml.xsom.XSModelGroup;
import com.sun.xml.xsom.XSParticle;
import com.sun.xml.xsom.XSTerm;

import org.junit.Assert;

public class TestJrxXmlModelMatchingUtil {
    private static final Logger log = LoggerFactory.getLogger(TestJrxXmlModelMatchingUtil.class);

    private static final String INTERACT_XSD_URL = "/schema/SwInt.xsd";

    private JrxXmlModelUtil modelUtil = JrxXmlModelUtil.newInstance();

    private JrxXmlModelMatchingUtil matchingUtil = JrxXmlModelMatchingUtil.getInstance();

    @Test
    public void testMatchingChoice() throws Exception {
        performMatching("/data/matching-choice.xml", "/schema/aaachoice.001.001.01.xsd",
                new String[][] { { "Doc", "urn:test:xsd:aaachoice.001.001.01" } }, "//Doc:ChoiceTestDocument");
    }

    @Test
    public void testMatchingChoice2() throws Exception {
        performMatching("/data/matching-choice.xml", "/schema/aaachoice.001.001.01.xsd",
                new String[][] { { "Doc", "urn:test:xsd:aaachoice.001.001.01" } },
                "//Doc:ChoiceTestDocument/Doc:testNode");
    }

    @SuppressWarnings("static-access")
    private void performMatching(String inputXml, String inputXsd, String[][] namespaces, String xpath)
	        throws Exception {

		log.info("Load document start");
		URL url = getClass().getResource(inputXml);
		Document xmlDoc = XmlUtils.openDocumentNS(url);
		log.info("Load document end");

		log.info("Add InterAct schema start");
		URL interActSchemaUrl = getClass().getResource(INTERACT_XSD_URL);
		modelUtil.addSchema(interActSchemaUrl);
		log.info("Add InterAct schema end");

		log.info("Add document schema start");
		URL schemaUrl = getClass().getResource(inputXsd);
		modelUtil.addSchema(schemaUrl);
		log.info("Add document schema end");
		log.info("Conversion start");
		JrxDocument jrxDoc = modelUtil.convertXmlToJrxModel(xmlDoc);
		log.info("Conversion end");
		log.info("Output:\n" + modelUtil.convertDocumentToString(jrxDoc));

		JrxElement jrxElement = modelUtil.getElementByXPath(jrxDoc, namespaces, xpath);
		Assert.assertNotNull(jrxElement);

		matchingUtil.performMatching(jrxElement);
		for (int i = 0; i < matchingUtil.getNewParticleArray().length; i++) {
			XSParticle xsParticle = matchingUtil.getNewParticleArray()[i];
			if (xsParticle == null) {
				System.out.println(i + " -> is defined! -> " + matchingUtil.getExistingTermArray()[i]);
			} else {
				XSTerm xsTerm = xsParticle.getTerm();
				if (xsTerm.isModelGroup()) {
					XSModelGroup xsModelGroup = (XSModelGroup) xsTerm;
					System.out.println(i + " *NEW* -> " + xsModelGroup.getCompositor() + modelUtil.getMultiplicitySymbol(xsParticle.getMinOccurs().intValue(), xsParticle.getMaxOccurs().intValue()));
				} else {
					System.out.println(i + " *NEW* -> " + xsTerm + modelUtil.getMultiplicitySymbol(xsParticle.getMinOccurs().intValue(), xsParticle.getMaxOccurs().intValue()));
				}
			}
		}
	}
}
