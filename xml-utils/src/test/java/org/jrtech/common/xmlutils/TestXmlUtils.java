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

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.Charset;
import java.sql.Time;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import ch.qos.logback.core.joran.spi.XMLUtil;

import org.junit.Assert;

public class TestXmlUtils {
	private static final Logger log = LoggerFactory.getLogger(TestXmlUtils.class);

	static final String XML_TIME_PATTERN = "HH:mm:ss.SSSZ";

	private static final Charset DEFAULT_CHARSET = Charset.forName("UTF-8");

	private Calendar calendar;

	@Before
	public void init() {
		calendar = Calendar.getInstance();
		int offset = calendar.get(Calendar.ZONE_OFFSET) + calendar.get(Calendar.DST_OFFSET);
		log.debug("Offset: " + offset);
	}

	@Test
	public void testGetNodeByXPath() {
		try {
			URL url = getClass().getResource("/input/choice1.xml");
			Document xmlDoc = XmlUtils.openDocumentNS(url);
			Node targetNode = XmlUtils.getNodeByXPath(xmlDoc, new String[][] { { "doc",
					"urn:test:xsd:aaachoice.001.001.01" } }, "//doc:AcctSvcr");
			Assert.assertNotNull(targetNode);
		} catch (Exception e) {
			Assert.fail();
		}
	}

	@Test
	public void testGetNodeByXPathByInvalidNamespaceDoc() {
		String[] xpathArray = new String[] { "SwInt:HandleRequest", "/SwInt:HandleRequest", "//SwInt:RequestPayload",
				"SwInt:HandleRequest//SwInt:RequestPayload", "//SwInt:RequestHandle",
				"SwInt:HandleRequest/SwInt:RequestHandle/SwInt:RequestPayload", "HandleRequest", "/HandleRequest",
				"//RequestPayload", "HandleRequest//RequestPayload", "//RequestHandle",
				"HandleRequest/RequestHandle/RequestPayload" };
		try {
			URL url = getClass().getResource("/input/invalid-namespace1.xml");
			Document xmlDoc = XmlUtils.openDocument(url);
			for (int i = 0; i < xpathArray.length; i++) {
				String xpath = xpathArray[i];
				Node targetNode = XmlUtils.getNodeByXPath(xmlDoc, xpath);
				System.out.println(xpath + " -> " + targetNode);
				// Assert.assertNotNull(targetNode);
			}
		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail();
		}
	}

	// Test path navigation

	@Test
	public void testGetPath() {
		try {
			InputStream is = getClass().getResourceAsStream("/data/setr.010.xml");
			Document xmlDoc = XmlUtils.createDocumentNS(is);
			Node targetNode = XmlUtils.getNodeByXPath(xmlDoc, new String[][] { { "Doc", "urn:swift:xsd:setr.010.001.03" } },
					"//Doc:IndvOrdrDtls/Doc:GrssAmt");
			Node[] nodePath = XmlUtils.getPath(targetNode);
			for (Node node : nodePath) {
				System.out.println(node.getNodeName() + " (" + getNodeIndex(node) + ")");
			}
		} catch (Exception e) {
			Assert.fail();
		}
	}

	@Test
	public void testGetAttributeNode() {
		try {
			InputStream is = getClass().getResourceAsStream("/data/setr.010.xml");
			String[][] namespacesDef = new String[][] { { "Doc", "urn:swift:xsd:setr.010.001.03" } };
			Document xmlDoc = XmlUtils.createDocumentNS(is);

			Node targetNode = XmlUtils.getNodeByXPath(xmlDoc, namespacesDef, "//Doc:IndvOrdrDtls/Doc:GrssAmt/@Ccy");
			System.out.println(targetNode.getNodeName());
		} catch (Exception e) {
			Assert.fail();
		}
	}

