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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.URL;
import java.nio.charset.Charset;
import java.sql.Time;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import javax.xml.XMLConstants;
import javax.xml.namespace.NamespaceContext;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.io.IOUtils;
import org.jrtech.common.utils.ArrayUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Attr;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

/**
 * The class <code>XMLUtil</code> provides simple functions for XML processing.
 */
public class XmlUtils {

    private static Logger log = LoggerFactory.getLogger(XmlUtils.class);

    public static final Charset UTF8_CHARSET = Charset.forName("UTF-8");

    static final String JAXP_SCHEMA_LANGUAGE = "http://java.sun.com/xml/jaxp/properties/schemaLanguage";

    static final String XML_DATE_PATTERN = "yyyy-MM-ddZ";

    static final String XML_NO_TZ_DATE_PATTERN = "yyyy-MM-dd";

    static final String XML_DATETIME_PATTERN = "yyyy-MM-dd'T'HH:mm:ss.SSSZ";

    static final String XML_TIME_PATTERN = "HH:mm:ss.SSSZ";

    public static final String XML_ATTRIBUTE_ID = "id";

    public static final String XML_ATTRIBUTE_NAME = "name";

    private static final String XML_LINARIZATION_REGEX = "(>|&gt;){1,1}(\\t)*(\\n|\\r)+(\\s)*(<|&lt;){1,1}";

    private static final String XML_LINARIZATION_REPLACEMENT = "$1$5";

    private static Class<?> tempClass = null;

    /*****************************
     * Document handling methods *
     *****************************/

    /**
     * The method <code>openDocument(String)</code> is used to open a XML file.
     * <p>
     * Use this method to open a XML file and get a Document. Note: The
     * implementation first tries the filesystem and then
     * FileUtil.getRelativeInputStream()
     * 
     * @param filename
     *            the name of the xml file
     * @return Document the DOM containing the xml structure
     * @exception Exception
     */
    public static Document openDocument(String filename) throws Exception {
        if (log.isDebugEnabled()) {
            log.debug("started ...");
            log.debug("... filename -> " + filename);
        }
        Document dom = null;
        File file = new File(filename);
        if (file.exists()) {
            if (log.isDebugEnabled())
                log.debug("... file exists -> using filesystem ...");
            dom = createDocument(new FileInputStream(file));
        } else {
            if (log.isDebugEnabled())
                log.debug("... file does not exist -> trying FileUtil.getRelativeInputStream() ...");
            dom = createDocument(getRelativeInputStream(filename));
        }
        if (log.isDebugEnabled())
            log.debug("finished.");
        return dom;
    }

    /**
     * The method <code>openDocument(String)</code> is used to open a XML file
     * that is namespace aware.
     * <p>
     * Use this method to open a XML file and get a Document. Note: The
     * implementation first tries the filesystem and then
     * FileUtil.getRelativeInputStream()
     * 
     * @param filename
     *            the name of the xml file
     * @return Document the DOM containing the xml structure
     * @exception Exception
     */
    public static Document openDocumentNS(String filename) throws Exception {
        if (log.isDebugEnabled()) {
            log.debug("started ...");
            log.debug("... filename -> " + filename);
        }
        Document dom = null;
        File file = new File(filename);
        if (file.exists()) {
            if (log.isDebugEnabled())
                log.debug("... file exists -> using filesystem ...");
            dom = createDocumentNS(new FileInputStream(file));
        } else {
            if (log.isDebugEnabled())
                log.debug("... file does not exist -> trying FileUtil.getRelativeInputStream() ...");
            dom = createDocumentNS(getRelativeInputStream(filename));
        }
        if (log.isDebugEnabled())
            log.debug("finished.");
        return dom;
    }

    /**
     * The method <code>openDocument(URL)</code> is used to open a XML file.
     * <p>
     * Use this method to open a XML file and get a Document.
     * 
     * @param url
     *            the url of the xml file
     * @return Document the DOM containing the xml structure
     * @exception Exception
     */
    public static Document openDocument(URL url) throws Exception {
        if (log.isDebugEnabled()) {
            log.debug("started ...");
            log.debug("... url -> " + url);
        }
        Document dom = createDocument(url.openStream());
        if (log.isDebugEnabled())
            log.debug("finished.");
        return dom;
    }

    /**
     * The method <code>openDocumentNS(URL)</code> is used to open a XML file
     * that is namespace aware.
     * <p>
     * Use this method to open a XML file and get a Document.
     * 
     * @param url
     *            the url of the xml file
     * @return Document the DOM containing the xml structure
     * @exception Exception
     */
    public static Document openDocumentNS(URL url) throws Exception {
        if (log.isDebugEnabled()) {
            log.debug("started ...");
            log.debug("... url -> " + url);
        }
        Document dom = createDocumentNS(url.openStream());
        if (log.isDebugEnabled())
            log.debug("finished.");
        return dom;
    }

    /**
     * The method <code>createDocument</code> is used to create a Document from
     * a String.
     * <p>
     * Use this method to create a Document from a well-formed xml string.
     * 
     * @param xmlString
     *            string containing the well-formed xml structure
     * @return Document the DOM containing the xml structure
     * @exception Exception
     */
    public static Document createDocument(String xmlString) throws Exception {
        if (log.isDebugEnabled()) {
            log.debug("started ...");
            log.debug("... xmlString -> " + xmlString);
            log.debug("finished.");
        }

        Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder()
                .parse(new InputSource(new StringReader(xmlString)));

        if (log.isDebugEnabled())
            log.debug("finished.");

        return document;
    }

    /**
     * The method <code>createDocumentNS</code> is used to create a Document
     * from a String that is namespace aware.
     * <p>
     * Use this method to create a Document from a well-formed xml string.
     * 
     * @param xmlString
     *            string containing the well-formed xml structure
     * 
     * @return Document the DOM containing the xml structure
     * 
     * @exception Exception
     */
    public static Document createDocumentNS(String xmlString) throws Exception {
        return createDocumentNS(xmlString, null);
    }

    /**
     * The method <code>createDocumentNS</code> is used to create a Document
     * from a String that is namespace aware.
     * <p>
     * Use this method to create a Document from a well-formed xml string.
     * 
     * @param xmlString
     *            string containing the well-formed xml structure
     * 
     * @param errorHandler
     *            custom error handler
     * @return Document the DOM containing the xml structure
     * @throws Exception
     */
    public static Document createDocumentNS(String xmlString, ErrorHandler errorHandler) throws Exception {
        if (log.isDebugEnabled()) {
            log.debug("started ...");
            log.debug("... xmlString -> " + xmlString);
        }
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        // enable namespaces
        factory.setNamespaceAware(true);
        DocumentBuilder docBuilder = factory.newDocumentBuilder();
        if (errorHandler != null) {
            docBuilder.setErrorHandler(errorHandler);
        }
        Document document = docBuilder.parse(new InputSource(new StringReader(xmlString)));
        if (log.isDebugEnabled())
            log.debug("finished.");
        return document;
    }

    public static String removeDocType(StringBuffer doc) {
        int b = doc.indexOf("<!DOCTYPE");
        if (b == -1)
            return doc.toString();
        int e = doc.indexOf(">", b) + 1;
        doc.replace(b, e, "");
        return doc.toString();
    }

    public static String removeVersion(StringBuffer doc) {
        int b = doc.indexOf("<?xml");
        if (b == -1)
            return doc.toString();
        int e = doc.indexOf(">", b) + 1;
        doc.replace(b, e, "");
        return doc.toString();
    }

    /**
     * The method <code>createDocument</code> is used to create a Document from
     * an InputStream.
     * <p>
     * Use this method to create a Document from an InputStream containing a
     * well-formed xml structure.
     * 
     * @param xmlStream
     *            stream containing the well-formed xml structure
     * @return Document the DOM containing the xml structure
     * @exception Exception
     */
    public static Document createDocument(InputStream xmlStream) throws Exception {
        log.debug("started ...");
        Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(xmlStream);
        xmlStream.close();
        log.debug("finished.");
        return document;
    }

    /**
     * The method <code>createDocumentNS</code> is used to create a Document
     * from an InputStream that is namespace aware.
     * <p>
     * Use this method to create a Document from an InputStream containing a
     * well-formed xml structure.
     * 
     * @param xmlStream
     *            stream containing the well-formed xml structure
     * @return Document the DOM containing the xml structure
     * @exception Exception
     */
    public static Document createDocumentNS(InputStream xmlStream) throws Exception {
        log.debug("started ...");
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        // enable namespaces
        factory.setNamespaceAware(true);
        Document document = factory.newDocumentBuilder().parse(xmlStream);
        xmlStream.close();
        log.debug("finished.");
        return document;
    }

