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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.jrtech.common.xsutils.model.JrxChoiceGroupUtil;
import org.jrtech.common.xsutils.model.JrxDocument;
import org.jrtech.common.xsutils.model.JrxElement;
import org.jrtech.common.xmlutils.XmlUtils;
import org.jrtech.common.xsutils.model.JrxAttribute;
import org.jrtech.common.xsutils.model.JrxChoiceGroup;
import org.jrtech.common.xsutils.model.JrxDeclaration;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import com.sun.xml.xsom.XSType;

/*********************************************************************************************************************
 * The class <code>TestJrxXmlModelUtil</code>
 * 
 *********************************************************************************************************************/
public class TestJrxXmlModelUtil {

	private static final Logger log = LoggerFactory.getLogger(TestJrxXmlModelUtil.class);

	private static final String SWIFT_INTERACT_XSD_URL = "schema/SwInt.xsd";

	@SuppressWarnings("unused")
	private static final String SWIFT_XSD_URL = "schema/Sw.xsd";

	@SuppressWarnings("unused")
	private static final String SWIFT_SECURITY_XSD_URL = "schema/SwSec.xsd";

	@SuppressWarnings("unused")
	private static final String SWIFT_GLOBAL_XSD_URL = "schema/SwGbl.xsd";

	private JrxXmlModelUtil util = JrxXmlModelUtil.newInstance();

	@Test
	public void testConvertXmlDocToJrxDocSimple() throws Exception {
		long[][] elapsedTimeArray = new long[3][2];
		elapsedTimeArray[0][0] = System.nanoTime();
		JrxDocument jrxDoc1 = convertXmlDocToJrxDoc("input/simple1.xml", "schema/aaachoice.001.001.01.xsd");
		elapsedTimeArray[0][1] = System.nanoTime();
		elapsedTimeArray[1][0] = System.nanoTime();
		JrxDocument jrxDoc2 = convertXmlDocToJrxDoc("input/simple1.xml", "schema/aaachoice.001.001.01.xsd");
		elapsedTimeArray[1][1] = System.nanoTime();
		elapsedTimeArray[2][0] = System.nanoTime();
		JrxDocument jrxDoc3 = convertXmlDocToJrxDoc("input/simple1.xml", "schema/aaachoice.001.001.01.xsd");
		elapsedTimeArray[2][1] = System.nanoTime();
		
		System.out.println("#1: " + jrxDoc1.getRootElement().hashCode() + "\n#2: " + jrxDoc2.getRootElement().hashCode() + "\n#3: " + jrxDoc3.getRootElement().hashCode());
		System.out.println("#1: " + jrxDoc1.getRootElement().getScopedNameHashCode() + "\n#2: " + jrxDoc2.getRootElement().getScopedNameHashCode() + "\n#3: " + jrxDoc3.getRootElement().getScopedNameHashCode());
		System.out.println("#1: " + ((JrxElement) jrxDoc1.getRootElement().getChildrenBlock().getElements().get(0)).getChildrenBlock().getElements().get(4).getScopedNameHashCode() +
				           "\n#2: " + ((JrxElement) jrxDoc2.getRootElement().getChildrenBlock().getElements().get(0)).getChildrenBlock().getElements().get(4).getScopedNameHashCode() + 
				           "\n#3: " + ((JrxElement) jrxDoc3.getRootElement().getChildrenBlock().getElements().get(0)).getChildrenBlock().getElements().get(4).getScopedNameHashCode());
		for (int i = 0; i < elapsedTimeArray.length; i++) {
			BigDecimal bd = new BigDecimal(elapsedTimeArray[i][1] - elapsedTimeArray[i][0]);
			bd = bd.divide(new BigDecimal(1000000));
			System.out.println("Round #" + (i + 1) + " -> " + bd.floatValue() + " ms.");
        }
	}

	@Test
	public void testEnrichJrxElementSimple1() throws Exception {
		enrichJrxDocument("input/simple1.xml", "schema/aaachoice.001.001.01.xsd", "expected/simple1.xml");
	}

	@Test
	public void testEnrichJrxElementSimple2() throws Exception {
		enrichJrxDocument("input/simple2.xml", "schema/aaachoice.001.001.01.xsd", "expected/simple2.xml");
	}

	@Test
	public void testEnrichJrxElementSimple3() throws Exception {
		enrichJrxDocument("input/request-header.xml", "schema/SwInt.xsd", "expected/request-header.xml");
	}

	@Test
	public void testEnrichJrxElementComplex1() throws Exception {
		enrichJrxDocument("input/enrichcomplex1.xml", "schema/setr.004.001.03.xsd", "expected/enrichcomplex1.xml");
	}

	public void testEnrichJrxElementChoice1() throws Exception {
		enrichJrxDocumentWithoutExpectation("expected/choice1.xml", "schema/aaachoice.001.001.01.xsd");
	}

	public void testEnrichJrxElementChoice2() throws Exception {
		enrichJrxDocumentWithoutExpectation("expected/choicegroup1.xml", "schema/aaachoice.001.001.01.xsd");
	}

	@Test
	public void testEnrichJrxElementComplex2() throws Exception {
		enrichJrxDocument("input/enrichcomplex2.xml", "schema/aaachoice.001.001.01.xsd", "expected/enrichcomplex2.xml");
	}

	@Test
	public void testEnrichJrxElementComplex3() throws Exception {
		enrichJrxDocument("input/enrichcomplex3.xml", "schema/aaachoice.001.001.01.xsd", "expected/enrichcomplex3.xml");
	}

	@Test
	public void testConvertXmlDocToJrxDocComplex1() throws Exception {
		convertXmlDocToJrxDoc("input/setr.004.xml", "schema/setr.004.001.03.xsd");
	}

	@Test
	public void testConvertXmlDocToJrxDocComplex2() throws Exception {
		convertXmlDocToJrxDoc("input/existing-setr012.xml", "schema/setr.012.001.03.xsd");
	}

	private void enrichJrxDocumentWithoutExpectation(String inputXmlFile, String schemaFile) throws Exception {
		URL url = getClass().getClassLoader().getResource(inputXmlFile);
		Document xmlDoc = XmlUtils.openDocumentNS(url);

		URL interActSchemaUrl = getClass().getClassLoader().getResource(SWIFT_INTERACT_XSD_URL);
		util.addSchema(interActSchemaUrl);

		URL schemaUrl = getClass().getClassLoader().getResource(schemaFile);
		util.addSchema(schemaUrl);

		log.debug("Start");
		log.debug("XML Document: " + XmlUtils.nodeToString(xmlDoc, true));
		JrxDocument jrxDoc = util.convertXmlToJrxModel(xmlDoc);
		String actualDocString = util.convertDocumentToString(jrxDoc, true);
		log.debug("Output:\n" + actualDocString);

		jrxDoc = util.enrichJrxDocument(jrxDoc);
		actualDocString = util.convertDocumentToString(jrxDoc, true);
		log.debug("Output after enrichment:\n" + actualDocString);
	}