	@Test
	public void testGetElementByPath() {
		String[] paths = new String[] {
			// @formatter:off
    			"/SwInt:ExchangeRequest/SwInt:Request/SwInt:RequestPayload/Doc:Document/Doc:SbcptOrdrV03/Doc:MltplOrdrDtls/Doc:IndvOrdrDtls/Doc:GrssAmt",
    			"/SwInt:ExchangeRequest/SwInt:Request/SwInt:RequestPayload/Doc:Document/Doc:SbcptOrdrV03/Doc:MltplOrdrDtls/Doc:IndvOrdrDtls/../*",
    			"/SwInt:ExchangeRequest/SwInt:Request/SwInt:RequestPayload/Doc:Document/Doc:SbcptOrdrV03/Doc:MltplOrdrDtls/../Doc:MsgId/Doc:Id",
    			"/SwInt:ExchangeRequest/SwInt:Request/SwInt:RequestPayload/Doc:Document/Doc:SbcptOrdrV03/Doc:MltplOrdrDtls/../Doc:MsgId/Doc:CreDtTm",
    		// @formatter:on
		};
		try {
			Document xmlDoc = XmlUtils.createDocument(getClass().getResourceAsStream("/data/setr.010.xml"));

			for (int i = 0; i < paths.length; i++) {
				if (paths[i].equals(""))
					continue;
				Element xmlElement = XmlUtils.getElementByPath(xmlDoc, paths[i]);
				if (xmlElement == null) {
					System.out.println("[NO ELEMENT FOUND] -> " + paths[i]);
				} else {
					System.out.println(xmlElement.getTagName() + " -> " + paths[i]);
					System.out.println("Text content: '" + xmlElement.getTextContent() + "'");
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail();
		}
	}

	private int getNodeIndex(Node node) {
		if (node == null || node instanceof Document) {
			return -1;
		}

		Node parentNode = node.getParentNode();

		NodeList nodeList = parentNode.getChildNodes();
		for (int i = 0; i < nodeList.getLength(); i++) {
			Node indexNode = nodeList.item(i);
			if (node.equals(indexNode)) {
				return i;
			}
		}

		return -1;
	}

	// Test XML Datetime, Date, and Time

	@Test
	public void testConvertDateToXmlValue() {
		log.debug(XmlUtils.convertDateToXmlValue(calendar.getTime()));
	}

	@Test
	public void testConvertDateTimeToXmlValue() {
		log.debug(XmlUtils.convertDateTimeToXmlValue(calendar.getTime()));
	}

	@Test
	public void testConvertTimeToXmlValue() {
		log.debug(XmlUtils.convertTimeToXmlValue(new Time(calendar.getTimeInMillis())));
	}

	@Test
	public void testConvertXmlValueToDate() throws ParseException {
		String text = XmlUtils.convertDateToXmlValue(calendar.getTime());
		log.debug("From: '" + text + "' to: '" + XmlUtils.convertXmlValueToDate(text) + "'");

		text = "2009-09-23";
		log.debug("From: '" + text + "' to: '" + XmlUtils.convertXmlValueToDate(text) + "'");
	}

	@Test
	public void testConvertXmlValueToDateTime() throws ParseException {
		String text = XmlUtils.convertDateTimeToXmlValue(calendar.getTime());
		log.debug("From: '" + text + "' to: '" + XmlUtils.convertXmlValueToDateTime(text) + "'");

		text = "2009-09-23T16:48:57.0Z";
		log.debug("From: '" + text + "' to: '" + XmlUtils.convertXmlValueToDateTime(text) + "'");
	}

	@Test
	public void testConvertXmlValueToTime() throws ParseException {
		calendar.setTime(new Date(System.currentTimeMillis()));
		String text = XmlUtils.convertTimeToXmlValue(new Time(calendar.getTimeInMillis()));
		log.debug("Input: " + text);
		Time time = convertXmlValueToTime(text);
		log.debug("" + time);
		SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss.SSSZ");
		log.debug(sdf.format(time));

		log.debug(XmlUtils.convertTimeToXmlValue(time));
	}

	public Time convertXmlValueToTime(String xmlFormattedTime) throws ParseException {
		String tzInfo = xmlFormattedTime.substring(xmlFormattedTime.length() - 6);
		String tzDiffSign = tzInfo.substring(0, 1);
		String tzDiffHour = tzInfo.substring(1, 3);
		String tzDiffMinute = tzInfo.substring(4);
		log.debug("sign: " + tzDiffSign + "; hour: " + tzDiffHour + "; minute: " + tzDiffMinute);

		String javaFormattedTime = xmlFormattedTime.substring(0, xmlFormattedTime.length() - 3)
				+ xmlFormattedTime.substring(xmlFormattedTime.length() - 2);
		log.debug("Java formatted text: " + javaFormattedTime);
		SimpleDateFormat sdf = new SimpleDateFormat(XML_TIME_PATTERN);
		Date date = sdf.parse(javaFormattedTime);
		log.debug("Date: " + date);
		Calendar cal = Calendar.getInstance();

		cal.setTime(new Date(System.currentTimeMillis()));
		cal.setTime(date);

		int offset = cal.get(Calendar.ZONE_OFFSET) + cal.get(Calendar.DST_OFFSET);
		log.debug("Offset: " + offset);
		Time time = new Time(cal.getTimeInMillis());

		return time;
	}

	@Test
	public void testPrettyFormatFromXmlDocument() throws Exception {
		String systemNewLine = "\n"; // System.getProperty("line.separator");

		String xmlStringInput = "<root><child>aaa</child><child/></root>";
		String formattedXmlString = XmlUtils.nodeToString(XmlUtils.createDocument(xmlStringInput), true);
		// formattedXmlString = formattedXmlString.replaceAll(systemNewLine, "\n");
		// formattedXmlString = formattedXmlString.replaceAll("\n", systemNewLine);
		formattedXmlString = XmlUtils.prettyFormatSAX(formattedXmlString, 4);
		String expectedOutput = "<root>" + systemNewLine + "    <child>aaa</child>" + systemNewLine + "    <child />"
				+ systemNewLine + "</root>";
		Assert.assertEquals(expectedOutput, formattedXmlString);

		xmlStringInput = "<Doc:root xmlns:Doc=\"urn:swift:xsd:camt.044.001.02\">" + systemNewLine
				+ "        <Doc:child>aaa</Doc:child><Doc:child/></Doc:root>";
		formattedXmlString = XmlUtils.nodeToString(XmlUtils.createDocument(xmlStringInput), true);
		formattedXmlString = XmlUtils.prettyFormatSAX(formattedXmlString, 2);
		expectedOutput = "<Doc:root xmlns:Doc=\"urn:swift:xsd:camt.044.001.02\">" + systemNewLine
				+ "  <Doc:child>aaa</Doc:child>" + systemNewLine + "  <Doc:child />" + systemNewLine + "</Doc:root>";
		Assert.assertEquals(expectedOutput, formattedXmlString);
	}

	@Test
	public void benchmarkXmlPrettyFormatter() {
		String[] inputs = new String[] { "/data/setr.004.xml", "/data/setr.010.xml" };
		String[] inputStrings = new String[inputs.length];
		int count = 10;
		int typeCount = 6;
		Long[][] sw = new Long[count][typeCount];
		try {
			for (int i = 0; i < count; i++) {
				int inputIndex = i % inputs.length;
				String inputString = inputStrings[inputIndex];
				if (inputString == null) {
					InputStream is = getClass().getResourceAsStream(inputs[inputIndex]);
					inputString = IOUtils.toString(is, DEFAULT_CHARSET);
				}

				// OLD pretty formatter -> Omit processing instructions
				long startTime = System.nanoTime();
				XmlUtils.prettyFormat(inputString, 4, true);
				long endTime = System.nanoTime();
				sw[i][0] = endTime - startTime;

				// OLD pretty formatter -> With processing instructions
				startTime = System.nanoTime();
				XmlUtils.prettyFormat(inputString, 4, false);
				endTime = System.nanoTime();
				sw[i][1] = endTime - startTime;

				// NEW pretty formatter -> Omit processing instructions
				startTime = System.nanoTime();
				XmlUtils.prettyFormatSAX(inputString, 4, true);
				endTime = System.nanoTime();
				sw[i][2] = endTime - startTime;

				// NEW pretty formatter -> With processing instructions
				startTime = System.nanoTime();
				XmlUtils.prettyFormatSAX(inputString, 4, false);
				endTime = System.nanoTime();
				sw[i][3] = endTime - startTime;

				// NEW pretty formatter 2 -> Omit processing instructions
				InputStream is = IOUtils.toInputStream(inputString, DEFAULT_CHARSET);
				startTime = System.nanoTime();
				XmlUtils.prettyFormatSAX(is, 4, true);
				endTime = System.nanoTime();
				sw[i][4] = endTime - startTime;

				// NEW pretty formatter 2 -> With processing instructions
				is.reset();
				startTime = System.nanoTime();
				XmlUtils.prettyFormatSAX(is, 4, false);
				endTime = System.nanoTime();
				sw[i][5] = endTime - startTime;
				if (is != null)
					try {
						is.close();
					} catch (IOException e) {
					}
				;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		// print statistics
		Long[] sumTimes = new Long[typeCount];
		for (int i = 0; i < sw.length; i++) {
			for (int j = 0; j < typeCount; j++) {
				if (sumTimes[j] == null) {
					sumTimes[j] = 0L;
				}
				sumTimes[j] += sw[i][j];
			}
		}
		NumberFormat nf = NumberFormat.getNumberInstance(Locale.ENGLISH);
		System.out.println("Count: " + nf.format(count));
		for (int i = 0; i < sumTimes.length; i++) {
			System.out.println("Total times #" + i + ": " + StringUtils.leftPad(nf.format(sumTimes[i]), 18) + " ns.");
			System.out.println("Avg. times  #" + i + ": " + StringUtils.leftPad(nf.format(sumTimes[i] / count), 18) + " ns.");
		}
	}

	@Test
	public void testPrettyFormatFromXmlString() throws ParserConfigurationException, SAXException, IOException {
		String systemNewLine = "\n"; // System.getProperty("line.separator");
		String xmlStringInput = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><root><child>aaa</child><child/></root>";

		String formattedXmlString = XmlUtils.prettyFormatSAX(xmlStringInput, 2);
		String expectedOutput = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<root>" + systemNewLine
				+ "  <child>aaa</child>" + systemNewLine + "  <child />" + systemNewLine + "</root>";
		System.out.println("expectedOutput:\n" + expectedOutput + "\n" + "formattedXmlString:\n" + formattedXmlString);
		Assert.assertEquals(expectedOutput, formattedXmlString);

		System.out.println();
		xmlStringInput = "<Doc:root xmlns:Doc=\"urn:swift:xsd:camt.044.001.02\">" + systemNewLine
				+ "        <Doc:child>aaa</Doc:child><Doc:child/></Doc:root>";
		formattedXmlString = XmlUtils.prettyFormatSAX(xmlStringInput, 2);
		expectedOutput = "<Doc:root xmlns:Doc=\"urn:swift:xsd:camt.044.001.02\">" + systemNewLine
				+ "  <Doc:child>aaa</Doc:child>" + systemNewLine + "  <Doc:child />" + systemNewLine + "</Doc:root>";
		System.out.println("expectedOutput:\n" + expectedOutput + "\n" + "formattedXmlString:\n" + formattedXmlString);
		Assert.assertEquals(expectedOutput, formattedXmlString);

		System.out.println();
		xmlStringInput = "<root><child>aaa</child><child/></root>";
		formattedXmlString = XmlUtils.prettyFormatSAX(xmlStringInput, 2);
		expectedOutput = "<root>" + systemNewLine + "  <child>aaa</child>" + systemNewLine + "  <child />"
				+ systemNewLine + "</root>";
		System.out.println("expectedOutput:\n" + expectedOutput + "\n" + "formattedXmlString:\n" + formattedXmlString);
		Assert.assertEquals(expectedOutput, formattedXmlString);
	}

	@Test
	public void testRemoveEmptyElements() throws Exception {
		removeEmptyElements("/input/doc-with-empty-elements-simple.xml", "/expected/doc-with-empty-elements-simple.xml");
		removeEmptyElements("/input/doc-with-empty-elements.xml", "/expected/doc-with-empty-elements.xml");
	}

	private void removeEmptyElements(String inputFile, String expectedFile) throws Exception {

		Document xmlDoc = XmlUtils.createDocument(getClass().getResourceAsStream(inputFile));
		XmlUtils.removeEmptyElements(xmlDoc);
		String actualXmlString = XmlUtils.documentToString(xmlDoc, true);
		xmlDoc = XmlUtils.createDocument(getClass().getResourceAsStream(expectedFile));
		String expectedXmlString = XmlUtils.documentToString(xmlDoc, true);
		Assert.assertEquals(expectedXmlString, actualXmlString);
	}

	@Test
	public void testDocumentWithoutNamespaceCreationAndQuery() throws Exception {
		String namespaceUri = "urn:acme:test:xsd";
		String namespacePrefix = "prefix";
		String initialXmlString = "<root xmlns=\"" + namespaceUri + "\"><book /></root>";

		Document xmlDoc = XmlUtils.createDocument(initialXmlString);
		String[][] namespace = new String[][] { { namespacePrefix, namespaceUri } };
		Element xmlBookElement = (Element) XmlUtils.getNodeByXPath(xmlDoc, namespace, "//" + namespacePrefix + ":book");
		Assert.assertNull(xmlBookElement);

		xmlBookElement = (Element) XmlUtils.getNodeByXPath(xmlDoc, "//book");
		Assert.assertNotNull(xmlBookElement);

		Element xmlTitleElement = XmlUtils.createSubElement(xmlBookElement, "title");
		xmlTitleElement.setTextContent("my title");
		System.out.println(XmlUtils.documentToString(xmlDoc, true));
	}

// @formatter:off
//    @Test
//    public void testDocumentWithoutNamespaceCreationAndQuery2() throws Exception {
//    	String namespaceUri = "urn:acme:test:xsd";
//    	String namespacePrefix = "prefix";
//    	String initialXmlString = "<" + namespacePrefix + ":root xmlns:" + namespacePrefix + "=\"" + namespaceUri + "\"><" + namespacePrefix + ":book /></" + namespacePrefix + ":root>";
//    	
//    	Document xmlDoc = XmlUtils.createDocument(initialXmlString);
//    	String[][] namespace = new String[][] {{namespacePrefix, namespaceUri}};
//    	Element xmlBookElement = (Element) XmlUtils.getNodeByXPath(xmlDoc, namespace, "//" + namespacePrefix + ":book");
//    	Assert.assertNull(xmlBookElement);
//    	System.out.println(XmlUtils.documentToString(xmlDoc, true));
//    	
//    	xmlBookElement = (Element) XmlUtils.getNodeByXPath(xmlDoc, "//" + namespacePrefix + ":book");
//    	Assert.assertNotNull(xmlBookElement);
//    	
//    	Element xmlTitleElement = XmlUtils.createSubElement(xmlBookElement, namespacePrefix + ":title");
//    	xmlTitleElement.setTextContent("my title");
//    	System.out.println(XmlUtils.documentToString(xmlDoc, true));
//    }
// @formatter:on

	@Test
	public void testDocumentWithNamespaceCreationAndQuery() throws Exception {
		String namespaceUri = "urn:acme:test:xsd";
		String namespacePrefix = "prefix";
		String initialXmlString = "<root xmlns=\"" + namespaceUri + "\"><book /></root>";

		Document xmlDoc = XmlUtils.createDocumentNS(initialXmlString);
		Element xmlBookElement = (Element) XmlUtils.getNodeByXPath(xmlDoc, "//book");
		Assert.assertNull(xmlBookElement);

		String[][] namespace = new String[][] { { namespacePrefix, namespaceUri } };
		xmlBookElement = (Element) XmlUtils.getNodeByXPath(xmlDoc, namespace, "//" + namespacePrefix + ":book");
		Assert.assertNotNull(xmlBookElement);

		Element xmlTitleElement = XmlUtils.createSubElement(xmlBookElement, "title");
		xmlTitleElement.setTextContent("my title");

		System.out.println(XmlUtils.documentToString(xmlDoc, true));
	}

	@Test
	public void testDocumentWithNamespaceCreationAndQuery2() throws Exception {
		String namespaceUri = "urn:acme:test:xsd";
		String namespacePrefix = "prefix";
		String initialXmlString = "<" + namespacePrefix + ":root xmlns:" + namespacePrefix + "=\"" + namespaceUri
				+ "\"><" + namespacePrefix + ":book /></" + namespacePrefix + ":root>";

		Document xmlDoc = XmlUtils.createDocumentNS(initialXmlString);
		Element xmlBookElement = (Element) XmlUtils.getNodeByXPath(xmlDoc, "//" + namespacePrefix + ":book");
		Assert.assertNull(xmlBookElement);

		String[][] namespace = new String[][] { { namespacePrefix, namespaceUri } };
		xmlBookElement = (Element) XmlUtils.getNodeByXPath(xmlDoc, namespace, "//" + namespacePrefix + ":book");
		Assert.assertNotNull(xmlBookElement);

		Element xmlTitleElement = XmlUtils.createSubElement(xmlBookElement, "title");
		xmlTitleElement.setTextContent("my title");

		System.out.println(XmlUtils.documentToString(xmlDoc, true));
	}

	@Test
	public void testNamespacePrefix() throws Exception {
		// Without prefix
		String namespaceUri = "urn:jrtech:test:xsd";
		String initialXmlString = "<root xmlns=\"" + namespaceUri + "\"><book /></root>";
		Document xmlDoc = XmlUtils.createDocumentNS(initialXmlString);
		Element xmlRootElement = xmlDoc.getDocumentElement();
		Assert.assertNull(xmlRootElement.getPrefix());

		// With prefix
		String namespacePrefix = "prefix";
		initialXmlString = "<" + namespacePrefix + ":root xmlns:" + namespacePrefix + "=\"" + namespaceUri + "\"><"
				+ namespacePrefix + ":book /></" + namespacePrefix + ":root>";
		xmlDoc = XmlUtils.createDocumentNS(initialXmlString);
		xmlRootElement = xmlDoc.getDocumentElement();
		Assert.assertEquals(xmlRootElement.getPrefix(), namespacePrefix);
		Element xmlBookElement = XmlUtils.getChildBySimpleTagName(xmlRootElement, "book");
		Assert.assertEquals(xmlBookElement.getPrefix(), namespacePrefix);

		// Multi Namespaces
		// Without prefix
		String namespaceUri2 = "urn:jrtech2:test:xsd";
		initialXmlString = "<root xmlns=\"" + namespaceUri + "\"><book xmlns=\"" + namespaceUri2 + "\"/></root>";
		xmlDoc = XmlUtils.createDocumentNS(initialXmlString);
		xmlRootElement = xmlDoc.getDocumentElement();
		Assert.assertNull(xmlRootElement.getPrefix());
		Assert.assertEquals(xmlRootElement.getNamespaceURI(), namespaceUri);
		xmlBookElement = XmlUtils.getChildBySimpleTagName(xmlRootElement, "book");
		Assert.assertNull(xmlBookElement.getPrefix());
		Assert.assertEquals(xmlBookElement.getNamespaceURI(), namespaceUri2);

		// With prefix
		String namespacePrefix1 = "pre1";
		String namespacePrefix2 = "pre2";
		initialXmlString = "<" + namespacePrefix1 + ":root xmlns:" + namespacePrefix1 + "=\"" + namespaceUri + "\"><"
				+ namespacePrefix2 + ":book xmlns:" + namespacePrefix2 + "=\"" + namespaceUri2 + "\" /></" + namespacePrefix1
				+ ":root>";
		xmlDoc = XmlUtils.createDocumentNS(initialXmlString);
		xmlRootElement = xmlDoc.getDocumentElement();
		xmlRootElement = xmlDoc.getDocumentElement();
		Assert.assertEquals(xmlRootElement.getPrefix(), namespacePrefix1);
		Assert.assertEquals(xmlRootElement.getNamespaceURI(), namespaceUri);
		xmlBookElement = XmlUtils.getChildBySimpleTagName(xmlRootElement, "book");
		Assert.assertEquals(xmlBookElement.getPrefix(), namespacePrefix2);
		Assert.assertEquals(xmlBookElement.getNamespaceURI(), namespaceUri2);
	}

	@Test
	public void testLinearize() {
		String xmlBefore = "<FIToFICstmrCdtTrf>\n  <GrpHdr>\n      <MsgId>JRTECH15061600349</MsgId>\n";
		String xmlAfter = "<FIToFICstmrCdtTrf><GrpHdr><MsgId>JRTECH15061600349</MsgId>";
		String result = XmlUtils.linearize(xmlBefore);
		Assert.assertEquals(xmlAfter, result);
	}
}