    /*
     * public static boolean isValidXML(Source xmlFile, String xsd) { try {
     * SchemaFactory factory =
     * SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
     * 
     * URL u = FileUtils.class.getResource(xsd); Schema schema =
     * factory.newSchema(u);
     * 
     * Validator validator = schema.newValidator();
     * 
     * validator.validate(xmlFile); return true; } catch (Exception ex) { return
     * false; } }
     */

    /**
     * The method <code>createValidatedDocument</code> is used to create a
     * Document from an InputStream and validate it given a set of XML schemata.
     * <p>
     * Use this method to create a Document from an InputStream containing a
     * well-formed xml structure.
     * 
     * @param xmlSource
     *            InputSource containing the well-formed xml structure
     * @param entityMap
     *            Map containing the schemata and where to find them
     * @return Document the DOM containing the xml structure
     * @exception Exception
     */
    public static Document createValidatedDocument(InputSource xmlSource, Map<String, String> entityMap)
            throws SAXException, IOException, ParserConfigurationException {
        log.debug("started ...");

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

        // enable namespaces
        factory.setNamespaceAware(true);
        // enable validation
        factory.setValidating(true);

        // this might throw an IllegalArgumentException if
        // the parser does not support JAXP 1.2
        factory.setAttribute(JAXP_SCHEMA_LANGUAGE, XMLConstants.W3C_XML_SCHEMA_NS_URI);

        // Parsers from this factory will automatically validate against the
        // associated schema
        DocumentBuilder builder = factory.newDocumentBuilder();
        SAXParserHandler handler = new SAXParserHandler();

        // vector of schemata for validation
        handler.setEntityMap(entityMap);
        builder.setEntityResolver(handler);
        builder.setErrorHandler(handler);

        Document document = null;

        // this will throw a SAXException, if validation fails
        document = builder.parse(xmlSource);

        log.debug("finished.");
        return document;
    }

    /**
     * The method <code>newDocument</code> is used to create an empty Document.
     * <p>
     * Use this method to create an empty Document.
     * 
     * @return Document the empty DOM
     * @exception Exception
     */
    public static Document newDocument() throws Exception {
        return DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
    }

    /**
     * The method <code>newDocument</code> is used to create an empty Document
     * with root element complete with namespace information.
     * <p>
     * Use this method to create an empty Document with namespace aware.
     * 
     * @param simpleRootElement
     *            [prefix:tagname]
     * @param namespacePrefix
     *            e.g. Doc and SwInt
     * @param namespaceURI
     *            e.g. urn:swift:xsd:setr.004.001.03
     * @return Document the empty DOM with default root element
     * @throws ParserConfigurationException
     */
    public static Document newDocumentNS(String simpleRootElement, String namespacePrefix, String namespaceURI)
            throws ParserConfigurationException {
        String qualifiedRootName = simpleRootElement;
        String namespaceAttribute = XMLConstants.XMLNS_ATTRIBUTE;
        if (namespacePrefix != null && !namespacePrefix.equals("")) {
            qualifiedRootName = namespacePrefix + ":" + simpleRootElement;
            namespaceAttribute += ":" + namespacePrefix;
        }

        // create the xml document builder factory object
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

        // set the factory to be namespace aware
        factory.setNamespaceAware(true);

        // create the xml document builder object
        // and get the DOMImplementation object
        DocumentBuilder builder = factory.newDocumentBuilder();
        DOMImplementation domImpl = builder.getDOMImplementation();

        // create a document with the default namespace
        // and a root node
        Document xmlDoc = domImpl.createDocument(namespaceURI, qualifiedRootName, null);
        Element xmlRootElement = xmlDoc.getDocumentElement();
        xmlRootElement.setAttributeNS(XMLConstants.XMLNS_ATTRIBUTE_NS_URI, namespaceAttribute, namespaceURI);

        return xmlDoc;
    }

    /******************
     * Output methods *
     ******************/

    public static String nodeToString(Node node) throws TransformerException, IOException {
        return nodeToString(node, false);
    }

    public static String nodeToString(Node node, boolean indent) throws TransformerException, IOException {
        return nodeToString(node, indent, true);
    }

    public static String nodeToString(Node node, boolean indent, boolean omitXmlDeclaration)
            throws TransformerException, IOException {
        StringWriter writer = new StringWriter();
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        transformer.setOutputProperty(OutputKeys.METHOD, "xml");
        transformer.setOutputProperty(OutputKeys.INDENT, indent ? "yes" : "no");
        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, omitXmlDeclaration ? "yes" : "no");
        transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
        if (indent)
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");