	private JrxDocument convertXmlDocToJrxDoc(String inputXmlFile, String schemaFile) throws Exception {
		log.debug("Load document start");
		URL url = getClass().getClassLoader().getResource(inputXmlFile);
		Document xmlDoc = XmlUtils.openDocumentNS(url);
		log.debug("Load document end");

		log.debug("Add InterAct schema start");
		URL interActSchemaUrl = getClass().getClassLoader().getResource(SWIFT_INTERACT_XSD_URL);
		util.addSchema(interActSchemaUrl);
		log.debug("Add InterAct schema end");

		log.debug("Add document schema start");
		URL schemaUrl = getClass().getClassLoader().getResource(schemaFile);
		util.addSchema(schemaUrl);
		log.debug("Add document schema end");
		log.debug("Conversion start");
		JrxDocument jrxDoc = util.convertXmlToJrxModel(xmlDoc);
		log.debug("Conversion end");
		log.debug("Output:\n" + util.convertDocumentToString(jrxDoc, true));

		return jrxDoc;
	}

	@Test
	public void testGetLeafElementDataType() throws Exception {
		JrxDocument jrxDoc = convertXmlDocToJrxDoc("input/existing-setr012.xml", "schema/setr.012.001.03.xsd");

		for (JrxElement jrxElement : jrxDoc.getElements()) {
			String baseTypeName = getBaseType(jrxElement);
			if (baseTypeName != null) {
				log.debug("Node: " + jrxElement.getName() + " -> " + baseTypeName);
			}
		}
	}

	private String getBaseType(JrxElement jrxElement) {
		if (jrxElement.getXsdDeclaration() == null) {
			return null;
		}

		XSType xsType = jrxElement.getXsdDeclaration().getType();

		if (xsType.isSimpleType()) {
			return xsType.getBaseType().getName();
		}

		return null;
	}

	@Test
	public void testConvertXsdToJrxDocSimple() throws Exception {
		convertXsdToJrxDoc(getClass().getClassLoader().getResource("input/complete-namespace1.xsd"), "Doc", "Document",
		        getClass().getClassLoader().getResource("expected/complete-namespace1.xml"));
	}

	@Test
	public void testConvertXsdToJrxDocXsExtension() throws Exception {
		URL interActSchemaUrl = getClass().getClassLoader().getResource(SWIFT_INTERACT_XSD_URL);
		util.addSchema(interActSchemaUrl);

		convertXsdToJrxDoc(getClass().getClassLoader().getResource("input/xs-extension-case.xsd"), "Doc", "Document",
		        getClass().getClassLoader().getResource("expected/xs-extension-case.xml"),
		        new NamespacePrefixAndUri[] { new NamespacePrefixAndUri("SwInt", "urn:swift:snl:ns.SwInt") });
	}

	@Test
	public void testConvertXsdToJrxDocXsExtension2() throws Exception {
		util.registerNamespaces(new NamespacePrefixAndUri[] { new NamespacePrefixAndUri("SwInt",
		        "urn:swift:snl:ns.SwInt") });
		JrxDocument jrxActualDoc = util.convertXsdToJrxDoc(
		        getClass().getClassLoader().getResource("input/xs-extension-case.xsd"), "Doc", "Document");
		String actualDocString = XmlUtils.documentToString(jrxActualDoc.getXmlDocument());
		System.out.println(Arrays.toString(util.getNamespacePrefixes()));
		log.debug("XML Document Output:\n" + actualDocString);
		log.debug("FMS Document Output:\n" + util.convertDocumentToString(jrxActualDoc, true));
	}

	@Test
	public void testConvertXsdToJrxDocComplex() throws Exception {
		convertXsdToJrxDoc(getClass().getClassLoader().getResource("input/complete-namespace2.xsd"), "Doc", "Document",
		        getClass().getClassLoader().getResource("expected/complete-namespace2.xml"));
	}

	private void convertXsdToJrxDoc(URL schemaUrl, String namespacePrefix, String rootElement, URL expectedDocUrl)
	        throws Exception {
		convertXsdToJrxDoc(schemaUrl, namespacePrefix, rootElement, expectedDocUrl, null);
	}

	private void convertXsdToJrxDoc(URL schemaUrl, String namespacePrefix, String rootElement, URL expectedDocUrl,
	        NamespacePrefixAndUri[] namespaceArray) throws Exception {
		if (namespaceArray != null)
			util.registerNamespaces(namespaceArray);

		JrxDocument jrxActualDoc = util.convertXsdToJrxDoc(schemaUrl, namespacePrefix, rootElement);
		String actualDocString = XmlUtils.documentToString(jrxActualDoc.getXmlDocument());
		Document xmlExpectedDoc = XmlUtils.openDocumentNS(expectedDocUrl);
		String expectedDocString = XmlUtils.documentToString(xmlExpectedDoc);
		log.debug("XML Document Output:\n" + XmlUtils.documentToString(jrxActualDoc.getXmlDocument()));
		log.debug("FMS Document Output:\n" + util.convertDocumentToString(jrxActualDoc, true));
		Assert.assertEquals(expectedDocString, actualDocString);
	}

	@Test
	public void testCreateNewMessage() throws Exception {
		JrxDocument jrxActualDoc = util.convertXsdToJrxDoc(
		        getClass().getClassLoader().getResource("input/complete-namespace2.xsd"), "Doc", "Document");
		String actualDocString = XmlUtils.documentToString(jrxActualDoc.getXmlDocument());
		String envelopDocString = FileUtils.readFileToString(new File(getClass().getClassLoader()
		        .getResource("input/swint-envelop.xml.txt").toURI()), Charset.forName("UTF-8"));

		// Remove XML definition
		// actualDocString = actualDocString.replaceFirst("[<][ ]*[?][ ]*xml[ ]*version.*[ ]*[?][ ]*>[ \\t\\n\\r]*",
		// "");

		String packagedDocString = StringUtils.replace(envelopDocString, "${Document}", actualDocString);
		Document xmlDoc = XmlUtils.createDocument(packagedDocString);
		log.debug("New initial message:\n" + XmlUtils.documentToString(xmlDoc));
	}

	@Test
	public void testGetNodePathArrayOnSameNamespace() throws Exception {
		URL url = getClass().getClassLoader().getResource("data/setr.004.xml");
		Document xmlDoc = XmlUtils.openDocumentNS(url);

		URL interActSchemaUrl = getClass().getClassLoader().getResource(SWIFT_INTERACT_XSD_URL);
		util.addSchema(interActSchemaUrl);

		URL schemaUrl = getClass().getClassLoader().getResource("schema/setr.004.001.03.xsd");
		util.addSchema(schemaUrl);

		Element xmlElement = (Element) XmlUtils.getNodeByXPath(xmlDoc, new String[][] {
		        { "Doc", "urn:swift:xsd:setr.004.001.03" }, { "SwInt", "urn:swift:snl:ns.SwInt" } }, "//Doc:Document");
		log.debug("Document element: " + xmlElement);
		log.debug("Document path array: " + arrayToString(util.getNodePathArrayOnSameNamespace(xmlElement)));

		xmlElement = (Element) XmlUtils.getNodeByXPath(xmlDoc, new String[][] {
		        { "Doc", "urn:swift:xsd:setr.004.001.03" }, { "SwInt", "urn:swift:snl:ns.SwInt" } },
		        "//Doc:MltplOrdrDtls");
		log.debug("MltplOrdrDtls element: " + xmlElement);
		log.debug("MltplOrdrDtls path array: " + arrayToString(util.getNodePathArrayOnSameNamespace(xmlElement)));
	}

	private String arrayToString(Object[] array) {
		if (array == null) {
			return "null";
		}
		StringBuffer sb = new StringBuffer();

		for (int i = 0; i < array.length; i++) {
			sb.append("" + array[i]).append("\n");
		}

		return sb.toString();
	}

	private void enrichJrxDocument(String inputXmlFile, String schemaFile, String expectedXmlFile) throws Exception {
		URL url = getClass().getClassLoader().getResource(inputXmlFile);
		Document xmlDoc = XmlUtils.openDocumentNS(url);

		URL interActSchemaUrl = getClass().getClassLoader().getResource(SWIFT_INTERACT_XSD_URL);
		util.addSchema(interActSchemaUrl);

		URL schemaUrl = getClass().getClassLoader().getResource(schemaFile);
		util.addSchema(schemaUrl);

		log.debug("Start");
		JrxDocument jrxDoc = util.convertXmlToJrxModel(xmlDoc);
		log.debug("End");
		String actualDocString = util.convertDocumentToString(jrxDoc, true);
		log.debug("Output:\n" + actualDocString);

		jrxDoc = util.enrichJrxDocument(jrxDoc);
		actualDocString = util.convertDocumentToString(jrxDoc, true);
		log.debug("Output after enrichment:\n" + actualDocString);

		url = getClass().getClassLoader().getResource(expectedXmlFile);
		xmlDoc = XmlUtils.openDocumentNS(url);

		log.debug("Start");
		JrxDocument jrxExpectedDoc = util.convertXmlToJrxModel(xmlDoc);
		log.debug("End");

		String expectedDocString = util.convertDocumentToString(jrxExpectedDoc);
		log.debug("Expected Output:\n" + expectedDocString);

		jrxExpectedDoc = util.enrichJrxDocument(jrxExpectedDoc);
		expectedDocString = util.convertDocumentToString(jrxExpectedDoc, true);
		log.debug("Output after enrichment:\n" + expectedDocString);

		Assert.assertEquals(expectedDocString, actualDocString);

		log.debug("XML Doc:\n" + XmlUtils.documentToString(jrxDoc.getXmlDocument()));
	}

	@Test
	public void testAddChoice1() throws Exception {
		addChoice("input/choice1.xml", "schema/aaachoice.001.001.01.xsd", "expected/choice1.xml", new String[][] { {
		        "doc", "urn:test:xsd:aaachoice.001.001.01" } }, new String[] {
		        "/doc:Document/doc:ChoiceTestDocument/doc:AcctSvcr",
		        "/doc:Document/doc:ChoiceTestDocument/doc:OwnrId[last()]" }, new int[] { 0, 0, 1, 2, 2 });
	}

	@Test
	public void testAddChoice2() throws Exception {
		addChoice("input/choice2.xml", "schema/aaachoice.001.001.01.xsd", "expected/choice2.xml", new String[][] { {
		        "doc", "urn:test:xsd:aaachoice.001.001.01" } }, new String[] {
		        "/doc:Document/doc:ChoiceTestDocument/doc:AcctSvcr",
		        "/doc:Document/doc:ChoiceTestDocument/doc:OwnrId[last()]",
		        "/doc:Document/doc:ChoiceTestDocument/doc:OwnrId[last()]",
		        "/doc:Document/doc:ChoiceTestDocument/doc:OwnrId[last()]",
		        "/doc:Document/doc:ChoiceTestDocument/doc:testNode/doc:RcptId" }, new int[] { 0, 0, 1, 2, 2 });
	}

	@Test
	public void testAddChoiceComplete1() throws Exception {
		URL url = getClass().getClassLoader().getResource("input/complete-choice.xml");
		// Document xmlDoc = XmlUtils.openDocumentNS(url);
		Document xmlDoc = XmlUtils.openDocument(url);

		URL interActSchemaUrl = getClass().getClassLoader().getResource(SWIFT_INTERACT_XSD_URL);
		util.addSchema(interActSchemaUrl);

		URL schemaUrl = getClass().getClassLoader().getResource("schema/complete-choice.xsd");
		util.addSchema(schemaUrl);

		JrxDocument jrxActualDoc = util.convertXmlToJrxModel(xmlDoc);
		jrxActualDoc = util.enrichJrxDocument(jrxActualDoc);
		util.addChoiceHelperElement(jrxActualDoc);
		String actualDocString = util.convertDocumentToString(jrxActualDoc, true);
		log.debug("Before add:\n" + actualDocString);
		log.debug("XML Document:\n" + XmlUtils.documentToString(jrxActualDoc.getXmlDocument()));

		JrxElement jrxParentElement = util.getElementByXPath(jrxActualDoc, null, "/Document/ChoiceTestDocument");
		Assert.assertNotNull(jrxParentElement);
		JrxChoiceGroup jrxDirectChoiceGroup = null;
		for (int i = 0; i < jrxParentElement.getChildrenBlock().getElements().size(); i++) {
			if (jrxParentElement.getChildrenBlock().getElements().get(i) instanceof JrxChoiceGroup) {
				jrxDirectChoiceGroup = (JrxChoiceGroup) jrxParentElement.getChildrenBlock().getElements().get(i);
			}

		}

		util.addChoiceElement(jrxDirectChoiceGroup, 1);

		actualDocString = util.convertDocumentToString(jrxActualDoc, true);
		log.debug("After add:\n" + actualDocString);
		log.debug("XML Document:\n" + XmlUtils.documentToString(jrxActualDoc.getXmlDocument()));
	}