        DOMSource source = new DOMSource(node);
        StreamResult result = new StreamResult(writer);
        transformer.transform(source, result);
        writer.close();
        String detail = writer.toString();
        detail = detail.replaceAll("(([\n]|[\r]|[\r\n])([\t]*|[ ]*))+([\n]|[\r]|[\r\n])", "\n");
        return detail;
    }

    public static String prettyFormatSAX(String input) throws ParserConfigurationException, SAXException, IOException {
        return prettyFormatSAX(input, 4, false);
    }

    public static String prettyFormatSAX(String input, int indent)
            throws ParserConfigurationException, SAXException, IOException {
        return prettyFormatSAX(input, indent, false);
    }

    public static String prettyFormatSAX(String input, int indent, boolean omitProcessingInstruction)
            throws ParserConfigurationException, SAXException, IOException {
        String prettifiedXml = prettyFormatSAX(IOUtils.toInputStream(input, UTF8_CHARSET), indent, true);
        if (!omitProcessingInstruction) {
            prettifiedXml = insertProcessingInstructions(input, prettifiedXml);
        }

        return prettifiedXml;
    }

    public static String prettyFormatSAX(InputStream inputStream, int indent, boolean omitProcessingInstruction)
            throws ParserConfigurationException, SAXException, IOException {
        if (inputStream == null)
            return null;

        // parse with SAX parser and prettifier handler
        SAXParserFactory factory = SAXParserFactory.newInstance();
        SAXParser parser = factory.newSAXParser();
        SAXPrettyPrinterHandler handler = new SAXPrettyPrinterHandler(indent);
        parser.parse(inputStream, handler);

        String prettifiedXml = handler.getFormattedXml();
        if (!omitProcessingInstruction) {
            prettifiedXml = insertProcessingInstructions(inputStream, prettifiedXml);
        }

        if (inputStream != null)
            try {
                inputStream.close();
            } catch (IOException e) {
            }

        return prettifiedXml;
    }

    private static String insertProcessingInstructions(String sourceXmlString, String targetXmlString) {
        List<String> processingInstructionList = new ArrayList<String>();
        // Collect processing instruction(s)
        int startPos = sourceXmlString.indexOf("<?");
        while (startPos >= 0) {
            int endPos = sourceXmlString.indexOf("?>", startPos + 2);
            if (endPos > (startPos + 2)) {
                // Valid processing instruction
                processingInstructionList.add(sourceXmlString.substring(startPos + 2, endPos));
                startPos = sourceXmlString.indexOf("<?", endPos);
            } else {
                break;
            }
        }

        StringBuffer sb = new StringBuffer();
        for (String pi : processingInstructionList) {
            sb.append("<?").append(pi).append("?>\n");
        }
        return sb.toString() + targetXmlString;
    }

    private static String insertProcessingInstructions(InputStream sourceXmlStream, String targetXmlString) {
        List<String> processingInstructionList = new ArrayList<String>();
        try {
            sourceXmlStream.reset();
            InputStreamReader reader = new InputStreamReader(sourceXmlStream);
            char[] charBuff = new char[4096]; // 4KB buffer
            reader.read(charBuff);
            char[] openingChars = new char[] { '<', '?' };
            char[] closingChars = new char[] { '<', '?' };
            // Collect processing instruction(s)
            int startPos = ArrayUtil.search(charBuff, openingChars, 0);
            while (startPos >= 0) {
                int contentPos = startPos + 2;
                int endPos = ArrayUtil.search(charBuff, closingChars, contentPos);
                if (endPos > contentPos) {
                    // Valid processing instruction
                    processingInstructionList.add(Arrays.toString(Arrays.copyOfRange(charBuff, contentPos, endPos)));
                    startPos = ArrayUtil.search(charBuff, openingChars, endPos + 2);
                } else {
                    break;
                }
            }

            StringBuffer sb = new StringBuffer();
            for (String pi : processingInstructionList) {
                sb.append("<?").append(pi).append("?>\n");
            }
            return sb.toString() + targetXmlString;
        } catch (IOException e) {
            try {
                sourceXmlStream.reset();
                log.error("Fail to insert processing instructions!" + IOUtils.toString(sourceXmlStream, UTF8_CHARSET),
                        e);
            } catch (IOException e1) {
                log.error("Fail to insert processing instructions!", e1);
            }
        }
        return targetXmlString;
    }

    public static String prettyFormat(String input) throws TransformerException {
        return prettyFormat(input, 2, true);
    }

    public static String prettyFormat(String input, int indent) throws TransformerException {
        return prettyFormat(input, indent, true);
    }

    public static String prettyFormat(String input, int indent, boolean omitXmlDeclaration)
            throws TransformerException {
        input = input.replaceAll("[\\s]*<", "<");
        Source xmlInput = new StreamSource(new StringReader(input));

        StringWriter stringWriter = new StringWriter();
        StreamResult xmlOutput = new StreamResult(stringWriter);

        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        try {
            transformerFactory.setAttribute("indent-number", indent);
        } catch (Exception e) {
            // Oracle 6519088 : (prefs) Preferences API depends on indentation
            // support of underlying XML transformer
        }

        Transformer transformer = transformerFactory.newTransformer();
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, omitXmlDeclaration ? "yes" : "no");
        transformer.setOutputProperty(OutputKeys.METHOD, "xml");
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", Integer.toString(indent));

        transformer.transform(xmlInput, xmlOutput);
        return xmlOutput.getWriter().toString();
    }

    /**
     * This method could be used to quote characters that are not supported in a
     * specific encoding. For example it is possible to encode unicode
     * characters in a non unicode encoding (e.g. UTF-8 to ISO-8859-1). The
     * unicode characters are quoted like: &#1042;
     * 
     * @param xml
     * @param encoding
     * @param omitDeclaration
     * @return
     * @throws FileNotFoundException
     * @throws TransformerException
     */
    public static String quoteEncoding(String xml, String encoding, boolean omitDeclaration)
            throws TransformerException {
        Transformer transformer = TransformerFactory.newInstance().newTransformer();
        transformer.setOutputProperty(OutputKeys.STANDALONE, "yes");
        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, omitDeclaration ? "yes" : "no");
        transformer.setOutputProperty(OutputKeys.ENCODING, encoding);
        StringWriter writer = new StringWriter();
        transformer.transform(new StreamSource(new StringReader(xml)), new StreamResult(writer));
        return writer.toString();
    }

    public static CharSequence quoteXML(CharSequence s) {
        StringBuffer result = null;
        for (int i = 0, max = s.length(), delta = 0; i < max; i++) {
            char c = s.charAt(i);
            String replacement = null;

            if (c == '&') {
                replacement = "&amp;";
            } else if (c == '<') {
                replacement = "&lt;";
            } else if (c == '\r') {
                replacement = "&#13;";
            } else if (c == '>') {
                replacement = "&gt;";
            } else if (c == '"') {
                replacement = "&quot;";
            } else if (c == '\'') {
                replacement = "&apos;";
            }

            if (replacement != null) {
                if (result == null) {
                    result = new StringBuffer(s);
                }
                result.replace(i + delta, i + delta + 1, replacement);
                delta += (replacement.length() - 1);
            }
        }
        if (result == null) {
            return s;
        }
        return result;
    }

    public static XMLReader createXmlReader() throws SAXException {
        XMLReader xmlReader = XMLReaderFactory.createXMLReader();
        try {
            // set namespace on false for performance reasons
            xmlReader.setFeature("http://xml.org/sax/features/namespaces", false);
            xmlReader.setFeature("http://xml.org/sax/features/namespace-prefixes", true);
        } catch (SAXException e) {
            log.info("Warning: could not set namespace feature to false", e);
            try {
                log.info("Current value is " + xmlReader.getFeature("http://xml.org/sax/features/namespaces"));
            } catch (SAXException x) {
                log.info("Current value is unknown but probably false.");
            }
        }
        return xmlReader;
    }

    /************************
     * Verification methods *
     ************************/

    /**
     * The method <code>isAttributeValid</code> is used to check a boolean
     * attribute.
     * <p>
     * Use this method to check if a boolean attribute is set (conatins 'true',
     * 'yes' or 'y'). Note: This method returns false, if the attribute is not
     * avalaible.
     * 
     * @param element
     *            element containing the attribute
     * @param attributeName
     *            the name of the attribute to check
     * @return boolean true if attribute contains 'true', 'yes' or 'y', false
     *         otherwise
     */
    public static boolean isAttributeValid(Element element, String attributeName) {
        return isAttributeValid(element, attributeName, false);
    }

    /**
     * The method <code>isAttributeValid</code> is used to check a boolean
     * attribute.
     * <p>
     * Use this method to check if a boolean attribute is set (conatins 'true',
     * 'yes' or 'y'). Note: This method returns the passed defaultValue, if the
     * attribute is not avalaible.
     * 
     * @param element
     *            element containing the attribute
     * @param attributeName
     *            the name of the attribute to check
     * @param defaultValue
     *            value to return if attribute is not available
     * @return boolean true if attribute contains 'true', 'yes' or 'y',
     *         defaultValue otherwise
     */
    public static boolean isAttributeValid(Element element, String attributeName, boolean defaultValue) {
        String attrValue = element.getAttribute(attributeName);
        if (attrValue.length() == 0) {
            return defaultValue;
        } else {
            return (attrValue.equalsIgnoreCase("true") || attrValue.equalsIgnoreCase("yes")
                    || attrValue.equalsIgnoreCase("y")) ? true : false;
        }
    }

    /******************
     * Access methods *
     ******************/

    /**
     * The method <code>getElements</code> is used to retrieve the nodes of type
     * ELEMENT_NODE.
     * <p>
     * Use this method to select only the nodes of type Node.ELEMENT_NODE of a
     * NodeList.
     * 
     * @param tmpElements
     *            the nodelist
     * @return List the list containing only ELEMENT_NODEs
     */
    public static List<Node> getElements(NodeList tmpElements) {
        List<Node> elements = new ArrayList<Node>();
        for (int i = 0; i < tmpElements.getLength(); i++) {
            if (tmpElements.item(i).getNodeType() == Node.ELEMENT_NODE) {
                elements.add(tmpElements.item(i));
            }
        }
        return elements;
    }

    /**
     * The method <code>getAttributes</code> is used to retrieve the nodes
     * attributes.
     * <p>
     * Use this method to select all nodes attributes
     * 
     * @param tmpElements
     *            the nodelist
     * @return List the list containing only ELEMENT_NODEs
     */
    public static List<Node> getAttributes(NamedNodeMap tmpElements) {
        List<Node> elements = new ArrayList<Node>();
        for (int i = 0; i < tmpElements.getLength(); i++) {
            elements.add(tmpElements.item(i));
        }
        return elements;
    }

    /**
     * This method returns the direct children with the given tagName.
     */
    public static NodeList getChildrenByTagName(Node node, String tagName) {
        VectorNodeList result = new VectorNodeList(); // result
        NodeList children = node.getChildNodes(); // get children
        for (int i = 0; i < children.getLength(); i++) {
            Node currentChild = children.item(i); // next child
            if (currentChild.getNodeName().equalsIgnoreCase(tagName)) {
                result.addElement(currentChild); // adding the element
            }
        }
        return result;
    }

    /**
     * This method returns the direct children with the given tagName.
     */
    public static NodeList getChildrenByTagName(Document doc, String tagName) {
        VectorNodeList result = new VectorNodeList(); // result
        NodeList children = doc.getChildNodes(); // get children
        for (int i = 0; i < children.getLength(); i++) {
            Node currentChild = children.item(i); // next child
            if (currentChild.getNodeName().equalsIgnoreCase(tagName)) {
                result.addElement(currentChild); // adding the element
            }
        }
        return result;
    }

    /**
     * This method returns the direct child elements with the given tagName.
     */
    public static List<Element> getChildElementListByTagName(Node node, String tagName) {
        List<Element> result = new ArrayList<Element>(); // result
        NodeList children = node.getChildNodes(); // get children
        for (int i = 0; i < children.getLength(); i++) {
            Node currentChild = children.item(i); // next child
            if (currentChild instanceof Element) {
                Element currentChildElement = (Element) currentChild;
                if (currentChildElement.getTagName().equals(tagName)) {
                    result.add(currentChildElement); // adding the element
                }
            }
        }
        return result;
    }

    /**
     * This method returns the direct child elements with the given tagName
     * attribute values
     * 
     * @param node
     *            starting element
     * @param tagName
     *            tag of the child element
     * @param attributeNames
     *            array of child attribute name
     * @param attributeValues
     *            array of child attribute value
     * @return
     */
    public static List<Element> getChildElementListByTagNameAndAttributeValues(Node node, String tagName,
            String[] attributeNames, String[] attributeValues) {
        List<Element> result = new ArrayList<Element>(); // result
        NodeList children = node.getChildNodes(); // get children
        for (int i = 0; i < children.getLength(); i++) {
            Node currentChild = children.item(i); // next child
            if (currentChild instanceof Element) {
                Element currentChildElement = (Element) currentChild;
                if (currentChildElement.getTagName().equals(tagName)) {
                    if (attributeNames != null) {
                        for (int j = 0; j < attributeNames.length; j++) {
                            String attributeName = attributeNames[j];
                            if (attributeName == null || attributeName.equals("")) {
                                result.add(currentChildElement); // adding the
                                                                 // element
                                continue;
                            }
                            String attributeValue = null;
                            if (attributeValues.length > j) {
                                attributeValue = attributeValues[j];
                            }
                            if (attributeValue == null
                                    || attributeValue.equals(currentChildElement.getAttribute(attributeName))) {
                                result.add(currentChildElement); // adding the
                                                                 // element
                            }
                        }
                    } else {
                        result.add(currentChildElement); // adding the element
                    }
                }
            }
        }
        return result;
    }

    /**
     * This method returns any direct child elements.
     */
    public static List<Element> getChildElementList(Node node) {
        List<Element> result = new ArrayList<Element>(); // result
        NodeList children = node.getChildNodes(); // get children
        for (int i = 0; i < children.getLength(); i++) {
            Node currentChild = children.item(i); // next child
            if (currentChild instanceof Element) {
                Element currentChildElement = (Element) currentChild;
                result.add(currentChildElement); // adding the element
            }
        }
        return result;
    }

    /**
     * This method returns any direct 1st child element.
     */
    public static Element getFirstChildElement(Node node) {
        NodeList children = node.getChildNodes(); // get children
        for (int i = 0; i < children.getLength(); i++) {
            Node currentChild = children.item(i); // next child
            if (currentChild instanceof Element) {
                return (Element) currentChild;
            }
        }
        return null;
    }

    /**
     * This method returns the direct child element with the given tagName.
     * 
     * @param element
     * @param tagName
     * @return
     */
    public static Element getChildByTagName(Element element, String tagName) {
        Node childNode = element.getFirstChild();
        while (childNode != null) {
            if (childNode instanceof Element) {
                if (((Element) childNode).getTagName().equals(tagName)) {
                    return (Element) childNode;
                }
            }
            childNode = childNode.getNextSibling();
        }

        return null;
    }

    public static Element getChildBySimpleTagName(Element element, String simpleTagName) {
        Node childNode = element.getFirstChild();
        while (childNode != null) {
            if (childNode instanceof Element) {
                Element xmlElement = (Element) childNode;
                if (xmlElement.getTagName().equals(simpleTagName)
                        || xmlElement.getTagName().endsWith(":" + simpleTagName)) {
                    return (Element) childNode;
                }
            }
            childNode = childNode.getNextSibling();
        }

        return null;
    }

    /**
     * @param node
     * @param tagName
     * @param attributeNames
     * @param attributeValues
     * @return
     */
    public static Element getChildByTagNameAndAttributeValues(Node node, String tagName, String[] attributeNames,
            String[] attributeValues) {
        NodeList children = node.getChildNodes(); // get children
        for (int i = 0; i < children.getLength(); i++) {
            Node currentChild = children.item(i); // next child
            if (currentChild instanceof Element) {
                Element currentChildElement = (Element) currentChild;
                if (currentChildElement.getTagName().equals(tagName)) {
                    if (attributeNames != null) {
                        for (int j = 0; j < attributeNames.length; j++) {
                            String attributeName = attributeNames[j];
                            if (attributeName == null || attributeName.equals("")) {
                                return currentChildElement; // found!
                            }
                            String attributeValue = null;
                            if (attributeValues.length > j) {
                                attributeValue = attributeValues[j];
                            }
                            if (attributeValue == null
                                    || attributeValue.equals(currentChildElement.getAttribute(attributeName))) {
                                return currentChildElement; // found!
                            }
                        }
                    } else {
                        return currentChildElement; // found!
                    }
                }
            }
        }
        return null;
    }

    /*****************
     * XPath methods *
     *****************/

    /**
     * The method <code>getNodeByXPath</code> is used to retrieve a node via
     * XPath.
     * <p>
     * Use this method to get ONE node matching ONE XPath.
     * 
     * @param parent
     *            the root
     * @param xpath
     *            the XPath expression
     * @return Node the node matching the XPath
     * @exception Exception
     *                if a parsing error occurs
     */
    public static Node getNodeByXPath(Node parent, String xpath) throws Exception {
// @formatter:off
//		if (xpath.indexOf("//") < 0 && xpath.indexOf("[") < 0 && xpath.indexOf("@") < 0) {
//			// Use simple method
//			return getElementByPath(parent, xpath);
//		}
// @formatter:on
        return (Node) newXpathFactory().newXPath().evaluate(xpath, parent, XPathConstants.NODE);
    }

    /**
     * The method <code>getNodeByXPath</code> is used to retrieve a node via
     * XPath. given the namespace context if namespaces are used.
     * <p>
     * Use this method to get ONE node matching ONE XPath.
     * 
     * @param parent
     * @param xpath
     * @param ctx
     * @return
     * @throws Exception
     */
    public static Node getNodeByXPath(Node parent, String xpath, NamespaceContext ctx) throws Exception {
        log.debug("started ...for ctx");
        log.debug("... xpath : " + xpath);
        XPath xp = newXpathFactory().newXPath();
        if (ctx != null)
            xp.setNamespaceContext(ctx);
        log.debug("finished.");
        return (Node) xp.evaluate(xpath, parent, XPathConstants.NODE);
    }

    /**
     * The method <code>getNodeByXPath</code> is used to retrieve a node via
     * XPath.
     * <p>
     * Use this method to get ONE node matching ONE XPath.
     * <p>
     * Ensure the document is opened with namespace awareness.
     * 
     * @param parent
     *            the root
     * @param namespaces
     *            e.g. {{"Doc", "urn:swift:xsd:setr.004.001.03"}, {"SwInt",
     *            "urn:swift:snl:ns.SwInt"}}
     * @param xpath
     *            the XPath expression
     * @return Node the node matching the XPath
     * @exception Exception
     *                if a parsing error occurs
     */
    public static Node getNodeByXPath(Node parent, String[][] namespaces, String xpath) throws Exception {
        XPath xpathEngine = newXpathFactory().newXPath();
        if (namespaces != null) {
            NamespaceContext nsContext = new StringArrayNamespaceContext(namespaces);
            xpathEngine.setNamespaceContext(nsContext);
        }
        return (Node) xpathEngine.evaluate(xpath, parent, XPathConstants.NODE);
    }

    public static NodeList getNodeListByXPath(Node parent, String[][] namespaces, String xpath) throws Exception {
        XPath xpathEngine = newXpathFactory().newXPath();
        if (namespaces != null) {
            NamespaceContext nsContext = new StringArrayNamespaceContext(namespaces);
            xpathEngine.setNamespaceContext(nsContext);
        }
        return (NodeList) xpathEngine.evaluate(xpath, parent, XPathConstants.NODESET);
    }

    /**
     * The method <code>getNodeByXPath</code> is used to retrieve a node via
     * XPath.
     * <p>
     * Use this method to get ONE node matching ONE XPath.
     * <p>
     * Ensure the document is opened with namespace awareness.
     * 
     * @param xml
     *            the xml string
     * @param namespaces
     *            e.g. {{"Doc", "urn:swift:xsd:setr.004.001.03"}, {"SwInt",
     *            "urn:swift:snl:ns.SwInt"}}
     * @param xpath
     *            the XPath expression
     * @return Node the node matching the XPath
     */
    public static Node getNodeByXPath(String xml, String[][] namespaces, String xpath) {
        NamespaceContext ctx = new StringArrayNamespaceContext(namespaces);
        XPath xp = newXpathFactory().newXPath();
        xp.setNamespaceContext(ctx);
        InputSource inputSource = new InputSource(new StringReader(xml));
        try {
            return (Node) xp.evaluate(xpath, inputSource, javax.xml.xpath.XPathConstants.NODE);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * The method <code>getNodeListByXPath</code> is used to retrieve nodes via
     * XPath.
     * <p>
     * Use this method to get a LIST of nodes matching ONE XPath.
     * 
     * @param parent
     *            the root
     * @param xpath
     *            the XPath
     * @return NodeList the list of nodes matching the XPath
     * @exception Exception
     *                if a parsing error occurs
     */
    public static NodeList getNodeListByXPath(Node parent, String xpath) throws Exception {
        return (NodeList) newXpathFactory().newXPath().evaluate(xpath, parent, XPathConstants.NODESET);
    }

    public static NodeList getNodeListByXPath(Node parent, String xpath, NamespaceContext ctx) throws Exception {
        log.debug("started ...for ctx");
        log.debug("... xpath : " + xpath);
        XPath xp = newXpathFactory().newXPath();
        if (ctx != null)
            xp.setNamespaceContext(ctx);
        log.debug("finished.");
        // return (NodeList)xp.compile(xpath).evaluate(parent,
        // XPathConstants.NODESET);
        return (NodeList) xp.evaluate(xpath, parent, XPathConstants.NODESET);
    }

    /**
     * The method <code>getNodeListByXPathList</code> is used to retrieve nodes
     * via a list of XPathes.
     * <p>
     * Use this method to get a LIST of nodes matching a LIST of XPathes.
     * 
     * @param parent
     *            the root
     * @param xpathList
     *            the list of XPathes
     * @return NodeList the list of nodes matching the list of XPathes
     * @exception Exception
     *                if a parsing error occurs
     */
    public static NodeList getNodeListByXPathList(Node parent, List<?> xpathList) throws Exception {
        VectorNodeList nodeList = new VectorNodeList();
        for (Iterator<?> iterator = xpathList.iterator(); iterator.hasNext();) {
            nodeList.addElement(getNodeByXPath(parent, (String) iterator.next()));
        }
        return nodeList;
    }

    public static NodeList getNodeListByXPathList(Node parent, String[][] namespaces, List<?> xpathList)
            throws Exception {
        VectorNodeList nodeList = new VectorNodeList();
        for (Iterator<?> iterator = xpathList.iterator(); iterator.hasNext();) {
            nodeList.addElement(getNodeByXPath(parent, namespaces, (String) iterator.next()));
        }
        return nodeList;
    }

    /**
     * The method <code>getNodeValueByXPath</code> is used to retrieve the value
     * of a node via XPath.
     * <p>
     * Use this method to get the value of ONE node matching ONE XPath. This is
     * a convenience method, it retrieves the node an checks if it contains a
     * child -> if yes, the child's value is returned.
     * 
     * @param parent
     *            the root
     * @param xpath
     *            the XPath
     * @return String the value of the node matching the XPath (empty string if
     *         no value)
     * @exception Exception
     *                if a parsing error occurs
     */
    public static String getNodeValueByXPath(Node parent, String xpath) throws Exception {
        Node node = getNodeByXPath(parent, xpath);
        String value = (node.getFirstChild() != null) ? node.getFirstChild().getNodeValue() : "";
        return value;
    }

    /**
     * The method <code>setValueToXPath</code> is used to set a value to a text
     * node that already exists.
     * 
     * @param parent
     * @param xpath
     * @param value
     * @throws Exception
     */
    public static void setValueToXPath(Node parent, String xpath, Object value) throws Exception {
        log.debug("...parent : " + parent + ", xpath: " + xpath + ", value: " + value);
        getNodeByXPath(parent, xpath + "/text()").setNodeValue(value.toString());
    }

    /**
     * The method <code>setValueToXPath</code> is used to to set a value to a
     * text node that already exists given the namespace context for namespace
     * aware nodes.
     * 
     * @param parent
     * @param xpath
     * @param value
     * @param ctx
     * @throws Exception
     */
    public static void setValueToXPath(Node parent, String xpath, Object value, NamespaceContext ctx) throws Exception {
        log.debug("...parent : " + parent + ", xpath: " + xpath + ", value: " + value + ", context set");
        Node node = getNodeByXPath(parent, xpath + "/text()", ctx);
        node.setNodeValue(value.toString());
        log.debug("... node set  : " + node.toString());
    }

    /**
     * The method <code>getNodeValueListByXPath</code> is used to retrieve the
     * values of nodes via XPath.
     * <p>
     * Use this method to get the values of a LIST of nodes matching ONE XPath.
     * This is a convenience method, it calls getNodeListByXPath(...) and then
     * getFirstChild().getNodeValue() on every Node in the list.
     * 
     * @param parent
     *            the root
     * @param xpath
     *            the XPath
     * @return List the values of the list of nodes matching the XPath (empty
     *         string if no value)
     * @exception Exception
     *                if a parsing error occurs
     */
    public static List<String> getNodeValueListByXPath(Node parent, String xpath) throws Exception {
        Vector<String> nodeValues = new Vector<String>();
        NodeList nodeList = getNodeListByXPath(parent, xpath);
        for (int i = 0; i < nodeList.getLength(); i++) {
            String value = (nodeList.item(i).getFirstChild() != null) ? nodeList.item(i).getFirstChild().getNodeValue()
                    : "";
            nodeValues.addElement(value);
        }
        return nodeValues;
    }

    /**
     * The method <code>getNodeValueListByXPathList</code> is used to retrieve
     * the values of nodes via a list of XPathes.
     * <p>
     * Use this method to get the values of a LIST of nodes matching a LIST of
     * XPathes. This is a convenience method, it calls
     * getNodeListByXPathList(...) and then getFirstChild().getNodeValue() on
     * every Node in the list.
     * 
     * @param parent
     *            the root
     * @param xpathList
     *            the list of XPathes
     * @return List the values of the list of nodes matching the list of XPathes
     * @exception Exception
     *                if a parsing error occurs
     */
    public static List<String> getNodeValueListByXPathList(Node parent, List<?> xpathList) throws Exception {
        Vector<String> nodeValues = new Vector<String>();
        for (Iterator<?> iterator = xpathList.iterator(); iterator.hasNext();) {
            nodeValues.addElement(getNodeValueByXPath(parent, (String) iterator.next()));
        }
        return nodeValues;
    }

    /**
     * The method <code>setValuesToXPathList</code> ...
     * <p>
     * 
     * @param parent
     *            ...
     * @param xpathList
     *            ...
     * @param valueList
     *            ...
     */
    public static void setValuesToXPathList(Node parent, List<?> xpathList, List<?> valueList) throws Exception {
        int i = 0;
        for (Iterator<?> iterator = xpathList.iterator(); iterator.hasNext(); i++) {
            setValueToXPath(parent, (String) iterator.next(), valueList.get(i));
        }
    }

    /**
     * The method <code>split</code> splits an XML document in substrings of a
     * specified maximum size, in such a way that the end of each substring
     * coincide with the end of an XML element.
     * <p>
     * 
     * @param xmlString
     *            the XML document (in string form) to split
     * @param size
     *            the maximum length each individual substring
     * @return The array of substrings
     */
    public static String[] split(String xmlString, int size) {
        List<String> list = new ArrayList<String>();
        int len = xmlString.length();
        int i = 0; // Index of the start of the current substring within the
        // xmlString
        int j = 0; // Index of the end of the current substring within the
        // xmlString
        int k1; // Index of the start of the end tag of the XML element: "</"
        int k2; // Index of the end of the end tag of the XML element: ">"

        while (true) {
            i = j;
            j += size; // Estimated substring end
            if (j >= len) {
                list.add(xmlString.substring(i));
                break;
            }
            k1 = xmlString.lastIndexOf("</", j); // Find start of end tag of
            // last XML element in
            // substring
            if (k1 < 0)
                throw new RuntimeException(
                        "Cannot find the start of an XML element end tag within the following substring: "
                                + xmlString.substring(i, j));
            k2 = xmlString.indexOf(">", k1); // Find end of end tag of last XML
            // element in substring
            if (k2 >= j) { // End of end tag lies beyond the end of substring
                k1 = xmlString.lastIndexOf("</", k1 - 1); // Find start of end
                // tag of last but
                // one XML element
                // in substring
                if (k1 < 0)
                    throw new RuntimeException(
                            "Cannot find the start of an XML element end tag within the following substring: "
                                    + xmlString.substring(i, j));
                k2 = xmlString.indexOf(">", k1); // Find end of end tag of last
                // but one XML element in
                // substring
                if (k2 >= j)
                    throw new RuntimeException("Cannot find an XML element end within the following substring: "
                            + xmlString.substring(i, j));
            }
            j = k2 + 1;
            list.add(xmlString.substring(i, j));
        }

        return list.toArray(new String[0]);
    }

    /**
     * Returns the path (all parents) of the specified node to the root.
     * 
     * @param node
     *            the node for request the path.
     * @return the path of the specified node.
     */
    public static Node[] getPath(Node node) {
        ArrayList<Node> path = new ArrayList<Node>();
        while (node != null && !(node instanceof Document)) {
            path.add(0, node);
            node = node.getParentNode();
        }
        return path.toArray(new Node[path.size()]);
    }

    /***************************
     * Element related methods *
     ***************************/

    /**
     * The method <code>createElement</code> is used to create an element under
     * a given document. The namespace is resolved.
     * 
     * @param currentDOM
     * @param elementId
     *            (example: SagInterf:AckMessage)
     * @return
     */
    public static Element createElement(Document doc, String elementId) {
        log.debug("started ...");
        log.debug("... elementId : " + elementId);
        Element element = null;
        String currentNamespace = (elementId.indexOf(":") > 0) ? elementId.substring(0, elementId.indexOf(":")) : null;
        if (currentNamespace != null) {
            log.debug("... currentNamespace -> " + currentNamespace);
            element = doc.createElementNS(currentNamespace, elementId);
        } else {
            element = doc.createElement(elementId);
        }
        log.debug("finished.");
        return element;
    }

    /**
     * The method <code>createElementFromXPath</code> is used to create an
     * element under a given document using a simple element path.
     * 
     * @param doc
     *            currentDOM
     * @param elementPath
     *            element path e.g. <blockquote>/root/parent/child</blockquote>
     * @return
     * @throws Exception
     */
    public static Element createElementFromXPath(Document doc, String elementPath) throws Exception {
        Node parentElementNode = doc;
        Element elementNode = null;

        String processingPath = elementPath;
        if (processingPath.startsWith("/")) {
            processingPath = processingPath.substring(1);
        }

        int nextSlashSign = processingPath.indexOf("/");
        while (nextSlashSign > 0 || processingPath.length() > 0) {
            String elementTag = "";
            if (nextSlashSign > 0) {
                elementTag = processingPath.substring(0, nextSlashSign);
                processingPath = processingPath.substring(nextSlashSign + 1);
            } else {
                elementTag = processingPath;
                processingPath = "";
            }

            if (parentElementNode instanceof Document) {
                elementNode = ((Document) parentElementNode).getDocumentElement();
                if (elementNode != null) {
                    if (!elementNode.getTagName().equals(elementTag)) {
                        elementNode = null;
                    }
                }
            } else {
                elementNode = getChildByTagName((Element) parentElementNode, elementTag);
            }
            if (elementNode == null) {
                elementNode = doc.createElement(elementTag);
                if (!(parentElementNode instanceof Document)) {
                    parentElementNode.appendChild(elementNode);
                }
            }

            parentElementNode = elementNode;
            nextSlashSign = processingPath.indexOf("/");
        }

        return elementNode;
    }

    /**
     * @see http://java.sun.com/developer/EJTechTips/2004/tt0527.html
     * @param doc
     * @return
     */
    public static String documentToString(Document doc) throws Exception {
        return documentToString(doc, true);
    }

    public static String documentToString(Document doc, boolean omitXmlDeclaration) throws Exception {
        return nodeToString(doc, true, omitXmlDeclaration);
        // OutputFormat format = new OutputFormat(doc);
        // format.setLineWidth(2000);
        // format.setIndenting(true);
        // format.setIndent(2);
        // Writer out = new StringWriter();
        // XMLSerializer serializer = new XMLSerializer(out, format);
        // serializer.serialize(doc);
        //
        // return out.toString();
    }

    /*****************************
     * Attribute related methods *
     *****************************/

    /**
     * The method <code>getIntAttribute</code> returns the value of an element
     * attribute which is expected to contain an integer. If the attribute is
     * missing or empty or has an non integer content, then the specified
     * default value is returned.
     * 
     * @param element
     *            Document element
     * @param attrName
     *            Attribute name
     * @param defValue
     *            Default value
     * @return The attribute value as integer
     */
    public static int getIntAttribute(Element element, String attrName, int defValue) {
        log.debug("started ...");
        String content = element.getAttribute(attrName);
        int value;
        if (content == null || content.trim().length() == 0) {
            log.debug("... attribute is not present or empty: return default value");
            value = defValue;
        } else {
            try {
                value = Integer.parseInt(content);
            } catch (Exception e) {
                log.debug("Unexpected exception: " + e + ": return default value");
                value = defValue;
            }
        }
        log.debug("finished.");
        return value;
    }

    /**
     * The method <code>getStringAttribute</code> returns the value of an
     * element attribute which is expected to contain a string. If the attribute
     * is missing or empty or has an non string content, then the specified
     * default value is returned.
     * 
     * @param element
     *            Document element
     * @param attrName
     *            Attribute name
     * @param defValue
     *            Default value
     * @return The attribute value as string
     */
    public static String getStringAttribute(Element element, String attrName) {
        log.debug("started ...");
        String content = element.getAttribute(attrName);
        String value;
        if (content == null || content.trim().length() == 0) {
            log.debug("... attribute is not present or empty: return default value");
            value = null;
        } else {
            try {
                value = content;
            } catch (Exception e) {
                log.debug("Unexpected exception: " + e + ": return default value");
                value = null;
            }
        }
        log.debug("finished.");
        return value;
    }

    /**
     * The method <code>convertDateToXmlValue</code> converts a Date object to
     * an XML date value string. The date pattern pattern used is
     * yyyy-MM-dd(+|-)zz:zz that complies to W3C XML Schema specification. For
     * example: 2009-07-30+02:00
     * 
     * @param date
     *            Date object to parse
     * @return formatted date value for XML as string
     */
    public static String convertDateToXmlValue(Date date) {
        SimpleDateFormat sdf = new SimpleDateFormat(XML_DATE_PATTERN);
        String javaFormattedDate = sdf.format(date);
        String xmlFormattedDate = javaFormattedDate.substring(0, javaFormattedDate.length() - 2) + ":"
                + javaFormattedDate.substring(javaFormattedDate.length() - 2);
        return xmlFormattedDate;
    }

    /**
     * The method <code>convertXmlValueToDate</code> converts a XML date value
     * string to a Date object. The date pattern pattern used is
     * yyyy-MM-dd((+|-)zz:zz) that complies to W3C XML Schema specification. For
     * example: 2009-07-30+02:00 or 2009-07-30
     * 
     * @param xmlFormattedDate
     *            XML date value string
     * @return Date object
     */
    public static Date convertXmlValueToDate(String xmlFormattedDate) throws ParseException {
        String javaFormattedDate = xmlFormattedDate;
        SimpleDateFormat sdf = null;
        if (xmlFormattedDate.endsWith("Z")) {
            javaFormattedDate = xmlFormattedDate.substring(0, xmlFormattedDate.length() - 3)
                    + xmlFormattedDate.substring(xmlFormattedDate.length() - 2);
            sdf = new SimpleDateFormat(XML_DATE_PATTERN);
        } else {
            sdf = new SimpleDateFormat(XML_NO_TZ_DATE_PATTERN);
        }
        return sdf.parse(javaFormattedDate);
    }

    /**
     * The method <code>convertDateTimeToXmlValue</code> converts a Date object
     * with date and time contents to an XML date time value string. The date
     * time pattern pattern used is yyyy-MM-dd'T'HH:mm:ss.SSS(+|-)zz:zz that
     * complies to W3C XML Schema specification. For example:
     * 2009-07-30T20:45:50.720+02:00
     * 
     * @param date
     *            Date object with date and time contents to parse
     * @return formatted date and time value for XML as string
     */
    public static String convertDateTimeToXmlValue(Date date) {
        SimpleDateFormat sdf = new SimpleDateFormat(XML_DATETIME_PATTERN);
        String javaFormattedDateTime = sdf.format(date);
        String xmlFormattedDateTime = javaFormattedDateTime.substring(0, javaFormattedDateTime.length() - 2) + ":"
                + javaFormattedDateTime.substring(javaFormattedDateTime.length() - 2);
        return xmlFormattedDateTime;
    }

    /**
     * The method <code>convertXmlValueToDate</code> converts a XML date time
     * value in string to a Timestamp object. The date time pattern pattern used
     * is yyyy-MM-dd'T'HH:mm:ss.SSS(+|-)zz:zz that complies to W3C XML Schema
     * specification. For example: 2009-07-30T20:45:50.720+02:00
     * 
     * @param xmlFormattedDate
     *            XML date and time value string
     * @return Timestamp object
     */
    public static Timestamp convertXmlValueToDateTime(String xmlFormattedDateTime) throws ParseException {
        String javaFormattedDateTime = xmlFormattedDateTime;
        if (javaFormattedDateTime.endsWith("Z")) {
            javaFormattedDateTime = javaFormattedDateTime.substring(0, javaFormattedDateTime.length() - 1) + "+00:00";
        }
        javaFormattedDateTime = javaFormattedDateTime.substring(0, javaFormattedDateTime.length() - 3)
                + javaFormattedDateTime.substring(javaFormattedDateTime.length() - 2);

        SimpleDateFormat sdf = new SimpleDateFormat(XML_DATETIME_PATTERN);
        Date date = sdf.parse(javaFormattedDateTime);
        return new Timestamp(date.getTime());
    }

    /**
     * The method <code>convertDateToXmlValue</code> converts a Date object to
     * an XML date value in string. The time pattern used is HH:mm:ss.SSSzzzzzz
     * that complies to W3C XML Schema specification. For example:
     * 20:45:50.720+02:00
     * 
     * @param time
     *            Time object to parse
     * @return formatted date value for XML as string
     */
    public static String convertTimeToXmlValue(Time time) {
        SimpleDateFormat sdf = new SimpleDateFormat(XML_TIME_PATTERN);
        String javaFormattedTime = sdf.format(time);
        String xmlFormattedTime = javaFormattedTime.substring(0, javaFormattedTime.length() - 2) + ":"
                + javaFormattedTime.substring(javaFormattedTime.length() - 2);
        return xmlFormattedTime;
    }

    /**
     * The method <code>convertXmlValueToDate</code> converts a XML time value
     * in string to a Time object. The time pattern used is HH:mm:ss.SSSzzzzzz
     * that complies to W3C XML Schema specification. For example:
     * 20:45:50.720+02:00
     * 
     * @param timeText
     *            XML time value string
     * @return Time object
     */
    public static Time convertXmlValueToTime(String xmlFormattedTime) throws ParseException {
        String javaFormattedTime = xmlFormattedTime.substring(0, xmlFormattedTime.length() - 3)
                + xmlFormattedTime.substring(xmlFormattedTime.length() - 2);
        SimpleDateFormat sdf = new SimpleDateFormat(XML_TIME_PATTERN);
        Date date = sdf.parse(javaFormattedTime);
        return new Time(date.getTime());
    }

    /**
     * The method <code>getOrCreateRootNode</code> retrieves the existing root
     * node of an XML document. A new root node would be created when the
     * document does not contain any root node (EMPTY).
     * 
     * @param xmlDoc
     * @param rootTagName
     * @return
     * @throws Exception
     */
    public static Element getOrCreateRootNode(Document xmlDoc, String rootTagName) throws Exception {
        Element xmlRootNode = (Element) XmlUtils.getNodeByXPath(xmlDoc, "/" + rootTagName);
        if (xmlRootNode == null) {
            xmlRootNode = xmlDoc.createElement(rootTagName);
            xmlDoc.appendChild(xmlRootNode);
        }

        return xmlRootNode;
    }

    /**
     * The method <code>createSubElement</code> produces sub-element
     * (QName.ELEMENT) on a particular xml node.
     * 
     * @param xmlParentNode
     * @param tagName
     * @return
     */
    public static Element createSubElement(Element xmlParentNode, String tagName) {
        return createSubElement(xmlParentNode, xmlParentNode.getNamespaceURI(), tagName);
    }

    /**
     * The method <code>createSubElement</code> produces sub-element
     * (QName.ELEMENT) on a particular xml node.
     * 
     * @param xmlParentNode
     * @param namespaceUri
     * @param tagName
     * @return
     */
    public static Element createSubElement(Element xmlParentNode, String namespaceUri, String tagName) {
        Element xmlNode = null;
        if (namespaceUri != null && !namespaceUri.equals("")) {
            String effectiveTagName = tagName;
            if (tagName.indexOf(":") > 0) {
                effectiveTagName = getSimpleTagName(tagName);
            }
            xmlNode = xmlParentNode.getOwnerDocument().createElementNS(namespaceUri, effectiveTagName);
            String namespacePrefix = xmlParentNode.lookupPrefix(namespaceUri);
            xmlNode.setPrefix(namespacePrefix);
        } else {
            xmlNode = xmlParentNode.getOwnerDocument().createElement(tagName);
        }
        xmlParentNode.appendChild(xmlNode);

        return xmlNode;
    }

    /**
     * The method <code>copyAttributes</code> copies the attributes of
     * sourceElement to targetElement.
     * 
     * @param sourceElement
     * @param targetElement
     * @return
     * @throws Exception
     */
    public static boolean copyAttributes(Element sourceElement, Element targetElement) throws Exception {
        NamedNodeMap attributeList = sourceElement.getAttributes();
        for (int i = 0; i < attributeList.getLength(); i++) {
            Node attribute = attributeList.item(i);
            targetElement.setAttribute(attribute.getNodeName(), attribute.getNodeValue());
        }

        return true;
    }

    /**
     * The method <code>copySubElementsWithValue</code> copies the sub elements
     * of sourceElement to targetElement.
     * 
     * @param sourceElement
     * @param targetElement
     * @return
     * @throws Exception
     */
    public static boolean copySubElementsWithValue(Element sourceElement, Element targetElement) throws Exception {
        NodeList subElementList = sourceElement.getChildNodes();
        for (int i = 0; i < subElementList.getLength(); i++) {
            Node node = subElementList.item(i);
            if (node instanceof Element) {
                Element subElement = (Element) node;
                Element targetSubElement = createSubElement(targetElement, subElement.getNodeName());
                copyAttributes(subElement, targetSubElement);
                if (subElement.hasChildNodes()) {
                    copySubElementsWithValue(subElement, targetSubElement);
                }
            } else if (node instanceof Text) {
                Text textNode = (Text) node;
                Text targetTextNode = targetElement.getOwnerDocument().createTextNode(textNode.getTextContent());
                targetElement.appendChild(targetTextNode);
            }
        }

        return true;
    }

    /**
     * The method <code>copyNode</code> copies the sub elements and attributes
     * of sourceElement to targetElement.
     * 
     * @param sourceElement
     * @param targetElement
     * @return
     * @throws Exception
     */
    public static boolean copyNode(Element sourceElement, Element targetElement) throws Exception {
        copyAttributes(sourceElement, targetElement);
        copySubElementsWithValue(sourceElement, targetElement);

        return true;
    }

    /**
     * The method <code>removeEmptyValues</code> removes all empty elements. An
     * empty element means element without child node (text value,
     * sub-element(s), attribute(s)).
     * 
     * @param xmlDoc
     *            the xml document to process
     * @throws Exception
     */
    public static void removeEmptyElements(Document xmlDoc) throws Exception {
        Element xmlElement = xmlDoc.getDocumentElement();
        removeEmptyElements(xmlElement, true, null);
    }

    /**
     * The method <code>removeEmptyValues</code> removes all empty elements. An
     * empty element means element without child node (text value,
     * sub-element(s), attribute(s)).
     * 
     * @param xmlDoc
     *            the xml document to process
     * @param excludeTagNames
     *            set of tag names to exclude from removal when empty
     * @throws Exception
     */
    public static void removeEmptyElements(Document xmlDoc, Set<String> excludeTagNames) throws Exception {
        Element xmlElement = xmlDoc.getDocumentElement();
        removeEmptyElements(xmlElement, true, excludeTagNames);
    }

    /**
     * The method <code>removeEmptyElements</code> removes all empty elements.
     * An empty element means element without child node (text value,
     * sub-element(s), attribute(s)).
     * 
     * @param xmlElement
     *            an XML element to process @param recursive the flag to process
     *            sub-element(s). @throws Exception @throws
     */
    public static void removeEmptyElements(Element xmlElement, boolean recursive) throws Exception {
        removeEmptyElements(xmlElement, recursive, null);
    }

    /**
     * The method <code>removeEmptyElements</code> removes all empty elements.
     * An empty element means element without child node (text value,
     * sub-element(s), attribute(s)).
     * 
     * @param xmlElement
     *            an XML element to process
     * @param recursive
     *            the flag to process sub-element(s).
     * @param excludeTagNames
     *            set of tag names to exclude from removal when empty
     * @throws Exception
     */
    public static void removeEmptyElements(Element xmlElement, boolean recursive, Set<String> excludeTagNames)
            throws Exception {
        if (recursive) {
            try {
                NodeList children = xmlElement.getChildNodes(); // get children
                for (int i = 0; i < children.getLength(); i++) {
                    Node currentChild = children.item(i); // next child
                    if (currentChild instanceof Element) {
                        removeEmptyElements((Element) currentChild, true, excludeTagNames);
                    } else if (!(currentChild instanceof Text)) {
                        // What kind of attribute here?
                        log.info("Unhandled child node type: '" + currentChild.getNodeName() + " ("
                                + currentChild.getClass() + ")' of " + xmlElement.getTagName());
                    }
                }
            } catch (Exception e) {
                log.error("Fail to remove empty elements on node: " + xmlElement.getNodeName(), e);
            }
        }

        // remove empty attributes
        NamedNodeMap attrs = xmlElement.getAttributes();
        int attrCount = 0;
        while (attrCount < attrs.getLength()) {
            Attr attr = (Attr) attrs.item(attrCount);
            if (attr.getNodeValue().equals("")) {
                xmlElement.removeAttributeNode(attr);
            } else {
                attrCount++;
            }
        }

        if (excludeTagNames == null || !excludeTagNames.contains(xmlElement.getTagName())) {
            if (attrCount < 1 && xmlElement.getFirstChild() == null) {
                removeElement(xmlElement); // only when there is no attribute
                                           // available
            } else {
                if (attrCount < 1 && getFirstChildElement(xmlElement) == null
                        && Node.TEXT_NODE == xmlElement.getFirstChild().getNodeType()
                        && xmlElement.getTextContent().trim().equals("")) {
                    // empty text value
                    removeElement(xmlElement);
                }
            }
        }
    }

    public static void removeElement(Element xmlElement) {
        Element xmlParentElement = (Element) xmlElement.getParentNode();
        xmlParentElement.removeChild(xmlElement);
    }

    public static boolean getBooleanAttribute(Element xmlElement, String attributeName, boolean defaultValue) {
        String stringValue = xmlElement.getAttribute(attributeName);

        if (stringValue == null || stringValue.equals(""))
            return defaultValue;

        try {
            return Boolean.parseBoolean(stringValue);
        } catch (Exception e) {
            // do nothing, let the default value returns.
        }

        return defaultValue;
    }

    public static XPathFactory newXpathFactory() {
        XPathFactory xPathFactory = null;
        try {
            xPathFactory = XPathFactory.newInstance(XPathFactory.DEFAULT_OBJECT_MODEL_URI,
                    "com.sun.org.apache.xpath.internal.jaxp.XPathFactoryImpl", XmlUtils.class.getClassLoader());
            // Use default XPath factory from com.sun
        } catch (Exception e) {
            try {
                xPathFactory = XPathFactory.newInstance(XPathFactory.DEFAULT_OBJECT_MODEL_URI,
                        "org.apache.xpath.jaxp.XPathFactoryImpl", XmlUtils.class.getClassLoader());
                // Use default XPath factory from com.sun
            } catch (Exception e1) {
                throw new RuntimeException(
                        "Error instantiating XPathFactory from JRE and Xalan. Please verify/control the system classpath setup.",
                        e1);
            }
        }
        return xPathFactory;
    }

    public static String getNamespacePrefix(String nodeName) {
        int index = nodeName.lastIndexOf(":");
        if (index >= 0) {
            return nodeName.substring(0, index);
        }
        return "";
    }

    public static final String getSimpleTagName(String tagName) {
        int index = tagName.lastIndexOf(":");
        if (index >= 0) {
            return tagName.substring(index + 1);
        }
        return tagName;
    }

    public static Element getElementByPath(Node parent, String path) {
        if (!(parent instanceof Element || parent instanceof Document) || parent == null || path == null
                || path.equals(""))
            return null;

        Element xmlCurrentNode = null;
        if (parent instanceof Document) {
            xmlCurrentNode = ((Document) parent).getDocumentElement();
        } else {
            xmlCurrentNode = (Element) parent;
        }

        String threeFirstChar = path.substring(0, (path.length() > 2 ? 3 : path.length()));
        if (threeFirstChar.startsWith("/")) {
            // Root
            xmlCurrentNode = xmlCurrentNode.getOwnerDocument().getDocumentElement();

            String newPath = path.substring(1);
            boolean lastTag = false;
            int nextSlashIndex = newPath.indexOf('/');
            if (nextSlashIndex < 0) {
                nextSlashIndex = newPath.length();
                lastTag = true;
            }

            String tagName = newPath.substring(0, nextSlashIndex);

            if (!xmlCurrentNode.getTagName().equals(tagName)) {
                // Root tag does not match
                return null;
            }

            if (lastTag) {
                return xmlCurrentNode;
            }

            // Chop the root node off from the path
            return getElementByPath(xmlCurrentNode, newPath.substring(nextSlashIndex + 1));
        } else if (threeFirstChar.equals("*")) {
            return getFirstChildElement(xmlCurrentNode);
        } else if (threeFirstChar.equals("../")) {
            // Go to parent
            if (xmlCurrentNode.getParentNode() instanceof Element) {
                xmlCurrentNode = (Element) xmlCurrentNode.getParentNode();
            } else {
                // It is currently in the root node, ignore this ".."
            }
            return getElementByPath(xmlCurrentNode, path.substring(3));
        } else {
            boolean lastTag = false;
            int nextSlashIndex = path.indexOf('/');
            if (nextSlashIndex < 0) {
                nextSlashIndex = path.length();
                lastTag = true;
            }
            String tagName = path.substring(0, nextSlashIndex);
            if (lastTag) {
                return getChildByTagName(xmlCurrentNode, tagName);
            } else {
                List<Element> xmlChildElementList = getChildElementListByTagName(xmlCurrentNode, tagName);
                String subPath = path.substring(nextSlashIndex + 1);
                for (Element xmlChildElement : xmlChildElementList) {
                    Element xmlFoundElement = getElementByPath(xmlChildElement, subPath);
                    if (xmlFoundElement != null)
                        return xmlFoundElement;
                }
            }
        }

        return null;
    }

    public static String linearize(String input) {
        return input.trim().replaceAll(XML_LINARIZATION_REGEX, XML_LINARIZATION_REPLACEMENT);
    }

    public static String getXPath(Element el) {
        String result = "";
        while (el != el.getOwnerDocument().getDocumentElement()) {
            result = el.getNodeName() + "/" + result;
            el = (Element) el.getParentNode();
        }
        if (result.length() > 0)
            result = result.substring(0, result.length() - 1);
        return "/" + result;
    }

    public static InputStream getRelativeInputStream(String relativePath) {
        log.debug("started ...");
        InputStream is = null;

        if (tempClass == null) {
            tempClass = new Object().getClass(); // inits the class object
        }
        log.debug("... opening relative file '" + relativePath + "' via Class Loader ...");
        is = tempClass.getResourceAsStream(relativePath); // gets resource
        // stream via class
        // loader
        if (is == null) { // class loader did not work, trying filesystem
            if (log.isDebugEnabled()) {
                log.info("...... couldn't open ResourceStream via Class Loader. BootstrapClassPath -> ");
            }
            log.debug("... trying filesystem ...");
            try {
                is = new FileInputStream(relativePath);
            } catch (FileNotFoundException fe) {
                log.error("...... couldn't open ResourceStream via filesystem -> " + fe);
            }
        }
        log.debug("finished.");
        return is;
    }
}