	@Test
	public void testChangeIndirectChoiceContent() throws Exception {
		String inputXmlFile = "input/choice1.xml";
		String schemaFile = "schema/aaachoice.001.001.01.xsd";
		// String expectedFile = "expected/choice1.xml";
		String choiceXPath = "/doc:Document/doc:ChoiceTestDocument/doc:Securities";

		URL url = getClass().getClassLoader().getResource(inputXmlFile);
		Document xmlDoc = XmlUtils.openDocumentNS(url);

		URL interActSchemaUrl = getClass().getClassLoader().getResource(SWIFT_INTERACT_XSD_URL);
		util.addSchema(interActSchemaUrl);

		URL schemaUrl = getClass().getClassLoader().getResource(schemaFile);
		util.addSchema(schemaUrl);

		JrxDocument jrxActualDoc = util.convertXmlToJrxModel(xmlDoc);
		jrxActualDoc = util.enrichJrxDocument(jrxActualDoc);
		util.addChoiceHelperElement(jrxActualDoc);
		String actualDocString = util.convertDocumentToString(jrxActualDoc, true);
		log.debug("Before add:\n" + actualDocString);
		log.debug("XML Document:\n" + XmlUtils.documentToString(jrxActualDoc.getXmlDocument()));

		JrxElement jrxChoiceElement = util.getElementByXPath(jrxActualDoc, new String[][] { { "doc",
		        "urn:test:xsd:aaachoice.001.001.01" } }, choiceXPath);
		Assert.assertNotNull(jrxChoiceElement);

		for (int i = 0; i < 3; i++) {
			int optionIndex = i;

			util.addChoiceElement(jrxChoiceElement, optionIndex);
			// util.addChoiceHelperElement(jrxChoiceElement);

			actualDocString = util.convertDocumentToString(jrxActualDoc, true);
			log.debug("After add: " + choiceXPath + " - " + optionIndex + "\n" + actualDocString);
			log.debug("XML Document:\n" + XmlUtils.documentToString(jrxActualDoc.getXmlDocument()));
		}

		// Check against expected file
		// url = getClass().getClassLoader().getResource(expectedXmlFile);
		// xmlDoc = XmlUtils.openDocumentNS(url);
		//
		// JrxDocument jrxExpectedDoc = util.convertXmlToJrxModel(xmlDoc);
		// log.debug("Expected XML Document:\n" + XmlUtils.documentToString(jrxExpectedDoc.getXmlDocument()));
		//
		// Assert.assertEquals(expectedDocString, actualDocString);
		// log.debug("Output after enrichment:\n" + expectedDocString);
		//
		// jrxExpectedDoc = util.removeChoiceHelperElement(jrxExpectedDoc);
		// expectedDocString = util.convertJrxDocumentToString(jrxExpectedDoc, true);
		// log.debug("Output after choice helper removed:\n" + expectedDocString);
	}

	@Test
	public void testChangeDirectChoiceContent() throws Exception {
		String inputXmlFile = "input/choice1.xml";
		String schemaFile = "schema/aaachoice.001.001.01.xsd";
		// String expectedFile = "expected/choice1.xml";
		String choiceParentXPath = "/doc:Document/doc:ChoiceTestDocument/doc:Order";

		URL url = getClass().getClassLoader().getResource(inputXmlFile);
		Document xmlDoc = XmlUtils.openDocumentNS(url);

		URL interActSchemaUrl = getClass().getClassLoader().getResource(SWIFT_INTERACT_XSD_URL);
		util.addSchema(interActSchemaUrl);

		URL schemaUrl = getClass().getClassLoader().getResource(schemaFile);
		util.addSchema(schemaUrl);

		JrxDocument jrxActualDoc = util.convertXmlToJrxModel(xmlDoc);
		jrxActualDoc = util.enrichJrxDocument(jrxActualDoc);
		util.addChoiceHelperElement(jrxActualDoc);
		String actualDocString = util.convertDocumentToString(jrxActualDoc, true);
		log.debug("Before add:\n" + actualDocString);
		log.debug("XML Document:\n" + XmlUtils.documentToString(jrxActualDoc.getXmlDocument()));

		JrxElement jrxChoiceParentElement = util.getElementByXPath(jrxActualDoc, new String[][] { { "doc",
		        "urn:test:xsd:aaachoice.001.001.01" } }, choiceParentXPath);
		Assert.assertNotNull(jrxChoiceParentElement);
		JrxChoiceGroup jrxChoiceGroup = null;
		for (int i = 0; i < jrxChoiceParentElement.getChildrenBlock().getElements().size(); i++) {
			if (jrxChoiceParentElement.getChildrenBlock().getElements().get(i) instanceof JrxChoiceGroup) {
				jrxChoiceGroup = (JrxChoiceGroup) jrxChoiceParentElement.getChildrenBlock().getElements().get(i);
				break;
			}
		}
		Assert.assertNotNull(jrxChoiceGroup);

		for (int i = 0; i < 3; i++) {
			int optionIndex = i;

			util.addChoiceElement(jrxChoiceGroup, optionIndex);

			actualDocString = util.convertDocumentToString(jrxActualDoc, true);
			log.debug("After add: " + choiceParentXPath + "/" + jrxChoiceGroup.getName() + " - " + optionIndex + "\n"
			        + actualDocString);
			log.debug("XML Document:\n" + XmlUtils.documentToString(jrxActualDoc.getXmlDocument()));
		}

		// Check against expected file
		// url = getClass().getClassLoader().getResource(expectedXmlFile);
		// xmlDoc = XmlUtils.openDocumentNS(url);
		//
		// JrxDocument jrxExpectedDoc = util.convertXmlToJrxModel(xmlDoc);
		// log.debug("Expected XML Document:\n" + XmlUtils.documentToString(jrxExpectedDoc.getXmlDocument()));
		//
		// Assert.assertEquals(expectedDocString, actualDocString);
		// log.debug("Output after enrichment:\n" + expectedDocString);
		//
		// jrxExpectedDoc = util.removeChoiceHelperElement(jrxExpectedDoc);
		// expectedDocString = util.convertJrxDocumentToString(jrxExpectedDoc, true);
		// log.debug("Output after choice helper removed:\n" + expectedDocString);
	}

	private void addChoice(String inputXmlFile, String schemaFile, String expectedXmlFile, String[][] namespaces,
	        String[] choiceXPaths, int[] optionIndexes) throws Exception {
		URL url = getClass().getClassLoader().getResource(inputXmlFile);
		Document xmlDoc = XmlUtils.openDocumentNS(url);

		URL interActSchemaUrl = getClass().getClassLoader().getResource(SWIFT_INTERACT_XSD_URL);
		util.addSchema(interActSchemaUrl);

		URL schemaUrl = getClass().getClassLoader().getResource(schemaFile);
		util.addSchema(schemaUrl);

		JrxDocument jrxActualDoc = util.convertXmlToJrxModel(xmlDoc);
		jrxActualDoc = util.enrichJrxDocument(jrxActualDoc);
		util.addChoiceHelperElement(jrxActualDoc);
		String actualDocString = util.convertDocumentToString(jrxActualDoc, true);
		log.debug("Before add:\n" + actualDocString);
		log.debug("XML Document:\n" + XmlUtils.documentToString(jrxActualDoc.getXmlDocument()));

		for (int i = 0; i < choiceXPaths.length; i++) {
			JrxElement jrxChoiceElement = util.getElementByXPath(jrxActualDoc, namespaces, choiceXPaths[i]);
			Assert.assertNotNull(jrxChoiceElement);

			int optionIndex = 0;
			if (i < optionIndexes.length) {
				optionIndex = optionIndexes[i];
			}
			util.addChoiceElement(jrxChoiceElement, optionIndex);
			util.addChoiceHelperElement(jrxChoiceElement);

			actualDocString = util.convertDocumentToString(jrxActualDoc, true);
			log.debug("After add: " + choiceXPaths[i] + " - " + optionIndex + "\n" + actualDocString);

			log.debug("XML Document:\n" + XmlUtils.documentToString(jrxActualDoc.getXmlDocument()));
		}

		// Check against expected file
		url = getClass().getClassLoader().getResource(expectedXmlFile);
		xmlDoc = XmlUtils.openDocumentNS(url);

		JrxDocument jrxExpectedDoc = util.convertXmlToJrxModel(xmlDoc);
		jrxExpectedDoc = util.enrichJrxDocument(jrxExpectedDoc);
		String expectedDocString = util.convertDocumentToString(jrxExpectedDoc, true);
		log.debug("Expected XML Document:\n" + XmlUtils.documentToString(jrxExpectedDoc.getXmlDocument()));

		Assert.assertEquals(expectedDocString, actualDocString);
		log.debug("Output after enrichment:\n" + expectedDocString);

		jrxExpectedDoc = util.removeChoiceHelperElement(jrxExpectedDoc);
		expectedDocString = util.convertDocumentToString(jrxExpectedDoc, true);
		log.debug("Output after choice helper removed:\n" + expectedDocString);
	}

	@Test
	public void testRemoveChoice1() throws Exception {
		removeChoice("input/choice3.xml", "schema/aaachoice.001.001.01.xsd", "expected/choice3.xml", new String[] {
		        "/Doc:Document/Doc:ChoiceTestDocument/Doc:OwnrId", "/Doc:Document/Doc:ChoiceTestDocument/Doc:OwnrId" });
	}

	private void removeChoice(String inputXmlFile, String schemaFile, String expectedXmlFile, String[] choiceXPaths)
	        throws Exception {
		URL url = getClass().getClassLoader().getResource(inputXmlFile);
		Document xmlDoc = XmlUtils.openDocumentNS(url);
		// Document xmlDoc = XmlUtils.openDocument(url);

		URL interActSchemaUrl = getClass().getClassLoader().getResource(SWIFT_INTERACT_XSD_URL);
		util.addSchema(interActSchemaUrl);

		URL schemaUrl = getClass().getClassLoader().getResource(schemaFile);
		util.addSchema(schemaUrl);

		JrxDocument jrxActualDoc = util.convertXmlToJrxModel(xmlDoc);
		jrxActualDoc = util.enrichJrxDocument(jrxActualDoc);
		String actualDocString = util.convertDocumentToString(jrxActualDoc, true);
		log.debug("Output:\n" + actualDocString);

		for (int i = 0; i < choiceXPaths.length; i++) {
			JrxElement jrxChoiceElement = util.getElementByXPath(jrxActualDoc, new String[][] { { "Doc",
			        "urn:test:xsd:aaachoice.001.001.01" } }, choiceXPaths[i]);
			// JrxElement jrxChoiceElement = util.getJrxElementByXPath(jrxActualDoc, util.getNamespacePrefixes(),
			// choiceXPaths[i]);
			JrxDeclaration<?> jrxParentDeclaration = jrxChoiceElement.getParentBlock().getOwner();
			JrxElement jrxParentElement = null;
			if (jrxParentDeclaration instanceof JrxElement) {
				jrxParentElement = (JrxElement) jrxParentDeclaration;
			} else {
				jrxParentElement = jrxParentDeclaration.getParentElement();
			}
			Assert.assertNotNull(jrxChoiceElement);

			util.removeChoiceElement(jrxChoiceElement);
			util.enrichJrxElement(jrxParentElement);
		}

		actualDocString = util.convertDocumentToString(jrxActualDoc, true);
		log.debug("Output:\n" + actualDocString);

		// Check against expected file
		url = getClass().getClassLoader().getResource(expectedXmlFile);
		xmlDoc = XmlUtils.openDocumentNS(url);

		JrxDocument jrxExpectedDoc = util.convertXmlToJrxModel(xmlDoc);
		jrxExpectedDoc = util.enrichJrxDocument(jrxExpectedDoc);
		String expectedDocString = util.convertDocumentToString(jrxExpectedDoc, true);
		log.debug("Output after enrichment:\n" + expectedDocString);

		Assert.assertEquals(expectedDocString, actualDocString);
		jrxActualDoc = util.removeChoiceHelperElement(jrxActualDoc);
		showElements(jrxActualDoc);
	}

	private void showElements(JrxDocument jrxDocument) {
		for (Iterator<Entry<Element, JrxElement>> it = jrxDocument.getElementEntries().iterator(); it.hasNext();) {
			Entry<Element, JrxElement> entry = it.next();
			log.debug(entry.getValue() + " -> Helper? " + JrxChoiceGroupUtil.isChoiceHelperElement(entry.getValue()));
		}
	}

	@Test
	public void testAddSimpleRepetitiveElement() throws Exception {
		addSimpleRepetitiveElement("input/simple-repetition-add1.xml", "schema/simple-repetition.xsd",
		        "expected/simple-repetition-add1.xml", new String[] { "//Doc:PrvsRef", "//Doc:PrvsRef",
		                "//Doc:Adr/Doc:AdrLine", "//Doc:Adr/Doc:AdrLine", "//Doc:Adr/Doc:AdrLine",
		                "//Doc:Adr/Doc:AdrLine", "//Doc:Adr/Doc:AdrLine", "//Doc:Adr/Doc:AdrLine" });
	}

	private void addSimpleRepetitiveElement(String inputXmlFile, String schemaFile, String expectedXmlFile,
	        String[] choiceXPaths) throws Exception {
		URL url = getClass().getClassLoader().getResource(inputXmlFile);
		Document xmlDoc = XmlUtils.openDocumentNS(url);
		// Document xmlDoc = XmlUtils.openDocument(url);

		URL interActSchemaUrl = getClass().getClassLoader().getResource(SWIFT_INTERACT_XSD_URL);
		util.addSchema(interActSchemaUrl);

		URL schemaUrl = getClass().getClassLoader().getResource(schemaFile);
		util.addSchema(schemaUrl);

		JrxDocument jrxActualDoc = util.convertXmlToJrxModel(xmlDoc);
		jrxActualDoc = util.enrichJrxDocument(jrxActualDoc);
		util.addChoiceHelperElement(jrxActualDoc);
		String actualDocString = util.convertDocumentToString(jrxActualDoc, true);
		log.debug("Output:\n" + actualDocString);

		for (int i = 0; i < choiceXPaths.length; i++) {
			// JrxElement jrxChoiceElement = util.getJrxElementByXPath(jrxActualDoc, new String[][] { { "doc",
			// "urn:test:xsd:aaachoice.001.001.01" } }, choiceXPaths[i]);
			JrxElement jrxRepetitiveElement = util.getElementByXPath(jrxActualDoc, util.getNamespacePrefixes(),
			        choiceXPaths[i]);
			Assert.assertNotNull(jrxRepetitiveElement);

			util.addSimpleRepetitiveElement(jrxRepetitiveElement);

			actualDocString = util.convertDocumentToString(jrxActualDoc, true);
			log.debug("Output:\n" + actualDocString);

		}

		// Check against expected file
		url = getClass().getClassLoader().getResource(expectedXmlFile);
		xmlDoc = XmlUtils.openDocumentNS(url);

		JrxDocument jrxExpectedDoc = util.convertXmlToJrxModel(xmlDoc);
		jrxExpectedDoc = util.enrichJrxDocument(jrxExpectedDoc);
		String expectedDocString = util.convertDocumentToString(jrxExpectedDoc, true);
		log.debug("Output after enrichment:\n" + expectedDocString);

		Assert.assertEquals(expectedDocString, actualDocString);

		jrxExpectedDoc = util.removeChoiceHelperElement(jrxExpectedDoc);
		expectedDocString = util.convertDocumentToString(jrxExpectedDoc, true);
		log.debug("Output after enrichment:\n" + expectedDocString);
	}

	@Test
	public void testRemoveSimpleRepetitiveElement1() throws Exception {
		removeSimpleRepetitiveElement("input/simple-repetition-remove1.xml", "schema/simple-repetition.xsd",
		        "expected/simple-repetition-remove1.xml", new String[] { "//Doc:PrvsRef[2]", "//Doc:PrvsRef" },
		        new boolean[] {});
	}

	@Test
	public void testRemoveSimpleRepetitiveElement2() throws Exception {
		removeSimpleRepetitiveElement("input/simple-repetition-remove2.xml", "schema/simple-repetition.xsd",
		        "expected/simple-repetition-remove2.xml", new String[] { "//Doc:PrvsRef[2]", "//Doc:PrvsRef",
		                "//Doc:PrvsRef" }, new boolean[] {});
	}

	@Test
	public void testRemoveSimpleRepetitiveElement3() throws Exception {
		removeSimpleRepetitiveElement("input/simple-repetition-remove3.xml", "schema/simple-repetition.xsd",
		        "expected/simple-repetition-remove3.xml", new String[] { "//Doc:PrvsRef[2]", "//Doc:PrvsRef",
		                "//Doc:PrvsRef" }, new boolean[] { false, false, true, true });
	}

	private void removeSimpleRepetitiveElement(String inputXmlFile, String schemaFile, String expectedXmlFile,
	        String[] choiceXPaths, boolean[] forceDeleteArrays) throws Exception {
		URL url = getClass().getClassLoader().getResource(inputXmlFile);
		Document xmlDoc = XmlUtils.openDocumentNS(url);
		// Document xmlDoc = XmlUtils.openDocument(url);

		URL interActSchemaUrl = getClass().getClassLoader().getResource(SWIFT_INTERACT_XSD_URL);
		util.addSchema(interActSchemaUrl);

		URL schemaUrl = getClass().getClassLoader().getResource(schemaFile);
		util.addSchema(schemaUrl);

		JrxDocument jrxActualDoc = util.convertXmlToJrxModel(xmlDoc);
		jrxActualDoc = util.enrichJrxDocument(jrxActualDoc);
		util.addChoiceHelperElement(jrxActualDoc);
		String actualDocString = util.convertDocumentToString(jrxActualDoc, true);
		log.debug("Output:\n" + actualDocString);

		for (int i = 0; i < choiceXPaths.length; i++) {
			boolean forceDelete = false;
			if (forceDeleteArrays != null && forceDeleteArrays.length > i)
				forceDelete = forceDeleteArrays[i];

			// JrxElement jrxChoiceElement = util.getJrxElementByXPath(jrxActualDoc, new String[][] { { "doc",
			// "urn:test:xsd:aaachoice.001.001.01" } }, choiceXPaths[i]);
			JrxElement jrxRepetitiveElement = util.getElementByXPath(jrxActualDoc, util.getNamespacePrefixes(),
			        choiceXPaths[i]);
			Assert.assertNotNull(jrxRepetitiveElement);

			if (forceDelete)
				util.removeSimpleRepetitiveElement(jrxRepetitiveElement, true);
			else
				util.removeSimpleRepetitiveElement(jrxRepetitiveElement);

			actualDocString = util.convertDocumentToString(jrxActualDoc, true);
			log.debug("After removal of node: " + choiceXPaths[i] + " Output:\n" + actualDocString);

		}

		// Check against expected file
		url = getClass().getClassLoader().getResource(expectedXmlFile);
		xmlDoc = XmlUtils.openDocumentNS(url);

		JrxDocument jrxExpectedDoc = util.convertXmlToJrxModel(xmlDoc);
		jrxExpectedDoc = util.enrichJrxDocument(jrxExpectedDoc);
		String expectedDocString = util.convertDocumentToString(jrxExpectedDoc, true);

		jrxActualDoc = util.removeChoiceHelperElement(jrxActualDoc);
		actualDocString = util.convertDocumentToString(jrxActualDoc, true);
		log.debug("After choice helper removal Output:\n" + actualDocString);

		jrxActualDoc = util.enrichJrxDocument(jrxActualDoc);
		actualDocString = util.convertDocumentToString(jrxActualDoc, true);

		Assert.assertEquals(expectedDocString, actualDocString);

		log.debug("Output after enrichment:\n" + actualDocString);

		jrxExpectedDoc = util.removeChoiceHelperElement(jrxExpectedDoc);
		jrxActualDoc = util.removeChoiceHelperElement(jrxActualDoc);

		actualDocString = util.convertDocumentToString(jrxActualDoc, true);
		log.debug("Output after enrichment (without helper):\n" + actualDocString);

		String actualXmlString = XmlUtils.documentToString(jrxActualDoc.getXmlDocument());
		// actualXmlString = actualXmlString.replaceAll("(([\n]|[\r]|[\r\n])([\t]|[ ]*))+([\n]|[\r]|[\r\n])", "\n");
		String expectedXmlString = XmlUtils.documentToString(jrxExpectedDoc.getXmlDocument());
		// expectedXmlString = expectedXmlString.replaceAll("(([\n]|[\r]|[\r\n])([\t]|[ ]*))+([\n]|[\r]|[\r\n])", "\n");

		Assert.assertEquals(expectedXmlString, actualXmlString);

		log.debug("Output XML:\n" + actualXmlString);

	}

	@Test
	public void testAddOptionInChoiceGroup() throws Exception {
		addOptionInChoiceGroup("input/choicegroup1.xml", "schema/aaachoice.001.001.01.xsd",
		        "expected/choicegroup1.xml", new String[] { "/Document/ChoiceTestDocument/testNode" },
		        new int[] { 0, 1 }, new int[] { 0, 1, 0 });
	}

	private void addOptionInChoiceGroup(String inputXmlFile, String schemaFile, String expectedXmlFile,
	        String[] choiceParentXPaths, int[] choiceElementIndexes, int[] optionIndexes) throws Exception {
		URL url = getClass().getClassLoader().getResource(inputXmlFile);
		// Document xmlDoc = XmlUtils.openDocumentNS(url);
		Document xmlDoc = XmlUtils.openDocument(url);

		URL interActSchemaUrl = getClass().getClassLoader().getResource(SWIFT_INTERACT_XSD_URL);
		util.addSchema(interActSchemaUrl);

		URL schemaUrl = getClass().getClassLoader().getResource(schemaFile);
		util.addSchema(schemaUrl);

		JrxDocument jrxActualDoc = util.convertXmlToJrxModel(xmlDoc);
		jrxActualDoc = util.enrichJrxDocument(jrxActualDoc);
		util.addChoiceHelperElement(jrxActualDoc);
		String actualDocString = util.convertDocumentToString(jrxActualDoc, true);
		log.debug("Before add:\n" + actualDocString);

		for (int i = 0; i < choiceParentXPaths.length; i++) {
			JrxElement jrxChoiceParentElement = util.getElementByXPath(jrxActualDoc, null, choiceParentXPaths[i]);
			Assert.assertNotNull(jrxChoiceParentElement);

			for (int j = 0; j < choiceElementIndexes.length; j++) {
				JrxChoiceGroup jrxChoiceElementGroup = (JrxChoiceGroup) jrxChoiceParentElement.getChildrenBlock()
				        .getElements().get(choiceElementIndexes[j]);
				int optionIndex = 0;
				if (j < optionIndexes.length) {
					optionIndex = optionIndexes[j];
				}
				JrxElement jrxNewElement = util.addChoiceElement(jrxChoiceElementGroup, optionIndex);

				actualDocString = util.convertDocumentToString(jrxActualDoc, true);
				log.debug("After add: " + choiceParentXPaths[i] + "-" + optionIndex + ": " + jrxNewElement.getName()
				        + "\n" + actualDocString);
			}
		}

		// Check against expected file
		url = getClass().getClassLoader().getResource(expectedXmlFile);
		xmlDoc = XmlUtils.openDocumentNS(url);

		JrxDocument jrxExpectedDoc = util.convertXmlToJrxModel(xmlDoc);
		jrxExpectedDoc = util.enrichJrxDocument(jrxExpectedDoc);
		String expectedDocString = util.convertDocumentToString(jrxExpectedDoc, true);
		log.debug("Output after enrichment:\n" + expectedDocString);

		Assert.assertEquals(expectedDocString, actualDocString);

		jrxExpectedDoc = util.removeChoiceHelperElement(jrxExpectedDoc);
		expectedDocString = util.convertDocumentToString(jrxExpectedDoc, true);
		log.debug("Output after helper removed:\n" + expectedDocString);
	}

	@Test
	public void validateDocumentSimple() throws Exception {
		validateDocument("input/simple1.xml", new String[] { "schema/aaachoice.001.001.01.xsd" });
	}

	@Test
	public void validateDocumentSimple2() throws Exception {
		validateDocument("input/setr.004.xml", new String[] { "schema/$ahV10.xsd", "schema/setr.004.001.03.xsd" });
	}

	@Test
	public void validateDocumentSimple3() throws Exception {
		validateDocument("input/setr.005.001.003.xml", new String[] { "schema/$ahV10.xsd",
		        "schema/setr.005.001.003.xsd" });
	}

	@Test
	public void validateWithJaxpSimple1() throws SAXException, IOException {
		validateWithJaxp("input/simple1.xml", "schema/aaachoice.001.001.01.xsd");
	}

	@Test
	public void validateWithJaxpSimple2() throws SAXException, IOException {
		validateWithJaxp("input/incomplete-setr.010.xml", "schema/setr.010.001.03.xsd");
	}

	public void validateWithJaxp(String inputFileName, String schemaFile) throws SAXException, IOException {
		// 1. Lookup a factory for the W3C XML Schema language
		SchemaFactory factory = SchemaFactory.newInstance("http://www.w3.org/2001/XMLSchema");

		// 2. Compile the schema.
		Schema schema = factory.newSchema(getClass().getClassLoader().getResource(schemaFile));

		CollectiveErrorHandler ceh = new CollectiveErrorHandler();

		// 3. Get a validator from the schema.
		Validator validator = schema.newValidator();
		validator.setErrorHandler(ceh);

		// 4. Parse the document you want to check.
		Source source = new StreamSource(getClass().getClassLoader().getResourceAsStream(inputFileName));

		Result result = null;
		// 5. Check the document
		try {
			validator.validate(source, result);
			if (ceh.errorMessages.size() > 0) {
				System.err.println("Following errors found in XML:");
				for (String[] strArray : ceh.errorMessages) {
					System.err.println("\t" + strArray[0] + " -> " + strArray[1]);
				}
			}
			if (ceh.warningMessages.size() > 0) {
				System.out.println("Following warning found in XML:");
				for (String[] strArray : ceh.warningMessages) {
					System.err.println("\t" + strArray[0] + " -> " + strArray[1]);
				}
			}
		} catch (SAXException ex) {
			System.err.println(inputFileName + " is not valid because ");
			System.err.println(ex.getMessage());
		}

	}

	private class CollectiveErrorHandler implements ErrorHandler {
		protected List<String[]> errorMessages = new ArrayList<String[]>();
		protected List<String[]> warningMessages = new ArrayList<String[]>();

		@Override
		public void error(SAXParseException e) throws SAXException {
			errorMessages.add(new String[] { e.getMessage(),
			        "Line: " + e.getLineNumber() + " Column: " + e.getColumnNumber() });
		}

		@Override
		public void fatalError(SAXParseException e) throws SAXException {
			errorMessages.add(new String[] { e.getMessage(),
			        "Line: " + e.getLineNumber() + " Column: " + e.getColumnNumber() });
		}

		@Override
		public void warning(SAXParseException e) throws SAXException {
			warningMessages.add(new String[] { e.getMessage(),
			        "Line: " + e.getLineNumber() + " Column: " + e.getColumnNumber() });
		}

	}

	@Test
	public void testEnrichment2() {
		String[] xpathArray = new String[] { "//Ah:AppHdr", "//Doc:Document", "SwInt:ExchangeRequest",
		        "/SwInt:ExchangeRequest", "//SwInt:ExchangeRequest", "//SwInt:Request", "//SwInt:RequestHeader",
		        "//SwInt:Request/SwInt:RequestHeader", "//SwInt:RequestControl",
		        "//SwInt:Request/SwInt:RequestControl", "//SwInt:DeliveryCtrl",
		        "//SwInt:DeliveryCtrl/Sw:DeliveryNotif", "//SwInt:DeliveryCtrl/Sw:OverdueWarningDelay",
		        "//SwInt:DeliveryCtrl/Sw:OverdueTime" };
		try {
			URL interActSchemaUrl = getClass().getClassLoader().getResource(SWIFT_INTERACT_XSD_URL);
			util.addSchema(interActSchemaUrl);

			URL ahSchemaUrl = getClass().getClassLoader().getResource("schema/$ahV10.xsd");
			util.addSchema(ahSchemaUrl);

			URL schemaUrl = getClass().getClassLoader().getResource("schema/setr.004.001.03.xsd");
			util.addSchema(schemaUrl);

			URL url = getClass().getResource("/input/xpath-failure.xml");
			Document xmlDoc = XmlUtils.openDocumentNS(url);

			log.debug("Initial XML document:\n" + XmlUtils.documentToString(xmlDoc));
			JrxDocument jrxDoc = util.convertXmlToJrxModel(xmlDoc);
			util.enrichJrxDocument(jrxDoc);

			log.debug("After enrichment XML:\n" + XmlUtils.documentToString(xmlDoc));

			// Re-sync from text to overcome XML DOM problem (a bug in XML DOM library?)
			String tempXmlString = XmlUtils.documentToString(jrxDoc.getXmlDocument());
			xmlDoc = XmlUtils.createDocumentNS(tempXmlString);
			jrxDoc = util.convertXmlToJrxModel(xmlDoc);

			log.debug("After enrichment XML from FMS document:\n" + XmlUtils.documentToString(jrxDoc.getXmlDocument()));

			String[][] namespaces = new String[][] { { "Ah", "urn:swift:xsd:$ahV10" },
			        { "Doc", "urn:swift:xsd:setr.004.001.03" }, { "SwInt", "urn:swift:snl:ns.SwInt" },
			        { "Sw", "urn:swift:snl:ns.Sw" }, { "SwGbl", "urn:swift:snl:ns.SwGbl" },
			        { "SwSec", "urn:swift:snl:ns.SwSec" } };

			jrxDoc.getXmlDocument().normalizeDocument();
			for (int i = 0; i < xpathArray.length; i++) {
				String xpath = xpathArray[i];
				Node targetNode = XmlUtils.getNodeByXPath(jrxDoc.getXmlDocument(), namespaces, xpath);
				System.out.println(xpath + " -> " + targetNode);
				Assert.assertNotNull(targetNode);
			}
		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail();
		}
	}

	private void validateDocument(String inputXmlFile, String[] schemaFiles) throws Exception {
		URL url = getClass().getClassLoader().getResource(inputXmlFile);
		Document xmlDoc = XmlUtils.openDocumentNS(url);

		URL interActSchemaUrl = getClass().getClassLoader().getResource(SWIFT_INTERACT_XSD_URL);
		util.addSchema(interActSchemaUrl);

		for (int i = 0; i < schemaFiles.length; i++) {
			URL schemaUrl = getClass().getClassLoader().getResource(schemaFiles[i]);
			util.addSchema(schemaUrl);
		}

		log.debug("Start");
		JrxDocument jrxDoc = util.convertXmlToJrxModel(xmlDoc);
		jrxDoc = util.enrichJrxDocument(jrxDoc);
		util.addChoiceHelperElement(jrxDoc);

		JrxElement jrxDocElement = null;
		if (util.getNamespaceUri("Doc") == null) {
			jrxDocElement = util.getElementByXPath(jrxDoc, null, "//Document");
		} else {
			jrxDocElement = util.getElementByXPath(jrxDoc, util.getNamespacePrefixes(), "//Doc:Document");
		}
		JrxElement jrxAppHdrElement = null;
		if (util.getNamespaceUri("Ah") == null) {
			jrxAppHdrElement = util.getElementByXPath(jrxDoc, null, "//AppHdr");
		} else {
			jrxAppHdrElement = util.getElementByXPath(jrxDoc, util.getNamespacePrefixes(), "//Ah:AppHdr");
		}

		util.enrichJrxElement(jrxAppHdrElement);
		util.enrichJrxElementRecursive(jrxAppHdrElement);
		util.addChoiceHelperElement(jrxAppHdrElement);
		util.enrichJrxElement(jrxDocElement);
		util.enrichJrxElementRecursive(jrxDocElement);
		util.addChoiceHelperElement(jrxDocElement);

		log.debug("End");
		List<XmlValidationError> validationResult = util.validateDocument(jrxDoc);
		if (validationResult.size() > 0) {
			System.err.println("Following errors found in XML:");
			for (XmlValidationError error : validationResult) {
				System.err.println("\t" + error.getDefaultMessage() + " -> " + error.getElement());
			}
		}

		validationResult = util.validateElement(jrxAppHdrElement, "//");
		if (validationResult != null && validationResult.size() > 0) {
			System.err.println("Following errors found in Application Header:");
			for (XmlValidationError error : validationResult) {
				System.err.println("\t" + error.getDefaultMessage() + " -> " + error.getElement());
			}
		}

		validationResult = util.validateElement(jrxDocElement, "//");
		if (validationResult != null && validationResult.size() > 0) {
			System.err.println("Following errors found in Document:");
			for (XmlValidationError error : validationResult) {
				System.err.println("\t" + error.getDefaultMessage() + " -> " + error.getElement());
			}
		}
	}

	@Test
	public void validateDocumentSimple4() throws Exception {
		validateDocument("input/incomplete-setr.010.xml", new String[] { "schema/$ahV10.xsd",
		        "schema/setr.010.001.03.xsd" });
	}

	@Test
	public void validateAmountWithCurrencyAttribute() throws Exception {
		validateDocument("data/validate-amount.xml", new String[] { "schema/$ahV10.xsd", "schema/setr.010.001.03.xsd" });
	}

	/**
	 * Choice within xs:complexContent
	 * 
	 * @throws Exception
	 */
	@Test
	public void enrichExistingChoiceSelection() throws Exception {
		enrichJrxDocumentWithoutExpectation("input/choice4.xml", "schema/choice4.xsd");
	}

	@Test
	public void enrichExistingChoiceSelection2() throws Exception {
		enrichJrxDocumentWithoutExpectation("expected/choice1.xml", "schema/aaachoice.001.001.01.xsd");
	}

	@Test
	public void enrichExistingChoiceSelection3() throws Exception {
		enrichJrxDocumentWithoutExpectation("expected/choice2.xml", "schema/aaachoice.001.001.01.xsd");
	}

	@Test
	public void enrichExistingChoiceSelection4() throws Exception {
		enrichJrxDocumentWithoutExpectation("expected/choicegroup1.xml", "schema/aaachoice.001.001.01.xsd");
	}

	@Test
	public void testConvertXmlDocumentWithExistingChoiceSelection1() throws Exception {
		convertXmlDocToJrxDoc("expected/choicegroup1.xml", "schema/aaachoice.001.001.01.xsd");
	}

	@Test
	public void getJrxAttributeByXpath() {
	    try {
            InputStream is = getClass().getResourceAsStream("/data/setr.010.xml");
            String[][] namespacesDef = new String[][] { { "Doc", "urn:swift:xsd:setr.010.001.03" } };
            Document xmlDoc = XmlUtils.createDocumentNS(is);
            
            JrxDocument jrxDoc = util.convertXmlToJrxModel(xmlDoc);
            
            List<JrxAttribute> jrxAttrList = util.getAttributeListByXPath(jrxDoc, namespacesDef,"//Doc:GrssAmt/@Ccy");
            for (JrxAttribute jrxAttr : jrxAttrList) {
                System.out.println(jrxAttr.getOwner().getName() + "@" + jrxAttr.getName());
            }
            
            util.enrichJrxDocument(jrxDoc);
            
            jrxAttrList = util.getAttributeListByXPath(jrxDoc, namespacesDef,"//Doc:TtlSttlmAmt/@Ccy");
            for (JrxAttribute jrxAttr : jrxAttrList) {
                System.out.println(jrxAttr.getOwner().getName() + "@" + jrxAttr.getName());
            }
        } catch (Exception e) {
            Assert.fail();
        }
	}
}
