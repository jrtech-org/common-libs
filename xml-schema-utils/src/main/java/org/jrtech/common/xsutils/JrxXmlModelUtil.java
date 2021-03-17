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
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.xml.XMLConstants;
import javax.xml.parsers.SAXParserFactory;

import org.apache.commons.lang3.ArrayUtils;
import org.jrtech.common.xmlutils.XmlUtils;
import org.jrtech.common.xsutils.model.JrxAttribute;
import org.jrtech.common.xsutils.model.JrxChoiceGroup;
import org.jrtech.common.xsutils.model.JrxChoiceGroupUtil;
import org.jrtech.common.xsutils.model.JrxDeclaration;
import org.jrtech.common.xsutils.model.JrxDocument;
import org.jrtech.common.xsutils.model.JrxElement;
import org.jrtech.common.xsutils.model.JrxElementGroup;
import org.jrtech.common.xsutils.model.JrxGroup;
import org.jrtech.common.xsutils.model.JrxTerm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.sun.xml.xsom.XSAttributeUse;
import com.sun.xml.xsom.XSComplexType;
import com.sun.xml.xsom.XSContentType;
import com.sun.xml.xsom.XSDeclaration;
import com.sun.xml.xsom.XSElementDecl;
import com.sun.xml.xsom.XSFacet;
import com.sun.xml.xsom.XSModelGroup;
import com.sun.xml.xsom.XSModelGroup.Compositor;
import com.sun.xml.xsom.XSModelGroupDecl;
import com.sun.xml.xsom.XSParticle;
import com.sun.xml.xsom.XSRestrictionSimpleType;
import com.sun.xml.xsom.XSSchema;
import com.sun.xml.xsom.XSSchemaSet;
import com.sun.xml.xsom.XSSimpleType;
import com.sun.xml.xsom.XSTerm;
import com.sun.xml.xsom.XSType;
import com.sun.xml.xsom.parser.XSOMParser;
import com.sun.xml.xsom.util.DomAnnotationParserFactory;

/**
 * The class <code>JrxXmlModelUtil</code> is used for processing XML document with XSD.
 * 
 */
public class JrxXmlModelUtil {

    private static final Logger log = LoggerFactory.getLogger(JrxXmlModelUtil.class);

    private Map<String, XSSchema> xsSchemaMap = new HashMap<String, XSSchema>();

    private Map<String, String> namespaceMap = new HashMap<String, String>();

    private Map<String, String> noNamespaceNodeMap = new HashMap<String, String>();

    private Set<String> enrichmentIgnoreList = new HashSet<String>();

    protected XsdToXmlUtil xsd2XmlUtil;

    private boolean toStringWithValue = false;

    public static JrxXmlModelUtil newInstance() {
        return new JrxXmlModelUtil();
    }

    public JrxXmlModelUtil() {
        xsd2XmlUtil = XsdToXmlUtil.getInstance();
    }

    /**
     * Add XML schema from the given URL for further processes. One or more XML schemas should be provided to allow the
     * element schema matching.
     * <p>
     * 
     * @param xmlSchemaUrl
     * @return array of target namespace URI
     * @throws SAXException
     */
    public String[] addSchema(URL xmlSchemaUrl) throws SAXException {
        XSOMParser xsomParser = new XSOMParser(SAXParserFactory.newInstance());
        xsomParser.setAnnotationParser(new DomAnnotationParserFactory());
        xsomParser.parse(xmlSchemaUrl);

        XSSchemaSet schemaSet = xsomParser.getResult();
        if (schemaSet == null) {
            throw new SchemaNotAvailableException();
        }

        StringBuffer namespaceUriBuffer = new StringBuffer();
        // Load all namespaces in XSD
        Iterator<XSSchema> it = schemaSet.iterateSchema();
        while (it.hasNext()) {
            XSSchema xsSchema = (XSSchema) it.next();
            String namespaceUri = addSchema(xsSchema);

            if (namespaceUriBuffer.length() > 0)
                namespaceUriBuffer.append(",");
            namespaceUriBuffer.append(namespaceUri);
        }

        return namespaceUriBuffer.toString().split(",");
    }

    /**
     * Add XML schema for further processes. One or more XML schemas should be provided to allow the element schema
     * matching.
     * <p>
     * 
     * @param XML
     *            Schema object
     * @return target namespace URI from the provided XML Schema
     * @param xsSchema
     */
    public String addSchema(XSSchema xsSchema) {
        String namespace = xsSchema.getTargetNamespace();
        if (!xsSchemaMap.containsKey(namespace)) {
            xsSchemaMap.put(namespace, xsSchema);
        }
        return namespace;
    }

    private JrxElement applySchemaElement(JrxElement jrxElement) {
        if (jrxElement == null) {
            return null;
        }

        if (isRootInNamespace(jrxElement)) {
            // Root element in the schema definition. Get the element
            // declaration directly from schema.
            XSSchema xsSchema = getSchema(jrxElement.getNamespaceUri());

            if (xsSchema == null) {
                log.warn("No Schema available for Namespace URI: " + jrxElement.getNamespaceUri() + " -> "
                        + jrxElement.getName());
                return jrxElement;
            }

            XSElementDecl xsElement = xsSchema.getElementDecl(jrxElement.getSimpleName());
            jrxElement.setXsdDeclaration(xsElement);
            return jrxElement;
        }

        // Look for Schema Element definition from parent.
        XSElementDecl xsParentSchemaElement = getParentSchemaElementDefinition(jrxElement);
        if (xsParentSchemaElement == null) {
            return null;
        }

        // It must be a complex type or else the parent element does not have
        // children.
        XSComplexType xsParentComplexType = (XSComplexType) xsParentSchemaElement.getType();

        // Sibling element(s)
        XSContentType xsParentContentType = xsParentComplexType.getContentType();
        if (xsParentContentType instanceof XSParticle) {
            XSParticle xsParentContentTypeParticle = xsParentContentType.asParticle();

            // Set group multiplicity
            jrxElement.getParentBlock().setMinOccurs(xsParentContentTypeParticle.getMinOccurs());
            jrxElement.getParentBlock().setMaxOccurs(xsParentContentTypeParticle.getMaxOccurs());

            XSTerm xsParentContentTypeTerm = xsParentContentTypeParticle.getTerm();
            if (xsParentContentTypeTerm.isModelGroup()) {
                XSModelGroup xsParentModelGroup = (XSModelGroup) xsParentContentTypeTerm;
                jrxElement.getParentBlock().setXsdDeclaration(xsParentModelGroup);
                return applySchemaElement(jrxElement, xsParentModelGroup);
            } else {
                log.warn("Unhandled Complex Type Term: " + xsParentContentTypeTerm.getClass().getSimpleName() + " -> "
                        + xsParentComplexType.getName());
            }
        } else {
            log.warn("Unhandlex Complex Type Content: " + xsParentContentType.getClass().getSimpleName() + " -> "
                    + xsParentComplexType.getName());
        }

        return jrxElement;
    }

    private JrxElement applySchemaElement(JrxElement jrxElement, XSModelGroup xsParentModelGroup) {
        for (int i = 0; i < xsParentModelGroup.getSize(); i++) {
            XSParticle xsParticle = xsParentModelGroup.getChild(i);
            XSTerm xsTerm = xsParticle.getTerm();
            if (xsTerm instanceof XSElementDecl) {
                XSElementDecl xsElement = (XSElementDecl) xsTerm;
                if (xsElement.getName().equals(jrxElement.getSimpleName())) {
                    // Set Multiplicity
                    jrxElement.setMinOccurs(xsParticle.getMinOccurs());
                    jrxElement.setMaxOccurs(xsParticle.getMaxOccurs());
                    jrxElement.setXsdDeclaration(xsElement);

                    if (Compositor.CHOICE.equals(xsParentModelGroup.getCompositor())) {
                        // jrxElement.setOrigin(xsParentModelGroup);
                        log.debug(jrxElement.getName() + " has choice origin!");
                    }
                    break;
                }
            } else if (xsTerm instanceof XSModelGroup) {
                XSModelGroup xsModelGroup = (XSModelGroup) xsTerm;

                JrxElement jrxElementResult = applySchemaElement(jrxElement, xsModelGroup);

                if (jrxElementResult.getXsdDeclaration() != null) {
                    break;
                }
                // JUMIN: un-finished business with schema definition xs:group
// @formatter:off
//			} else if (xsTerm instanceof XSModelGroupDecl) {
//				XSModelGroupDecl xsGroup = (XSModelGroupDecl) xsTerm;
// @formatter:on
            } else {
                log.warn("Unhandled Model Group Term: " + xsTerm.getClass().getSimpleName() + " -> "
                        + xsParentModelGroup.getLocator().getLineNumber());
            }
        }

        return jrxElement;
    }

    /**
     * Convert JRX document to string.
     * <p>
     * 
     * @param jrxDocument
     * @return
     */
    public String convertDocumentToString(JrxDocument jrxDocument) {
        return convertDocumentToString(jrxDocument, false);
    }

    public String convertDocumentToString(JrxDocument jrxDocument, boolean prettyFormat) {
        StringBuffer sb = new StringBuffer();

        JrxElement jrxRootElement = jrxDocument.getRootElement();
        sb.append(convertJrxElementToString(jrxRootElement));

        String text = sb.toString();

        if (prettyFormat) {
            text = prettyFormat(text);
        }

        return text;
    }

    public static String prettyFormat(String text) {
        String formattedText = "";
        int pos = 0;
        int strLen = text.length();
        String indentStr = "    ";
        String newLine = "\n";
        char chr;

        for (int i = 0; i < strLen; i++) {
            chr = text.charAt(i);

            // if (chr == '}' || chr == ']') {
            if (chr == '}') {
                formattedText = formattedText + newLine;
                pos = pos - 1;

                for (int j = 0; j < pos; j++) {
                    formattedText = formattedText + indentStr;
                }
            }

            formattedText = formattedText + chr;

            // if (chr == '{' || chr == '[' || chr == ',') {
            if (chr == '{' || chr == ',') {
                formattedText = formattedText + newLine;

                // if (chr == '{' || chr == '[') {
                if (chr == '{') {
                    pos = pos + 1;
                }

                for (int k = 0; k < pos; k++) {
                    formattedText = formattedText + indentStr;
                }
            }
        }
        formattedText = formattedText.replaceAll("\\{\\s+\\}", "{}");

        return formattedText;
    }

    private String convertJrxElementGroupToString(JrxElementGroup jrxElementGroup) {
        StringBuffer sb = new StringBuffer();

        if (jrxElementGroup == null)
            return "";

        if (jrxElementGroup instanceof JrxChoiceGroup) {
            JrxChoiceGroup jrxChoiceGroup = (JrxChoiceGroup) jrxElementGroup;
            sb.append(jrxChoiceGroup.getName());
        } else if (XSModelGroup.ALL.equals(jrxElementGroup.getCompositor())) {
            sb.append("[").append(jrxElementGroup.getCompositor().name()).append("]");
        }
        if (jrxElementGroup.isRepetitive()) {
            if (jrxElementGroup.isMandatory()) {
                sb.append("+");
            } else {
                sb.append("*");
            }
        } else {
            if (!jrxElementGroup.isMandatory()) {
                sb.append("?");
            }
        }

        if (jrxElementGroup instanceof JrxChoiceGroup) {
            // show selection
            sb.append("(");
            JrxChoiceGroup jrxChoiceGroup = (JrxChoiceGroup) jrxElementGroup;
            JrxTerm<?> jrxTerm = jrxChoiceGroup.getSelection();
            if (jrxTerm != null) {
                if (jrxTerm instanceof JrxElement) {
                    sb.append(convertJrxElementToString((JrxElement) jrxTerm));
                } else {
                    sb.append(convertJrxElementGroupToString((JrxElementGroup) jrxTerm));
                }
            }
            sb.append(")");
        } else {
            sb.append("{");
            for (int i = 0; i < jrxElementGroup.getElements().size(); i++) {
                if (i > 0)
                    sb.append(",");
                JrxTerm<?> jrxSubTerm = jrxElementGroup.getElements().get(i);
                if (jrxSubTerm instanceof JrxElement) {
                    sb.append(convertJrxElementToString((JrxElement) jrxSubTerm));
                } else if (jrxSubTerm instanceof JrxGroup) {
                    sb.append(convertJrxGroupToString((JrxGroup) jrxSubTerm));
                } else {
                    sb.append(convertJrxElementGroupToString((JrxElementGroup) jrxSubTerm));
                }
            }
            sb.append("}");
        }

        return sb.toString();
    }

    public String convertJrxElementToString(JrxElement jrxElement, boolean prettyFormat) {
        String text = convertJrxElementToString(jrxElement);

        if (prettyFormat) {
            text = prettyFormat(text);
        }
        return text;
    }

    private String convertJrxElementToString(JrxElement jrxElement) {
        StringBuffer sb = new StringBuffer();

        sb.append(jrxElement.getName());
        if (jrxElement.isRepetitive()) {
            if (isElementMandatory(jrxElement)) {
                sb.append("+");
            } else {
                sb.append("*");
            }
        } else {
            if (!isElementMandatory(jrxElement)) {
                sb.append("?");
            }
        }

        // Attributes
        if (jrxElement.getAttributeList().size() > 0)
            sb.append("(");
        for (int i = 0; i < jrxElement.getAttributeList().size(); i++) {
            if (i > 0)
                sb.append(",");
            sb.append("@").append(jrxElement.getAttributeList().get(i));
        }
        if (jrxElement.getAttributeList().size() > 0)
            sb.append(")");

        // Sub-Element
        sb.append(convertJrxElementGroupToString(jrxElement.getChildrenBlock()));

        if (toStringWithValue) {
            if (jrxElement.isLeaf()) {
                sb.append(":'")
                        .append(jrxElement.getXmlElement() == null ? "" : jrxElement.getXmlElement().getTextContent()
                                .trim()).append("'");
            }
        }

        return sb.toString();
    }

    private String convertJrxGroupToString(JrxGroup jrxGroup) {
        StringBuffer sb = new StringBuffer();

        sb.append(jrxGroup.getName());
        if (jrxGroup.isRepetitive()) {
            if (jrxGroup.isMandatory()) {
                sb.append("+");
            } else {
                sb.append("*");
            }
        } else {
            if (!jrxGroup.isMandatory()) {
                sb.append("?");
            }
        }

        sb.append("{");

        // Sub-Element
        if (jrxGroup.getChildrenBlock() != null) {
            boolean isChoice = false;
            if (XSModelGroup.Compositor.CHOICE.equals(jrxGroup.getChildrenBlock().getCompositor())) {
                // sb.append("(");
                isChoice = true;
            }
            for (int i = 0; i < jrxGroup.getChildrenBlock().getElements().size(); i++) {
                JrxTerm<?> jrxSubTerm = jrxGroup.getChildrenBlock().getElements().get(i);
                if (jrxSubTerm instanceof JrxElement) {
                    sb.append(convertJrxElementToString((JrxElement) jrxSubTerm));
                } else if (jrxSubTerm instanceof JrxGroup) {
                    sb.append(convertJrxGroupToString((JrxGroup) jrxSubTerm));
                } else {
                    sb.append(convertJrxElementGroupToString((JrxElementGroup) jrxSubTerm));
                }
                if (isChoice) {
                    sb.append("|");
                } else {
                    sb.append(",");
                }
            }
            char lastChar = sb.charAt(sb.length() - 1);
            if (lastChar == ',' || lastChar == '|') {
                sb.deleteCharAt(sb.length() - 1);
            }
            // if (isChoice) {
            // sb.append(")");
            // }
        }
        char lastChar = sb.charAt(sb.length() - 1);
        if (lastChar == ',' || lastChar == '|') {
            sb.deleteCharAt(sb.length() - 1);
        }
        sb.append("}");

        return sb.toString();
    }

    /**
     * Convert an XML document into JRX document. When the required schema(s) is provided via {@link addSchema()}
     * method, the model will have the link to both XML element and Schema element declaration.
     * <p>
     * 
     * @param xmlDocument
     *            the input XML document
     * @return a model from JrxDocument
     * @throws Exception
     */
    public JrxDocument convertXmlToJrxModel(Document xmlDocument) throws Exception {
        if (xmlDocument == null) {
            return null;
        }

        Element xmlRoot = xmlDocument.getDocumentElement();
        if (xmlRoot == null) {
            return null;
        }

        JrxDocument jrxDoc = new JrxDocument();
        jrxDoc.setXmlDocument(xmlDocument);
        JrxElement jrxRootElement = convertXmlToJrxModel(xmlRoot, null);
        jrxRootElement.setOwnerDocument(jrxDoc);
        jrxDoc.setRootElement(jrxRootElement);

        return jrxDoc;
    }

    /**
     * Convert an XML element into JRX element. When the required schema(s) is provided via {@link addSchema()} method,
     * the model will have the link to both XML element and Schema element declaration.
     * <p>
     * 
     * @param xmlElement
     * @param jrxParentElement
     * @return
     * @throws Exception
     */
    public JrxElement convertXmlToJrxModel(Element xmlElement, JrxElement jrxParentElement) throws Exception {
        if (xmlElement == null) {
            return null;
        }

        String simpleTagName = XmlUtils.getSimpleTagName(xmlElement.getNodeName());
		
        JrxElement jrxElement = null;
        if (jrxParentElement != null && jrxParentElement.getXsdDeclaration() != null) {
            XSParticle xsParticle = getXsElementParticleFromParentBlockByElementName(
                    jrxParentElement.getChildrenBlock(), simpleTagName);
            if (xsParticle == null && jrxParentElement.getChildrenBlock().getXsdDeclaration() != null) {
                XSParticle[] xsParticlePath = getXsParticlePathFromParentBlockByElementName(jrxParentElement
                        .getChildrenBlock().getXsdDeclaration(), simpleTagName);
                if (xsParticlePath.length > 0) {
                    XSTerm xsTerm = xsParticlePath[xsParticlePath.length - 1].getTerm();
                    if (xsTerm.isElementDecl() && xsTerm.asElementDecl().getName().equals(simpleTagName)) {
                        // Build up the nested JrxElementGroup???
                        buildUpNestedElementGroups(jrxParentElement.getChildrenBlock(), xsParticlePath);

                        // Set xsParticle
                        xsParticle = xsParticlePath[xsParticlePath.length - 1];
                    }
                }
            }

            if (xsParticle != null) {
                jrxElement = convertXsElementToJrxElement(xsParticle.getTerm().asElementDecl(), jrxParentElement,
                        xmlElement, false);
                if (jrxElement != null) {
                    jrxElement.setMinOccurs(xsParticle.getMinOccurs());
                    jrxElement.setMaxOccurs(xsParticle.getMaxOccurs());
                }
            }
        }

        if (jrxElement == null) {
            jrxElement = new JrxElement();
            jrxElement.setXmlElement(xmlElement);
            jrxElement.setName(xmlElement.getNodeName());
        }

        if (jrxParentElement == null) {
        } else {
            // Choose the correct JrxElementGroup to insert
            JrxElementGroup jrxEffectiveElementGroup = getScopedElementGroupByElementName(
                    jrxParentElement.getChildrenBlock(), simpleTagName);

            if (jrxEffectiveElementGroup != null && jrxEffectiveElementGroup instanceof JrxChoiceGroup) {
                JrxChoiceGroup jrxParentChoiceGroup = (JrxChoiceGroup) jrxEffectiveElementGroup;
                jrxParentChoiceGroup.setSelection(jrxElement);
            }

            if (jrxEffectiveElementGroup == null) {
                jrxEffectiveElementGroup = jrxParentElement.getChildrenBlock();
            }

            jrxElement.setParentBlock(jrxEffectiveElementGroup);
        }

        // Attribute
        NamedNodeMap xmlAttrs = xmlElement.getAttributes();
        String namespacePrefix = "";
        for (int i = 0; i < xmlAttrs.getLength(); i++) {
            Attr xmlAttr = (Attr) xmlAttrs.item(i);
            String attrName = xmlAttr.getNodeName();
            if (attrName.startsWith(XMLConstants.XMLNS_ATTRIBUTE)) {
                int colonIndex = attrName.indexOf(":");
                if (colonIndex > 0 && colonIndex < (attrName.length() - 1)) {
                    namespacePrefix = attrName.substring(colonIndex + 1);
                }
                String namespaceUri = xmlAttr.getNodeValue();
                jrxElement.setNamespaceUri(namespacePrefix, namespaceUri);

                if (!namespacePrefix.equals("")) {
                    if (!namespaceMap.containsKey(namespacePrefix)) {
                        namespaceMap.put(namespacePrefix, namespaceUri);
                    }
                } else {
                    noNamespaceNodeMap.put(jrxElement.getName(), namespaceUri);
                }

                continue;
            }

            JrxAttribute jrxAttr = null;

            // find out if the attribute is available in JrxElement
            for (JrxAttribute jrxElementAttr : jrxElement.getAttributeList()) {
                if (jrxElementAttr.getName().equals(attrName)) {
                    jrxAttr = jrxElementAttr;
                    break;
                }
            }
            
            if (jrxAttr == null) {
                jrxAttr = new JrxAttribute();
                jrxAttr.setName(attrName);
                jrxAttr.setOwner(jrxElement);
                jrxElement.getAttributeList().add(jrxAttr);
            }
        }

        // Assign namespace URI
        if (jrxElement.getNamespaceUri() == null) {
            if (jrxElement.getNamespacePrefix().equals("")) {
                if (jrxParentElement != null) {
                    // Copy namespace information from parent
                    String parentNamespaceUri = jrxParentElement.getNamespaceUri(jrxElement.getNamespacePrefix());
                    jrxElement.setNamespaceUri(jrxElement.getNamespacePrefix(), parentNamespaceUri);
                    noNamespaceNodeMap.put(jrxElement.getName(), parentNamespaceUri);
                }
            } else {
                // retrieve from namespace map
                jrxElement.setNamespaceUri(jrxElement.getNamespacePrefix(),
                        namespaceMap.get(jrxElement.getNamespacePrefix()));
            }
        }

        // Attach XML Schema declaration
        if (hasSchemas()) {
            applySchemaElement(jrxElement);
        }

        XSElementDecl xsElementDecl = jrxElement.getXsdDeclaration();
        JrxElementGroup jrxElementGroup = null;
        if (xsElementDecl != null) {
            // Only create parent block when the schema defines so.
            jrxElementGroup = enrichJrxElementWithBlockInfo(jrxElement);
        }

        // Sub-Element - Add sub element(s)
        List<Element> xmlSubElementList = XmlUtils.getChildElementList(xmlElement);
        if (xmlSubElementList.size() > 0) {
            if (jrxElementGroup == null) {
                // Create default one
                jrxElementGroup = new JrxElementGroup();
                jrxElementGroup.setCompositor(XSModelGroup.SEQUENCE);
                jrxElementGroup.setOwner(jrxElement);
            }

            if (jrxElementGroup instanceof JrxChoiceGroup) {
                // Choice for the time being is not repeatable. In this context would require further parent
                // element. Set selection
                Element xmlSelectionElement = xmlSubElementList.get(0);
                JrxElement jrxChildElement = convertXmlToJrxModel(xmlSelectionElement, jrxElement);

                if (jrxChildElement.getXsdDeclaration() != null) {
                    XSModelGroup xsModelGroup = jrxElementGroup.getXsdDeclaration();
                    if (xsModelGroup != null) {
                        XSParticle[] xsChildParticles = xsModelGroup.getChildren();
                        for (XSParticle xsChildParticle : xsChildParticles) {
                            if (xsChildParticle.getTerm().equals(jrxChildElement.getXsdDeclaration())) {
                                // Update multiplicities
                                jrxChildElement.setMinOccurs(xsChildParticle.getMinOccurs());
                                jrxChildElement.setMaxOccurs(xsChildParticle.getMaxOccurs());
                            }
                        }
                    }
                }
            } else {
                XSParticle[] xsChildParticles = new XSParticle[] {};
                XSModelGroup xsModelGroup = jrxElementGroup.getXsdDeclaration();
                if (xsModelGroup != null) {
                    xsChildParticles = xsModelGroup.getChildren();
                }
                // copy as child elements
                for (Element xmlSubElement : xmlSubElementList) {
                    JrxElement jrxChildElement = convertXmlToJrxModel(xmlSubElement, jrxElement);
                    if (jrxChildElement.getXsdDeclaration() != null) {
                        for (XSParticle xsChildParticle : xsChildParticles) {
                            if (xsChildParticle.getTerm().equals(jrxChildElement.getXsdDeclaration())) {
                                // Update multiplicities
                                jrxChildElement.setMinOccurs(xsChildParticle.getMinOccurs());
                                jrxChildElement.setMaxOccurs(xsChildParticle.getMaxOccurs());
                            }
                        }
                    }
                }
            }
        }

        return jrxElement;
    }

    /**
     * Get the node path through the parent hierarchy for the same namespace.
     * <p>
     * 
     * @param xmlElement
     * @return an array of node simple name (without namespace) where current element node is last index of the array.
     */
    public String[] getNodePathArrayOnSameNamespace(Element xmlElement) {
        if (xmlElement == null) {
            return null;
        }
        List<String> pathList = new ArrayList<String>();

        String tagName = xmlElement.getNodeName();
        String scopingNamespacePrefix = XmlUtils.getNamespacePrefix(tagName);
        Element xmlCurrentElement = xmlElement;
        while (xmlCurrentElement != null) {
            tagName = xmlCurrentElement.getNodeName();
            String currentNamespacePrefix = XmlUtils.getNamespacePrefix(tagName);
            if (!scopingNamespacePrefix.equals(currentNamespacePrefix)) {
                // Break! Have reach other name space
                break;
            }

            String simpleTagName = XmlUtils.getSimpleTagName(tagName);

            if (pathList.isEmpty()) {
                pathList.add(simpleTagName);
            } else {
                pathList.add(0, simpleTagName);
            }

            if (!(xmlCurrentElement.getParentNode() instanceof Element)) {
                // Break! Have reach the document (root)
                break;
            }
            xmlCurrentElement = (Element) xmlCurrentElement.getParentNode();
        }

        return pathList.toArray(new String[] {});
    }

    private XSElementDecl getParentSchemaElementDefinition(JrxElement jrxElement) {
        if (jrxElement == null || jrxElement.getParentBlock() == null) {
            return null;
        }

        JrxElement jrxParentElement = jrxElement.getParentElement();

        if (jrxParentElement == null)
            return null;

        return jrxParentElement.getXsdDeclaration();
    }

    /**
     * Retrieve XML Schema by a namespace URI.
     * 
     * @param namespaceUri
     * @return XML Schema for the namespace.
     */
    public XSSchema getSchema(String namespaceUri) {
        return xsSchemaMap.get(namespaceUri);
    }

    private boolean isRootInNamespace(JrxElement jrxElement) {
        if (jrxElement.getParentBlock() == null)
            return true;

        if (jrxElement.getParentBlock() != null && jrxElement.getParentBlock().getOwner() != null) {
            JrxElement jrxParentElement = jrxElement.getParentElement();
            if (!jrxParentElement.getNamespacePrefix().equals(jrxElement.getNamespacePrefix())) {
                return true;
            }

            if (jrxParentElement.getNamespaceUri() != null && jrxElement.getNamespaceUri() != null
                    && jrxElement.getNamespaceUri().length() > 0
                    && !jrxParentElement.getNamespaceUri().equals(jrxElement.getNamespaceUri())) {
                return true;
            }
        }

        return false;
    }

    /**
     * Enrich a JRX document with optional elements and choice-blocks based on the schema element definition.
     * 
     * @param jrxDocument
     * @return
     * @throws Exception
     */
    public JrxDocument enrichJrxDocument(JrxDocument jrxDocument) throws Exception {
        if (jrxDocument == null) {
            return null;
        }

        // Enrich with missing elements
        JrxElement jrxRootElement = jrxDocument.getRootElement();
        jrxRootElement = enrichJrxElement(jrxRootElement);
        jrxRootElement = enrichJrxElementRecursive(jrxRootElement);

        return jrxDocument;
    }

    public JrxElement enrichJrxElementRecursive(JrxElement jrxElement) throws Exception {
        if (jrxElement == null) {
            return null;
        }

        JrxElementGroup jrxChildrenBlock = jrxElement.getChildrenBlock();
        if (jrxChildrenBlock == null) {
            return jrxElement;
        }

        XSModelGroup xsModelGroup = jrxChildrenBlock.getXsdDeclaration();

        if (xsModelGroup == null) {
            return jrxElement;
        }

        updateElementGroup(jrxChildrenBlock, xsModelGroup);

        enrichSingleOptionChoiceElement(jrxElement);

        enrichJrxElementGroupRecursive(jrxElement.getChildrenBlock());

        return jrxElement;
    }

    protected void enrichSingleOptionChoiceElement(JrxElement jrxElement) throws Exception {
        JrxElementGroup jrxChildrenBlock = jrxElement.getChildrenBlock();
        if (jrxChildrenBlock instanceof JrxChoiceGroup && ((JrxChoiceGroup) jrxChildrenBlock).getSelection() == null
                && xsd2XmlUtil.countChoiceElementParticle(jrxChildrenBlock.getXsdDeclaration()) == 1) {
            // Choice with 1 item to choose -> Choose it directly
            addChoiceElement(jrxElement, 0);
            JrxTerm<?> jrxChoiceSelectionTerm = ((JrxChoiceGroup) jrxChildrenBlock).getSelection();
            if (jrxChoiceSelectionTerm != null && jrxChoiceSelectionTerm instanceof JrxElement) {
                JrxElement jrxChoiceSelection = (JrxElement) jrxChoiceSelectionTerm;
                jrxChoiceSelection = enrichJrxElement(jrxChoiceSelection);
                jrxChoiceSelection = enrichJrxElementRecursive(jrxChoiceSelection);
            }
        }
    }

    private void enrichJrxElementGroupRecursive(JrxElementGroup jrxElementGroup) throws Exception {
        if (jrxElementGroup == null) {
            return;
        }

        XSModelGroup xsModelGroup = jrxElementGroup.getXsdDeclaration();

        if (xsModelGroup == null) {
            return;
        }

        updateElementGroup(jrxElementGroup, xsModelGroup);

        for (int i = 0; i < jrxElementGroup.getElements().size(); i++) {
            JrxTerm<?> jrxChildTerm = jrxElementGroup.getElements().get(i);
            if (jrxChildTerm instanceof JrxElement) {
                JrxElement jrxChildElement = (JrxElement) jrxChildTerm;
                enrichJrxElement(jrxChildElement);
                enrichJrxElementRecursive(jrxChildElement);
            } else if (jrxChildTerm instanceof JrxChoiceGroup) {
                JrxChoiceGroup jrxChildChoiceGroup = (JrxChoiceGroup) jrxChildTerm;
                if (jrxChildChoiceGroup.getElements().size() > 0
                        && jrxChildChoiceGroup.getElements().get(0) instanceof JrxElement) {
                    JrxTerm<?> jrxChoiceSelectionTerm = jrxChildChoiceGroup.getSelection();
                    if (jrxChoiceSelectionTerm instanceof JrxElement) {
                        enrichJrxElement((JrxElement) jrxChoiceSelectionTerm);
                        enrichJrxElementRecursive((JrxElement) jrxChoiceSelectionTerm);
                    }
                }
            } else if (jrxChildTerm instanceof JrxGroup) {
                enrichJrxGroup((JrxGroup) jrxChildTerm);
                enrichJrxGroupRecursive((JrxGroup) jrxChildTerm);
            }
        }
    }

    public JrxGroup enrichJrxGroupRecursive(JrxGroup jrxGroup) throws Exception {
        if (jrxGroup == null) {
            return null;
        }

        XSModelGroupDecl xsModelGroupDecl = jrxGroup.getXsdDeclaration();

        if (xsModelGroupDecl == null) {
            return jrxGroup;
        }

        enrichJrxElementGroupRecursive(jrxGroup.getChildrenBlock());

        return jrxGroup;
    }

    public JrxElement enrichJrxElement(JrxElement jrxElement) throws Exception {
        if (jrxElement == null) {
            return null;
        }
        
        XSElementDecl xsElement = jrxElement.getXsdDeclaration();
        if (xsElement == null) {
            return jrxElement;
        }

        enrichJrxElementWithBlockInfo(jrxElement);

        XSType xsType = xsElement.getType();
        if (xsType.isSimpleType()) {
            return jrxElement;
        }

        XSComplexType xsComplexType = xsType.asComplexType();

        // Attribute
        enrichAttributeFromXsComplexType(xsComplexType, jrxElement);

        syncDeclarationFromXsd(jrxElement);

        return jrxElement;
    }

    private <T extends JrxDeclaration<?>> T syncDeclarationFromXsd(T jrxDeclaration) throws Exception {
        if (jrxDeclaration.getChildrenBlock() == null) {
            return jrxDeclaration;
        }

        XSModelGroup xsModelGroup = jrxDeclaration.getChildrenBlock().getXsdDeclaration();

        if (xsModelGroup != null) {
            // synchronize XSModelGroup info
            // updateJrxElementGroup(jrxElement.getChildrenBlock(),
            // xsModelGroup);
        }

        JrxXmlModelMatchingUtil matchingUtil = JrxXmlModelMatchingUtil.getInstance();
        if (matchingUtil.performMatching(jrxDeclaration.getChildrenBlock())) {

            if (!matchingUtil.isMergeRequired()) {
                if (jrxDeclaration.getChildrenBlock().isChoiceGroup()) {
                    JrxTerm<?> jrxSelection = ((JrxChoiceGroup) jrxDeclaration.getChildrenBlock()).getSelection();
                    if (jrxSelection != null) {
                        if (jrxSelection instanceof JrxElement) {
                            enrichJrxElement((JrxElement) jrxSelection);
                        }
                    }
                }

                return jrxDeclaration; // Skip merging
            }

            List<XSParticle> dummyElementForChoice = new ArrayList<XSParticle>();
            List<Integer> dummyIndexForChoice = new ArrayList<Integer>();
            List<XSParticle> dummyElementForGroup = new ArrayList<XSParticle>();
            List<Integer> dummyIndexForGroup = new ArrayList<Integer>();

            int memberSize = matchingUtil.getSchemaParticleArray().length;

            @SuppressWarnings("unchecked")
            List<Object>[] mergedElementTable = new List[memberSize];
            // Merging existing jrx element with new element and additionally
            // promote choice group to the existing jrx element.
            for (int i = 0; i < memberSize; i++) {
                List<JrxTerm<?>> jrxTermList = matchingUtil.getExistingTermArray()[i];
                mergedElementTable[i] = new ArrayList<Object>();
                if (jrxTermList.size() < 1) {
                    XSParticle xsChildParticle = matchingUtil.getNewParticleArray()[i];
                    if (xsChildParticle == null) {
                        // particle has been defined!
                        log.error("Matching table error for element: " + jrxDeclaration.getName()
                                + "! If you found this text, please report to developer!");
                        continue;
                    }
                    // New member to be included in the element
                    // It could be an option to only include the mandatory
                    // sub-element(s) only
                    XSTerm xsChildTerm = xsChildParticle.getTerm();
                    if (xsChildTerm instanceof XSModelGroup) {
                        XSModelGroup xsChildModelGroup = xsChildTerm.asModelGroup();
                        if (xsChildModelGroup.getCompositor().equals(XSModelGroup.CHOICE)) {
                            if (xsChildParticle.getMaxOccurs().intValue() > 1 || xsChildParticle.getMaxOccurs().intValue() == -1) {
                                // Register particle and index for dummy
                                // creation
                                dummyElementForChoice.add(xsChildParticle);
                                dummyIndexForChoice.add(i);
                            }
                        }
                    } else if (xsChildTerm instanceof XSModelGroupDecl) {
                        dummyElementForGroup.add(xsChildParticle);
                        dummyIndexForGroup.add(i);
                    }
                    mergedElementTable[i].add(xsChildParticle);
                } else {
                    if (jrxTermList.size() < 1) {
                        continue;
                    }
                    XSParticle[] xsSchemaParticleArray = matchingUtil.getSchemaParticleArray();
                    XSParticle xsSchemaParticle = xsSchemaParticleArray[i];
                    XSTerm xsSchemaTerm = xsSchemaParticle.getTerm();
                    if (xsSchemaTerm.isModelGroup()) {
                        XSModelGroup xsSchemaModelGroup = xsSchemaTerm.asModelGroup();
                        if (xsSchemaModelGroup.getCompositor().equals(Compositor.CHOICE)) {
                            if (jrxTermList.get(0) instanceof JrxChoiceGroup) {
                                // The element is a JrxChoiceGroup. It has been solved to the proper jrx choice.
                                mergedElementTable[i].add(jrxTermList.get(0));
                            } else {
                                // The element is a JrxElement, choice has not yet been resolved.
                                JrxChoiceGroup jrxPromotedChoiceGroup = null;
                                if (jrxTermList.size() > 1) {
                                    JrxElementGroup jrxPromotedChoiceGroupChildrenBlock = null;
                                    for (int j = 0; j < jrxTermList.size(); j++) {
                                        JrxElement jrxChildElement = (JrxElement) jrxTermList.get(j);
                                        JrxElementGroup jrxChildContainer = jrxChildElement.getParentBlock();
                                        int childIndex = jrxChildContainer.getElements().indexOf(jrxChildElement);
                                        jrxChildContainer.getElements().remove(childIndex);
                                        if (jrxPromotedChoiceGroup == null) {
                                            jrxPromotedChoiceGroup = createJrxChoiceGroup(xsSchemaModelGroup,
                                                    jrxDeclaration, childIndex, xsSchemaParticle.getMinOccurs().intValue(),
                                                    xsSchemaParticle.getMaxOccurs().intValue());
                                            jrxPromotedChoiceGroupChildrenBlock = promoteElementGroupForChoiceGroup(
                                                    jrxPromotedChoiceGroup, jrxChildElement);
                                            mergedElementTable[i].add(jrxPromotedChoiceGroup);
                                        }
                                        if (jrxPromotedChoiceGroupChildrenBlock != null) {
                                            enrichJrxElementWithBlockInfo(jrxChildElement);
                                            jrxPromotedChoiceGroupChildrenBlock.getElements().add(jrxChildElement);
                                            jrxChildElement.setParentBlock(jrxPromotedChoiceGroupChildrenBlock);
                                        }
                                    }
                                } else {
                                    JrxElement jrxChildElement = (JrxElement) jrxTermList.get(0);
                                    JrxElementGroup jrxChildContainer = jrxChildElement.getParentBlock();
                                    int childIndex = -1;
                                    if (jrxChildContainer != null) {
                                        childIndex = jrxChildContainer.getElements().indexOf(jrxChildElement);
                                        jrxChildContainer.getElements().remove(childIndex);
                                    }

                                    jrxPromotedChoiceGroup = createJrxChoiceGroup(xsSchemaModelGroup, jrxDeclaration,
                                            childIndex, xsSchemaParticle.getMinOccurs().intValue(),
                                            xsSchemaParticle.getMaxOccurs().intValue());

                                    enrichJrxElementWithBlockInfo(jrxChildElement);
                                    jrxPromotedChoiceGroup.setSelection(jrxChildElement);
                                    jrxChildElement.setOrigin(jrxPromotedChoiceGroup);
                                    jrxChildElement.setParentBlock(jrxPromotedChoiceGroup);
                                    mergedElementTable[i].add(jrxPromotedChoiceGroup);
                                }
                            }
                        } else {
                            // Ignore additional ALL or SEQUENCE group
                            mergedElementTable[i].addAll(jrxTermList);
                        }
                    } else if (xsSchemaTerm.isModelGroupDecl()) {
                        if (jrxTermList.get(0) instanceof JrxGroup) {
                            // The element is a JrxGroup. It has been resolved.
                            mergedElementTable[i].addAll(jrxTermList);
                        } else {
                            // The element has not yet been resolved.
                            JrxGroup jrxPromotedGroup = null;

                            for (JrxTerm<?> jrxChildTerm : jrxTermList) {
                                // JrxElement jrxChildElement = (JrxElement) jrxTermList.get(j);
                                if (jrxPromotedGroup == null) {
                                    JrxElementGroup jrxChildContainer = jrxChildTerm.getParentBlock();
                                    int childIndex = jrxChildContainer.getElements().indexOf(jrxChildTerm);
                                    jrxChildContainer.getElements().remove(childIndex);

                                    // Promote JrxGroup
                                    jrxPromotedGroup = createJrxGroup((XSModelGroupDecl) xsSchemaTerm, jrxDeclaration,
                                            childIndex, xsSchemaParticle.getMinOccurs().intValue(),
                                            xsSchemaParticle.getMaxOccurs().intValue());
                                    mergedElementTable[i].add(jrxPromotedGroup);
                                }
                                if (jrxChildTerm instanceof JrxElement)
                                    enrichJrxElementWithBlockInfo((JrxElement) jrxChildTerm);
                                jrxPromotedGroup.getChildrenBlock().getElements().add(jrxChildTerm);
                                jrxChildTerm.setParentBlock(jrxPromotedGroup.getChildrenBlock());
                            }
                        }
                    } else {
                        mergedElementTable[i].addAll(jrxTermList);
                        if (!xsSchemaTerm.isElementDecl()) {
                            log.warn("Unsupported schema term: " + xsSchemaTerm.getClass().getSimpleName()
                                    + " for enrichment of jrx element: " + jrxDeclaration.getName());
                        }
                    }
                }
            }

            while (!jrxDeclaration.getChildrenBlock().getElements().isEmpty()) {
                jrxDeclaration.getChildrenBlock().getElements().remove(0);
            }
            int xmlIndex = 0;

            // Search for correct prefix.
            JrxElement jrxRefElement = null;
            if (jrxDeclaration instanceof JrxElement) {
                jrxRefElement = (JrxElement) jrxDeclaration;
            } else {
                jrxRefElement = jrxDeclaration.getParentElement();
            }

            for (int i = 0; i < mergedElementTable.length; i++) {
                List<Object> newChildElementList = mergedElementTable[i];
                for (Object newChildElement : newChildElementList) {
                    JrxTerm<?> jrxChildTerm = null;
                    if (newChildElement instanceof JrxTerm) {
                        jrxChildTerm = (JrxTerm<?>) newChildElement;
                        if (jrxChildTerm instanceof JrxElement) {
                            enrichJrxElementWithBlockInfo((JrxElement) jrxChildTerm);
                        }
                        jrxDeclaration.getChildrenBlock().getElements().add((JrxTerm<?>) newChildElement);
                        xmlIndex++;
                    } else if (newChildElement instanceof XSParticle) {
                        try {
                            XSParticle xsNewChildParticle = (XSParticle) newChildElement;
                            jrxChildTerm = createJrxTermFromParticle(xsNewChildParticle, jrxDeclaration, xmlIndex);
                            if (jrxChildTerm instanceof JrxElement) {
                                JrxElement jrxChildElement = (JrxElement) jrxChildTerm;
                                Element xmlExpectedChildElement = jrxChildElement.getXmlElement();
                                Element xmlElement = (Element) xmlExpectedChildElement.getParentNode();
                                try {
                                    Element xmlActualChildElement = (Element) XmlUtils.getNodeByXPath(xmlElement, "./*["
                                            + (xmlIndex + 1) + "]");
                                    if (!xmlExpectedChildElement.equals(xmlActualChildElement)) {
                                        // Not at the proper index, move
                                        xmlElement.insertBefore(xmlExpectedChildElement, xmlActualChildElement);
                                    }
                                    xmlIndex++;
                                } catch (Exception e) {
                                    log.warn("Error in retrieving xml child elements for: " + jrxDeclaration.getName());
                                }
                                jrxChildElement.setOwnerDocument(jrxRefElement.getOwnerDocument());
                            } else {
                                // Choice-element
                                xmlIndex++;
                            }
                        } catch (UnhandledGroupTermException e) {
                            log.warn(e.getMessage() + " -> " + e.getXsTerm().getLocator().getLineNumber());
                        }
                    }
                }
            }
        }

        return jrxDeclaration;
    }

    public JrxGroup enrichJrxGroup(JrxGroup jrxGroup) throws Exception {
        if (jrxGroup == null) {
            return null;
        }

        XSModelGroupDecl xsGroup = jrxGroup.getXsdDeclaration();
        if (xsGroup == null) {
            return jrxGroup;
        }

        enrichJrxGroupWithBlockInfo(jrxGroup);

        syncDeclarationFromXsd(jrxGroup);

        return jrxGroup;
    }

    private void updateElementGroup(JrxElementGroup jrxElementGroup, XSModelGroup xsModelGroup) {
        jrxElementGroup.setCompositor(xsModelGroup.getCompositor());
    }

    protected JrxElementGroup enrichJrxElementWithBlockInfo(JrxElement jrxElement) throws Exception {
        JrxElementGroup jrxElementGroup = null;

        if (jrxElement.getXsdDeclaration() == null) {
            // No Schema element defined, return!
            return null;
        }

        XSType xsType = jrxElement.getXsdDeclaration().getType();
        if (xsType.isSimpleType()) {
            // Is a simple type or the children block has been defined, return!
            return null;
        }

        XSComplexType xsComplexType = xsType.asComplexType();

        enrichAttributeFromXsComplexType(xsComplexType, jrxElement);

        if (!(xsComplexType.getContentType() instanceof XSParticle)) {
            // This complex type is based on simple type and does not have
            // further sub-elements but it may have it's
            // own attribute declaration.
            return null;
        }

        XSParticle xsParticle = xsComplexType.getContentType().asParticle();
        XSTerm xsTerm = xsParticle.getTerm();
        XSModelGroup xsModelGroup = xsTerm.asModelGroup();
        if (jrxElement.getChildrenBlock() != null && XSModelGroup.CHOICE.equals(xsModelGroup.getCompositor())
                && !XSModelGroup.CHOICE.equals(jrxElement.getChildrenBlock().getCompositor())) {
            JrxChoiceGroup jrxChoiceGroup = new JrxChoiceGroup();
            jrxChoiceGroup.setMinOccurs(xsParticle.getMinOccurs());
            jrxChoiceGroup.setMaxOccurs(xsParticle.getMaxOccurs());
            jrxChoiceGroup.setCompositor(XSModelGroup.CHOICE);
            jrxChoiceGroup.setXsdDeclaration(xsTerm.asModelGroup());

            // Move existing selection
            if (jrxElement.getChildrenBlock().getElements().size() > 0) {
                JrxTerm<?> jrxSelectedElement = jrxElement.getChildrenBlock().getElements().get(0);
                jrxChoiceGroup.getElements().add(jrxSelectedElement);
                jrxChoiceGroup.setSelection(jrxSelectedElement);
            }

            jrxElementGroup = jrxChoiceGroup;
        } else if (jrxElement.getChildrenBlock() == null) {
            // Promote a children block to the element because the datatype =
            // complex type and there is no children
            // block defined.
            jrxElementGroup = createJrxElementGroup(xsModelGroup, xsParticle.getMinOccurs().intValue(), xsParticle.getMaxOccurs().intValue());

// @formatter:off
//			Element xmlElement = jrxElement.getXmlElement();
//			if (xmlElement != null) {
//				List<Element> xmlSubElementList = XmlUtils.getChildElementList(xmlElement);
//				if (xmlSubElementList.size() > 0) {
//					if (jrxElementGroup instanceof JrxChoiceGroup) {
//						// set selection
//						Element xmlSelectionElement = xmlSubElementList.get(0);
//						convertXmlToJrxModel(xmlSelectionElement, jrxElement);
//					} else {
//						// copy as child elements
//						for (Element xmlSubElement : xmlSubElementList) {
//							convertXmlToJrxModel(xmlSubElement, jrxElement);
//						}
//					}
//				}
//			}
// @formatter:on

            jrxElement.setChildrenBlock(jrxElementGroup);
        } else {
            // When the children block is not null/empty, leave it as it is. Let's the enrichJrxElement(JrxElement) do
            // the job.
            // Careful of synchronizing XML-XSD-JRXModel.
            jrxElementGroup = jrxElement.getChildrenBlock();
        }

        return jrxElementGroup;
    }

    protected void enrichJrxElementGroupWithBlockInfo(JrxElement jrxElement) {
        if (jrxElement.getXsdDeclaration() == null) {
            // No Schema element defined, return!
            return;
        }
        XSType xsType = jrxElement.getXsdDeclaration().getType();
        if (xsType.isSimpleType()
                || (jrxElement.getChildrenBlock() != null && jrxElement.getChildrenBlock().getXsdDeclaration() != null)) {
            // Is a simple type or the children block has been defined, return!
            return;
        }

        XSComplexType xsComplexType = xsType.asComplexType();

        if (!(xsComplexType.getContentType() instanceof XSParticle)) {
            // This complex type is based on simple type and does not have
            // further sub-elements but it may have it's
            // own attribute declaration.
            return;
        }

        XSParticle xsParticle = xsComplexType.getContentType().asParticle();
        XSTerm xsTerm = xsParticle.getTerm();
        // Promote a children block to the element because the datatype =
        // complex type and there is no children
        // block defined.
        XSModelGroup xsModelGroup = xsTerm.asModelGroup();
        if (jrxElement.getChildrenBlock() == null) {
            jrxElement.setChildrenBlock(createJrxElementGroup(xsModelGroup, xsParticle.getMinOccurs().intValue(),
                    xsParticle.getMaxOccurs().intValue()));
        } else if (jrxElement.getChildrenBlock() != xsModelGroup) {
            jrxElement.getChildrenBlock().setXsdDeclaration(xsModelGroup);
        }
    }

    protected void enrichJrxGroupWithBlockInfo(JrxGroup jrxGroup) {
        if (jrxGroup.getXsdDeclaration() == null) {
            // No Schema element defined, return!
            return;
        }
        if (jrxGroup.getChildrenBlock() != null) {
            // The children block has been defined, return!
            return;
        }

        XSModelGroup xsModelGroup = jrxGroup.getXsdDeclaration().getModelGroup();
        JrxElementGroup jrxElementGroup = createJrxElementGroup(xsModelGroup, 1, 1);
        jrxGroup.setChildrenBlock(jrxElementGroup);
    }

    private void enrichAttributeFromXsComplexType(XSComplexType xsComplexType, JrxElement jrxElement) {
        // Attribute
        XSAttributeUse[] attrArray = xsComplexType.getAttributeUses().toArray(new XSAttributeUse[] {});
        for (int i = 0; i < attrArray.length; i++) {
            XSAttributeUse xsAttr = attrArray[i];
            String attributeName = xsAttr.getDecl().getName();
            
            JrxAttribute jrxFoundAttribute = null;
            for (JrxAttribute jrxAttribute : jrxElement.getAttributeList()) {
                if (jrxAttribute.getSimpleName().equals(attributeName)) {
                    jrxFoundAttribute = jrxAttribute;
                    // found the attribute, check the data type
                    if (jrxFoundAttribute.getXsdDeclaration() == null) {
                        jrxFoundAttribute.setXsdDeclaration(xsAttr.getDecl());
                    }
                    break;
                }
            }

            if (jrxFoundAttribute != null) {
                jrxFoundAttribute.setMandatory(xsAttr.isRequired());
                continue;
            }

            // create new attribute
            jrxFoundAttribute = new JrxAttribute();
            jrxFoundAttribute.setMandatory(xsAttr.isRequired());
            jrxFoundAttribute.setName(attributeName);
            jrxFoundAttribute.setXsdDeclaration(xsAttr.getDecl());
            // to ensure the sequence is correct, 1st add to list and then
            // setOwner.
            jrxElement.getAttributeList().add(i, jrxFoundAttribute);
            jrxFoundAttribute.setOwner(jrxElement);
            
            // Initialize attribute value
            if (!jrxElement.getXmlElement().hasAttribute(attributeName)) {
                jrxElement.getXmlElement().setAttribute(attributeName, "");
            }

            if (xsAttr.getDefaultValue() != null && xsAttr.getDefaultValue().value != null) {
                // Set default value? Set fix value?
                jrxElement.getXmlElement().setAttribute(attributeName, xsAttr.getDefaultValue().value);
            }
        }
    }

    /**
     * @param xsChildElement
     * @param jrxElement
     */
    public JrxElement convertXsElementToJrxElement(XSElementDecl xsElement, JrxDeclaration<?> jrxParentDeclaration) {
        return convertXsElementToJrxElement(xsElement, jrxParentDeclaration, null, true);
    }

    public JrxElement convertXsElementToJrxElement(XSElementDecl xsElement, JrxDeclaration<?> jrxParentDeclaration,
            Element xmlElement, boolean recursive) {
        if (xsElement == null || jrxParentDeclaration == null) {
            return null;
        }

        JrxElement jrxElement = new JrxElement();
        jrxElement.setXsdDeclaration(xsElement);

        // Search for correct prefix.
        String namespaceUri = xsElement.getTargetNamespace();
        String namespacePrefix = findProperNamespacePrefixFromTargetNamespace(jrxParentDeclaration, namespaceUri);

        if (xmlElement == null) {
            jrxElement.setName(namespacePrefix + (namespacePrefix.equals("") ? "" : ":") + xsElement.getName());
        } else {
            jrxElement.setName(xmlElement.getTagName());
        }
        Map<String, String> namespace = new HashMap<String, String>();
        namespace.put(namespacePrefix, namespaceUri);
        jrxElement.setNamespace(namespace);

        JrxElement jrxParentElement = null;
        if (jrxParentDeclaration instanceof JrxElement) {
            jrxParentElement = (JrxElement) jrxParentDeclaration;
        } else {
            jrxParentElement = jrxParentDeclaration.getParentElement();
        }
        boolean refreshDocumentIndexTable = false;
        if (jrxParentElement != null) {
            jrxElement.setOwnerDocument(jrxParentElement.getOwnerDocument());
            refreshDocumentIndexTable = true;
        }
        
        Element xmlEffectiveElement = xmlElement;

        if (xmlEffectiveElement == null) {
            Element xmlParentElement = jrxParentElement.getXmlElement();
            xmlEffectiveElement = XmlUtils.createSubElement(xmlParentElement, namespaceUri, jrxElement.getName());
        }
        jrxElement.setXmlElement(xmlEffectiveElement);

        if (refreshDocumentIndexTable && jrxElement.getOwnerDocument() != null) {
            jrxElement.getOwnerDocument().put(xmlEffectiveElement, jrxElement);
        }
        
        XSType xsType = xsElement.getType();

        if (xsType.isSimpleType()) {
            return jrxElement;
        }

        if (!xsType.isComplexType()) {
            log.warn("Unhandled type: " + xsType.getClass().getSimpleName() + "for: " + xsElement.getTargetNamespace()
                    + ":" + xsElement.getName());
        }

        XSComplexType xsComplexType = xsType.asComplexType();

        // Attribute
        enrichAttributeFromXsComplexType(xsComplexType, jrxElement);

        XSContentType xsContentType = xsComplexType.getContentType();
        if (xsContentType instanceof XSParticle) {
            XSParticle xsContentTypeParticle = xsContentType.asParticle();

            // Set group multiplicity
            jrxElement.setMinOccurs(xsContentTypeParticle.getMinOccurs());
            jrxElement.setMaxOccurs(xsContentTypeParticle.getMaxOccurs());

            XSTerm xsContentTypeTerm = xsContentTypeParticle.getTerm();
            if (xsContentTypeTerm.isModelGroup()) {
                XSModelGroup xsModelGroup = (XSModelGroup) xsContentTypeTerm;

                int minOccurs = xsContentTypeParticle.getMinOccurs().intValue();
                int maxOccurs = xsContentTypeParticle.getMaxOccurs().intValue();

                if (jrxElement.getChildrenBlock() == null) {
                    JrxElementGroup jrxElementGroup = createJrxElementGroup(xsModelGroup, minOccurs, maxOccurs);
                    jrxElement.setChildrenBlock(jrxElementGroup);
                }

                if (recursive && xmlElement == null) {
                    if (!XSModelGroup.CHOICE.equals(xsModelGroup.getCompositor())) {
                        // Only when it is not a choice
                        for (int i = 0; i < xsModelGroup.getSize(); i++) {
                            XSParticle xsChildParticle = xsModelGroup.getChild(i);
                            try {
                                createJrxTermFromParticle(xsChildParticle, jrxElement, -1);
                            } catch (UnhandledGroupTermException e) {
                                log.warn(e.getMessage() + " -> " + xsModelGroup.getLocator().getLineNumber());
                            }
                        }
                    }
                }
            }
        }

        return jrxElement;
    }

    public JrxGroup convertXsGroupToJrxGroup(XSModelGroupDecl xsGroup, JrxDeclaration<?> jrxParentDeclaration) {
        if (xsGroup == null || jrxParentDeclaration == null) {
            return null;
        }

        JrxGroup jrxGroup = new JrxGroup();
        jrxGroup.setXsdDeclaration(xsGroup);

        // Set group multiplicity
        jrxGroup.setMinOccurs(1);
        jrxGroup.setMaxOccurs(1);

        XSModelGroup xsModelGroup = xsGroup.getModelGroup();

        if (jrxGroup.getChildrenBlock() == null) {
            jrxGroup.setChildrenBlock(createJrxElementGroup(xsModelGroup, 1, 1));
        }

        // JUMIN: un-finished business with schema definition xs:group
// @formatter:off
//		for (int i = 0; i < xsModelGroup.getSize(); i++) {
//			XSParticle xsChildParticle = xsModelGroup.getChild(i);
//			try {
//				createJrxTermFromParticle(xsChildParticle, jrxElement, -1);
//			} catch (UnhandledGroupTerm e) {
//				log.warn(e.getMessage() + " -> " + xsModelGroup.getLocator().getLineNumber());
//			}
//		}
// @formatter:on
        return jrxGroup;
    }

    private JrxElement createJrxElement(XSElementDecl xsElement, JrxDeclaration<?> jrxParentDeclaration, int index,
            int minOccurs, int maxOccurs) {
        JrxElement jrxChildElement = convertXsElementToJrxElement(xsElement, jrxParentDeclaration);

        jrxChildElement.setMinOccurs(minOccurs);
        jrxChildElement.setMaxOccurs(maxOccurs);

        if (index >= 0 && index < jrxParentDeclaration.getChildrenBlock().getElements().size()) {
            // to ensure the sequence is correct, 1st add to list and then
            // setParent.
            jrxParentDeclaration.getChildrenBlock().getElements().add(index, jrxChildElement);
            jrxChildElement.setParentBlock(jrxParentDeclaration.getChildrenBlock());
            jrxChildElement.setOwnerDocument(jrxParentDeclaration.getParentElement().getOwnerDocument());

            // Search for correct prefix.
            JrxElement jrxParentElement = null;
            if (jrxParentDeclaration instanceof JrxElement) {
                jrxParentElement = (JrxElement) jrxParentDeclaration;
            } else {
                jrxParentElement = jrxParentDeclaration.getParentElement();
            }

            Element xmlParentElement = jrxParentElement.getXmlElement();
            Element xmlChildElement = jrxChildElement.getXmlElement();

            JrxTerm<?> jrxChildNextSiblingTerm = jrxParentDeclaration.getChildrenBlock().getElements().get(index + 1);
            if (jrxChildNextSiblingTerm != null && jrxChildNextSiblingTerm instanceof JrxElement) {
                JrxElement jrxChildNextSiblingElement = (JrxElement) jrxChildNextSiblingTerm;
                Element xmlChildNextSiblingElement = jrxChildNextSiblingElement.getXmlElement();
                if (xmlChildNextSiblingElement != null)
                    xmlParentElement.insertBefore(xmlChildElement, xmlChildNextSiblingElement);
            }
        } else {
            // append to the end
            jrxChildElement.setParentBlock(jrxParentDeclaration.getChildrenBlock());
        }
        return jrxChildElement;
    }

    private JrxChoiceGroup createJrxChoiceGroup(XSModelGroup xsModelGroup, JrxDeclaration<?> jrxParentDeclaration,
            int index, int minOccurs, int maxOccurs) {
        // Create dummy/helper node
        JrxChoiceGroup jrxChoiceGroup = createJrxChoiceGroup(xsModelGroup, minOccurs, maxOccurs);

        if (index >= 0 && index < jrxParentDeclaration.getChildrenBlock().getElements().size()) {
            // to ensure the sequence is correct, 1st add to list and then
            // setParent.
            jrxParentDeclaration.getChildrenBlock().getElements().add(index, jrxChoiceGroup);
            jrxChoiceGroup.setParentBlock(jrxParentDeclaration.getChildrenBlock());
        } else {
            jrxChoiceGroup.setParentBlock(jrxParentDeclaration.getChildrenBlock());
        }
        return jrxChoiceGroup;
    }

// @formatter:off
//	private JrxChoiceGroup createJrxChoiceGroupDirectUnderParentDeclaration(XSModelGroup xsModelGroup,
//	        JrxDeclaration<?> jrxParentDeclaration, int minOccurs, int maxOccurs) {
//		// Create dummy/helper node
//		JrxChoiceGroup jrxChoiceGroup = createJrxChoiceGroup(xsModelGroup, minOccurs, maxOccurs);
//
//		jrxParentDeclaration.setChildrenBlock(jrxChoiceGroup);
//
//		return jrxChoiceGroup;
//	}
// @formatter:on

    private JrxChoiceGroup createJrxChoiceGroup(XSModelGroup xsModelGroup, int minOccurs, int maxOccurs) {
        // Create dummy/helper node
        JrxChoiceGroup jrxChoiceGroup = new JrxChoiceGroup();
        jrxChoiceGroup.setXsdDeclaration(xsModelGroup);
        jrxChoiceGroup.setCompositor(xsModelGroup.getCompositor());
        jrxChoiceGroup.setMinOccurs(minOccurs);
        jrxChoiceGroup.setMaxOccurs(maxOccurs);

        return assignNameToChoiceGroup(jrxChoiceGroup);
    }

    private JrxGroup createJrxGroup(XSModelGroupDecl xsModelGroupDecl, JrxDeclaration<?> jrxParentDeclaration,
            int index, int minOccurs, int maxOccurs) {
        // Create dummy/helper node
        JrxGroup jrxGroup = new JrxGroup();
        jrxGroup.setXsdDeclaration(xsModelGroupDecl);
        jrxGroup.setMinOccurs(minOccurs);
        jrxGroup.setMaxOccurs(maxOccurs);

        enrichJrxGroupWithBlockInfo(jrxGroup);

        if (index >= 0 && index < jrxParentDeclaration.getChildrenBlock().getElements().size()) {
            // to ensure the sequence is correct, 1st add to list and then
            // setParent.
            jrxParentDeclaration.getChildrenBlock().getElements().add(index, jrxGroup);
            jrxGroup.setParentBlock(jrxParentDeclaration.getChildrenBlock());
        } else {
            jrxGroup.setParentBlock(jrxParentDeclaration.getChildrenBlock());
        }
        return jrxGroup;
    }

    private JrxTerm<?> createJrxTermFromParticle(XSParticle xsParticle, JrxDeclaration<?> jrxParentDeclaration,
            int index) throws UnhandledGroupTermException {
        XSTerm xsTerm = xsParticle.getTerm();
        if (xsTerm instanceof XSElementDecl) {
            return createJrxElement((XSElementDecl) xsTerm, jrxParentDeclaration, index, xsParticle.getMinOccurs().intValue(),
                    xsParticle.getMaxOccurs().intValue());
        } else if (xsTerm instanceof XSModelGroup) {
            XSModelGroup xsModelGroup = xsTerm.asModelGroup();
            if (xsModelGroup.getCompositor().equals(XSModelGroup.CHOICE)) {
                return createJrxChoiceGroup(xsModelGroup, jrxParentDeclaration, index, xsParticle.getMinOccurs().intValue(),
                        xsParticle.getMaxOccurs().intValue());
            } else {
                // Not yet needed! // Aggregate-up all the elements inside the model group
                for (int i = 0; i < xsModelGroup.getSize(); i++) {
                    XSParticle xsAdditionalParticle = xsModelGroup.getChild(i);
                    createJrxTermFromParticle(xsAdditionalParticle, jrxParentDeclaration, -1);
                }
            }
        } else if (xsTerm instanceof XSModelGroupDecl) {
            return createJrxGroup((XSModelGroupDecl) xsTerm, jrxParentDeclaration, index, xsParticle.getMinOccurs().intValue(),
                    xsParticle.getMaxOccurs().intValue());
        } else {
            throw new UnhandledGroupTermException(xsTerm);
        }

        return null;
    }

    public static int countTermWithSameName(JrxElementGroup jrxScopingElementGroup, String elementName) {
        int count = 0;

        for (JrxTerm<?> jrxTerm : jrxScopingElementGroup.getElements()) {
            if (jrxTerm instanceof JrxDeclaration<?> && ((JrxDeclaration<?>) jrxTerm).getName().equals(elementName)) {
                count++;
            } else if (jrxTerm instanceof JrxChoiceGroup && ((JrxChoiceGroup) jrxTerm).getName().equals(elementName)) {
                count++;
            }
        }

        return count;
    }

    public static int countTermWithSameSchemaDefinition(JrxElementGroup jrxScopingElementGroup, XSTerm xsTerm) {
        return getTermsWithSameSchemaDefinition(jrxScopingElementGroup, xsTerm).length;
    }

    public static int countElementWithSameSchemaDefinition(JrxElementGroup jrxScopingElementGroup,
            XSDeclaration xsDeclaration) {
        return getDeclarationsWithSameSchemaDefinition(jrxScopingElementGroup, xsDeclaration).length;
    }

    /**
     * Collects all term within a particular scoping element group that has same XSTerm definition
     * 
     * @param jrxScopingElementGroup
     * @param xsTerm
     * @return Array of JrxTerm in a proper sequence
     */
    public static JrxTerm<?>[] getTermsWithSameSchemaDefinition(JrxElementGroup jrxScopingElementGroup, XSTerm xsTerm) {
        if (xsTerm == null)
            return new JrxTerm<?>[] {}; // return only empty array, never NULL

        List<JrxTerm<?>> jrxTermList = new ArrayList<JrxTerm<?>>();

        for (JrxTerm<?> jrxTerm : jrxScopingElementGroup.getElements()) {
            if (jrxTerm.getXsdDeclaration() != null && jrxTerm.getXsdDeclaration().equals(xsTerm)) {
                jrxTermList.add(jrxTerm);
            }
        }

        return jrxTermList.toArray(new JrxTerm<?>[] {});
    }

    public static int getElementWithSameNameIndex(JrxElementGroup jrxElementGroup, JrxDeclaration<?> jrxDeclaration) {
        int index = 0;

        for (JrxTerm<?> jrxTerm : jrxElementGroup.getElements()) {
            if (jrxTerm instanceof JrxDeclaration) {
                JrxDeclaration<?> jrxElementPointer = (JrxDeclaration<?>) jrxTerm;
                if (jrxElementPointer.getName().equals(jrxDeclaration.getName())) {
                    if (jrxElementPointer.equals(jrxDeclaration)) {
                        return index;
                    }
                    index++;
                }
            } else if (jrxTerm instanceof JrxChoiceGroup) {
                JrxChoiceGroup jrxSubChoiceGroup = (JrxChoiceGroup) jrxTerm;
                if (jrxSubChoiceGroup.getSelection() != null) {
                    if (jrxSubChoiceGroup.getSelection().equals(jrxDeclaration)) {
                        return index;
                    }
                    index++;
                }
            } else if (jrxTerm instanceof JrxElementGroup) {
                Object[] subResult = getElementWithSameName((JrxElementGroup) jrxTerm, jrxDeclaration, index);
                if (subResult != null) {
                    index = (int) subResult[0];
                    if (subResult[1] != null) {
                        return index;
                    }
                }
            }
        }

        return -1;
    }

    public static Object[] getElementWithSameName(JrxElementGroup jrxElementGroup, JrxDeclaration<?> jrxDeclaration,
            int startIndex) {
        int index = startIndex;

        for (JrxTerm<?> jrxTerm : jrxElementGroup.getElements()) {
            if (jrxTerm instanceof JrxDeclaration) {
                JrxDeclaration<?> jrxElementPointer = (JrxDeclaration<?>) jrxTerm;
                if (jrxElementPointer.getName().equals(jrxDeclaration.getName())) {
                    if (jrxElementPointer.equals(jrxDeclaration)) {
                        return new Object[] { index, jrxElementPointer };
                    }
                    index++;
                }
            } else if (jrxTerm instanceof JrxChoiceGroup) {
                JrxChoiceGroup jrxSubChoiceGroup = (JrxChoiceGroup) jrxTerm;
                if (jrxSubChoiceGroup.getSelection() != null) {
                    if (jrxSubChoiceGroup.getSelection().equals(jrxDeclaration)) {
                        return new Object[] { index, jrxSubChoiceGroup.getSelection() };
                    }
                    index++;
                }
            } else if (jrxTerm instanceof JrxElementGroup) {
                Object[] subResult = getElementWithSameName((JrxElementGroup) jrxTerm, jrxDeclaration, index);
                if (subResult != null) {
                    if (subResult[1] != null) {
                        return subResult;
                    }

                    index = (int) subResult[0];
                }
            }
        }

        return new Object[] { index, null };
    }

    public static JrxElement[] getDeclarationsWithSameSchemaDefinition(JrxElementGroup jrxScopingElementGroup,
            XSDeclaration xsDeclaration) {
        if (xsDeclaration == null)
            return new JrxElement[] {}; // return only empty array, never NULL

        List<JrxElement> jrxElementList = new ArrayList<>();

        for (JrxTerm<?> jrxSubTerm : jrxScopingElementGroup.getElements()) {
            if (jrxSubTerm instanceof JrxChoiceGroup) {
                JrxChoiceGroup jrxSubChoiceGroup = (JrxChoiceGroup) jrxSubTerm;
                if (jrxSubChoiceGroup.getSelection() == null) {
                    continue;
                }

                if (jrxSubChoiceGroup.getSelection().getXsdDeclaration() != null
                        && jrxSubChoiceGroup.getSelection().getXsdDeclaration().equals(xsDeclaration)) {
                    jrxElementList.add((JrxElement) jrxSubChoiceGroup.getSelection());
                }
            } else if (jrxSubTerm instanceof JrxElementGroup) {
                // Nested element group
                JrxElement[] jrxFoundNextedElements = getDeclarationsWithSameSchemaDefinition(
                        (JrxElementGroup) jrxSubTerm, xsDeclaration);
                if (jrxFoundNextedElements != null && jrxFoundNextedElements.length > 0) {
                    return jrxFoundNextedElements;
                }
            } else if (jrxSubTerm instanceof JrxElement) {
                if (jrxSubTerm.getXsdDeclaration() != null && jrxSubTerm.getXsdDeclaration().equals(xsDeclaration)) {
                    jrxElementList.add((JrxElement) jrxSubTerm);
                }
            }
        }

        return jrxElementList.toArray(new JrxElement[] {});
    }

    public JrxElement addChoiceElement(JrxElement jrxElement, int choiceIndex) {
        JrxChoiceGroup jrxChoiceGroup = jrxElement.getScopedSingleChoiceGroup();

        if (jrxChoiceGroup == null) {
            throw new InvalidChoiceElementException(jrxElement);
        }

        XSParticle xsChoiceParticle = xsd2XmlUtil.getChoiceElementParticle(jrxChoiceGroup.getXsdDeclaration(),
                choiceIndex);
        XSElementDecl xsChoiceElement = (XSElementDecl) xsChoiceParticle.getTerm();

        if (xsChoiceElement == null)
            return jrxElement;

        JrxElement jrxElementPointer = jrxElement;

        JrxElement jrxChoiceElement = convertXsElementToJrxElement(xsChoiceElement, jrxElementPointer);
        jrxChoiceElement.setMinOccurs(xsChoiceParticle.getMinOccurs());
        jrxChoiceElement.setMaxOccurs(xsChoiceParticle.getMaxOccurs());

        // In this case the choice element is the only sub-element in the
        // sequence. For the case with choice group with
        // sibling of element or other choice group, please use method
        // addChoiceElementGroup(JrxElementGroup, int).
        while (jrxChoiceGroup.getSelection() != null) {
            if (jrxChoiceGroup.getSelection() instanceof JrxElement) {
                JrxElement jrxExistingChoiceElement = (JrxElement) jrxChoiceGroup.getSelection();
                Element xmlExistingChoiceElement = jrxExistingChoiceElement.getXmlElement();
                Element xmlExistingChoiceElementParentNode = (Element) xmlExistingChoiceElement.getParentNode();
                xmlExistingChoiceElementParentNode.removeChild(xmlExistingChoiceElement);
                jrxChoiceGroup.setSelection(null);
            } else {
                // Not yet supported!!!
                // TODO
            }
        }
        jrxChoiceGroup.setSelection(jrxChoiceElement);
        jrxChoiceElement.setOrigin(jrxChoiceGroup);
        jrxChoiceElement.setParentBlock(jrxChoiceGroup);
        if (jrxElementPointer.equals(jrxElement)) {
            // It already reaches the max-occur, remove the choice group
            // jrxElement.getChildrenBlock().getElements().remove(jrxChoiceGroup);
            // jrxChoiceGroup.setParentBlock(null);
        }

        return jrxElementPointer;
    }

    public JrxElement addChoiceElement(JrxChoiceGroup jrxChoiceGroup, int choiceIndex) {
        XSParticle xsChoiceParticle = xsd2XmlUtil.getChoiceElementParticle(jrxChoiceGroup.getXsdDeclaration(),
                choiceIndex);
        XSElementDecl xsChoiceElement = (XSElementDecl) xsChoiceParticle.getTerm();

        if (xsChoiceElement == null)
            return null;

        JrxElement jrxParentElement = null;
        JrxDeclaration<?> jrxParentDeclaration = jrxChoiceGroup.getParentBlock().getOwner();

        if (jrxParentDeclaration instanceof JrxGroup) {
            jrxParentElement = ((JrxGroup) jrxParentDeclaration).getParentElement();
        } else {
            jrxParentElement = (JrxElement) jrxParentDeclaration;
        }
        JrxElement jrxNextSiblingElement = getNextSiblingElement(jrxChoiceGroup);

        JrxElement jrxChoiceElement = convertXsElementToJrxElement(xsChoiceElement, jrxParentDeclaration);
        jrxChoiceElement.setMinOccurs(xsChoiceParticle.getMinOccurs());
        jrxChoiceElement.setMaxOccurs(xsChoiceParticle.getMaxOccurs());

        Element xmlChoiceElementParentNode = jrxParentElement.getXmlElement();
        while (jrxChoiceGroup.getElements().size() > 0) {
            JrxElement jrxExistingChoiceElement = (JrxElement) jrxChoiceGroup.getElements().get(0);
            Element xmlExistingChoiceElement = jrxExistingChoiceElement.getXmlElement();
            xmlChoiceElementParentNode = (Element) xmlExistingChoiceElement.getParentNode();
            xmlChoiceElementParentNode.removeChild(xmlExistingChoiceElement);
            jrxChoiceGroup.getElements().remove(0);
        }

        jrxChoiceGroup.setSelection(jrxChoiceElement);
        jrxChoiceElement.setOrigin(jrxChoiceGroup);
        jrxChoiceElement.setParentBlock(jrxChoiceGroup);
        Element xmlChoiceElement = jrxChoiceElement.getXmlElement();
        if (jrxNextSiblingElement == null) { // in case it is the last element, we just can append it
            xmlChoiceElementParentNode.appendChild(xmlChoiceElement);
        } else {
            xmlChoiceElementParentNode.insertBefore(xmlChoiceElement, jrxNextSiblingElement.getXmlElement());
        }

        return jrxChoiceElement;
    }

    private JrxElementGroup getElementContainerGroup(JrxTerm<?> jrxTerm) {
        if (jrxTerm instanceof JrxElement) {
            JrxElement jrxElement = (JrxElement) jrxTerm;
            JrxElementGroup jrxParentContainer = jrxElement.getParentBlock();

            if (jrxParentContainer.getOwner() != null) {
                return jrxParentContainer;
            }

            return getElementContainerGroup(jrxParentContainer);
        } else {
            JrxElementGroup jrxElementGroup = (JrxElementGroup) jrxTerm;
            if (jrxElementGroup.getOwner() != null) {
                // The term itself is the parent container!
                return jrxElementGroup;
            }

            JrxElementGroup jrxParentContainer = jrxElementGroup.getParentBlock();
            if (jrxParentContainer.getOwner() != null) {
                return jrxParentContainer;
            }

            return getElementContainerGroup(jrxParentContainer);
        }
    }

    // @formatter:off
	// private JrxElement getPreviousSiblingElement(JrxTerm<?> jrxTerm) {
	// // Identifying parent
	// JrxElementGroup jrxParentChildrenBlock = getElementContainerGroup(jrxTerm);
	// if (jrxParentChildrenBlock == null)
	// return null;
	//
	// // Scanning next element from parent
	// JrxElement jrxPreviousSiblingElement = null;
	// int i = jrxParentChildrenBlock.getElements().indexOf(jrxTerm) - 1;
	// while (jrxPreviousSiblingElement == null && i >= 0) {
	// JrxTerm<?> jrxPreviousSiblingTerm = jrxParentChildrenBlock.getElements().get(i);
	// if (jrxPreviousSiblingTerm instanceof JrxElement) {
	// jrxPreviousSiblingElement = (JrxElement) jrxPreviousSiblingTerm;
	// break;
	// }
	// i--;
	// }
	//
	// return jrxPreviousSiblingElement;
	// }
	// @formatter:on

    private JrxElement getNextSiblingElement(JrxTerm<?> jrxTerm) {
        // Identifying parent
        JrxElementGroup jrxParentChildrenBlock = getElementContainerGroup(jrxTerm);
        if (jrxParentChildrenBlock == null)
            return null;

        // Scanning next element from parent
        JrxElement jrxNextSiblingElement = null;
        int i = jrxParentChildrenBlock.getElements().indexOf(jrxTerm) + 1;
        while (jrxNextSiblingElement == null && i < jrxParentChildrenBlock.getElements().size()) {
            JrxTerm<?> jrxNextSiblingTerm = jrxParentChildrenBlock.getElements().get(i);
            if (jrxNextSiblingTerm instanceof JrxElement) {
                jrxNextSiblingElement = (JrxElement) jrxNextSiblingTerm;
                break;
            }
            i++;
        }

        return jrxNextSiblingElement;
    }

    public JrxElement getElementByXPath(JrxDocument jrxDocument, String[][] namespaces, String xPathExpression)
            throws Exception {
        Document xmlDoc = jrxDocument.getXmlDocument();
        if (xmlDoc == null) {
            return null;
        }
        Element xmlElement = null;
        if (namespaces == null) {
            xmlElement = (Element) XmlUtils.getNodeByXPath(xmlDoc, xPathExpression);
        } else {
            xmlElement = (Element) XmlUtils.getNodeByXPath(xmlDoc, namespaces, xPathExpression);
        }
        if (xmlElement == null) {
            return null;
        }
        return jrxDocument.get(xmlElement);
    }

    public List<JrxElement> getElementListByXPath(JrxDocument jrxDocument, String[][] namespaces,
            String xPathExpression) throws Exception {
        Document xmlDoc = jrxDocument.getXmlDocument();
        if (xmlDoc == null) {
            return null;
        }
        List<JrxElement> jrxElementList = new ArrayList<JrxElement>();

        NodeList xmlNodeList = null;
        if (namespaces == null) {
            xmlNodeList = XmlUtils.getNodeListByXPath(xmlDoc, xPathExpression);
        } else {
            xmlNodeList = XmlUtils.getNodeListByXPath(xmlDoc, namespaces, xPathExpression);
        }
        for (int i = 0; i < xmlNodeList.getLength(); i++) {
            Element xmlElement = (Element) xmlNodeList.item(i);
            if (xmlElement != null) {
                jrxElementList.add(jrxDocument.get(xmlElement));
            }
        }

        return jrxElementList;
    }

    public JrxElement getElementByXPath(JrxElement jrxParentElement, String[][] namespaces, String xPathExpression)
            throws Exception {
        Element xmlParentElement = jrxParentElement.getXmlElement();
        if (xmlParentElement == null) {
            return null;
        }
        Element xmlElement = null;
        if (namespaces == null) {
            xmlElement = (Element) XmlUtils.getNodeByXPath(xmlParentElement, xPathExpression);
        } else {
            xmlElement = (Element) XmlUtils.getNodeByXPath(xmlParentElement, namespaces, xPathExpression);
        }
        if (xmlElement == null) {
            return null;
        }
        return jrxParentElement.getOwnerDocument().get(xmlElement);
    }

    public List<JrxElement> getElementListByXPath(JrxElement jrxParentElement, String[][] namespaces,
            String xPathExpression) throws Exception {
        Element xmlParentElement = jrxParentElement.getXmlElement();
        if (xmlParentElement == null) {
            return null;
        }

        List<JrxElement> jrxElementList = new ArrayList<JrxElement>();

        NodeList xmlNodeList = null;
        if (namespaces == null) {
            xmlNodeList = XmlUtils.getNodeListByXPath(xmlParentElement, xPathExpression);
        } else {
            xmlNodeList = XmlUtils.getNodeListByXPath(xmlParentElement, namespaces, xPathExpression);
        }
        for (int i = 0; i < xmlNodeList.getLength(); i++) {
            Element xmlElement = (Element) xmlNodeList.item(i);
            if (xmlElement != null) {
                jrxElementList.add(jrxParentElement.getOwnerDocument().get(xmlElement));
            }
        }

        return jrxElementList;
    }

    public void addChoiceHelperElement(JrxDocument jrxDocument) {
        addChoiceHelperElement(jrxDocument.getRootElement());
    }

    public void addChoiceHelperElement(JrxElement jrxElement) {
        if (jrxElement == null)
            return;

        if (jrxElement.getScopedSingleChoiceGroup() == null || jrxElement.getParentBlock() == null) {
            // This element itself is not a choice element or a root element (root element cannot have choice or at
            // least not supported).

            // Add choice helper of child choice element
            addChoiceHelperElement(jrxElement.getChildrenBlock());
            return;
        }

        // This element itself is a choice element, try to add helper to the parent block

        // Check if this JrxElement is the last member when it is repetitive
        if (jrxElement.getMaxOccurs() == 1) {
            // Skip this element because it can only exist once. Skip!
            return;
        }

        JrxElementGroup jrxParentBlock = jrxElement.getParentBlock();

        if (jrxParentBlock == null)
            return;

        JrxTerm<?>[] jrxElementsFromSameSchemaDef = getTermsWithSameSchemaDefinition(jrxParentBlock,
                jrxElement.getXsdDeclaration());
        if (jrxElement.getMaxOccurs() != -1 && jrxElementsFromSameSchemaDef.length >= jrxElement.getMaxOccurs()) {
            // Not unlimited and already reached maximum occurrences. Skip!
            return;
        }

        JrxElement lastJrxElementFromSameSchemaDef = (JrxElement) jrxElementsFromSameSchemaDef[jrxElementsFromSameSchemaDef.length - 1];
        if (JrxChoiceGroupUtil.isChoiceHelperElement(lastJrxElementFromSameSchemaDef)) {
            // A choice helper exists as last JrxElement in the array. Skip!
            return;
        }

        // A new choice helper is still possible to be added.

        // Create dummy/helper node
        JrxDeclaration<?> jrxParentDeclaration = getParentDeclaration(jrxElement);
        if (jrxParentDeclaration == null) {
            // Cannot create helper
            log.warn("Cannot create choice helper for JrxElement: " + jrxElement.getName());
            return;
        }

        JrxElement jrxDummyElement = convertXsElementToJrxElement(jrxElement.getXsdDeclaration(), jrxParentDeclaration);

        jrxDummyElement.setMinOccurs(jrxElement.getMinOccurs());
        jrxDummyElement.setMaxOccurs(jrxElement.getMaxOccurs());

        Element xmlParentElement = (Element) jrxElement.getXmlElement().getParentNode();
        Element xmlDummyElement = jrxDummyElement.getXmlElement();
        xmlParentElement.insertBefore(xmlDummyElement, jrxElement.getXmlElement().getNextSibling());

        // to ensure the sequence is correct, 1st add to list just
        // after the last element with same type and then setParent to
        // avoid improper index shifting.
        jrxParentBlock.getElements().add(
                jrxParentDeclaration.getChildrenBlock().getElements().indexOf(lastJrxElementFromSameSchemaDef) + 1,
                jrxDummyElement);
        jrxDummyElement.setParentBlock(jrxParentBlock);
        jrxDummyElement.setOwnerDocument(jrxElement.getOwnerDocument());
    }

    public void addChoiceHelperElement(JrxElementGroup jrxParentBlock) {
        if (jrxParentBlock == null) {
            return;
        }

        XSModelGroup xsModelGroup = jrxParentBlock.getXsdDeclaration();
        if (xsModelGroup == null) {
            return;
        }

        Set<XSTerm> xsProcessedTerm = new HashSet<XSTerm>(); // To avoid re-processed of the same term
        for (int i = 0; i < jrxParentBlock.getElements().size(); i++) {
            JrxTerm<?> jrxTerm = jrxParentBlock.getElements().get(i);
            if (xsProcessedTerm.contains(jrxTerm.getXsdDeclaration())) {
                continue; // skip
            }
            if (jrxTerm instanceof JrxElement) {
                addChoiceHelperElement((JrxElement) jrxTerm);
                xsProcessedTerm.add(jrxTerm.getXsdDeclaration());
            } else if (jrxTerm instanceof JrxElementGroup) {
                JrxElementGroup jrxElementGroup = (JrxElementGroup) jrxTerm;
                if (jrxElementGroup instanceof JrxChoiceGroup) {
                    // Direct choice group
                    addChoiceHelperDirectChoiceGroup((JrxChoiceGroup) jrxElementGroup);
                    xsProcessedTerm.add(jrxTerm.getXsdDeclaration());
                } else {
                    addChoiceHelperElement(jrxElementGroup);
                }
            } else if (jrxTerm instanceof JrxGroup) {
                JrxGroup jrxGroup = (JrxGroup) jrxTerm;
                addChoiceHelperElement(jrxGroup.getChildrenBlock());
            } else {
                // Not yet supported
            }
        }
    }

    public void addChoiceHelperDirectChoiceGroup(JrxChoiceGroup jrxDirectChoiceGroup) {
        // Check if this jrxDirectChoiceGroup is the last member when it is repetitive
        if (jrxDirectChoiceGroup.getMaxOccurs() < 2) {
            // Skip this element because it can only exist once. Skip!
            return;
        }

        JrxElementGroup jrxParentBlock = jrxDirectChoiceGroup.getParentBlock();

        if (jrxParentBlock == null)
            return; // A direct choice group must have parent block.

        JrxTerm<?>[] jrxDirectChoiceGroupsFromSameSchemaDef = getTermsWithSameSchemaDefinition(jrxParentBlock,
                jrxDirectChoiceGroup.getXsdDeclaration());
        if (jrxDirectChoiceGroup.getMaxOccurs() != -1
                && jrxDirectChoiceGroupsFromSameSchemaDef.length >= jrxDirectChoiceGroup.getMaxOccurs()) {
            // Not unlimited and already reached maximum occurrences. Skip!
            return;
        }

        JrxChoiceGroup lastJrxChoiceGroupFromSameSchemaDef = (JrxChoiceGroup) jrxDirectChoiceGroupsFromSameSchemaDef[jrxDirectChoiceGroupsFromSameSchemaDef.length - 1];
        if (JrxChoiceGroupUtil.isChoiceHelperElement(lastJrxChoiceGroupFromSameSchemaDef)) {
            // A choice helper exists as last JrxElement in the array. Skip!
            return;
        }

        // A new choice helper is still possible to be added.

        // Create dummy/helper node
        JrxDeclaration<?> jrxParentDeclaration = getParentDeclaration(jrxDirectChoiceGroup);
        if (jrxParentDeclaration == null) {
            // Cannot create helper
            log.warn("Cannot create choice helper for JrxElement: " + jrxDirectChoiceGroup.getName());
            return;
        }

        int index = jrxParentBlock.getElements().indexOf(lastJrxChoiceGroupFromSameSchemaDef);
        createJrxChoiceGroup(jrxDirectChoiceGroup.getXsdDeclaration(), jrxParentDeclaration, index + 1,
                jrxDirectChoiceGroup.getMinOccurs(), jrxDirectChoiceGroup.getMaxOccurs());
    }

    public void removeChoiceElement(JrxElement jrxElement) {
        // JrxElement jrxParentElement = jrxElement.getParentBlock().getOwner();
        JrxDeclaration<?> jrxParentDeclaration = jrxElement.getParentBlock().getOwner();
        JrxElementGroup jrxParentBlock = null;
        if (jrxParentDeclaration == null) {
            jrxParentDeclaration = JrxXmlModelUtil.getParentElement(jrxElement.getParentBlock());
            jrxParentBlock = jrxElement.getParentBlock();
        } else {
            jrxParentBlock = jrxParentDeclaration.getChildrenBlock();
        }
        // Remove element mapping table in the document
        jrxElement.getOwnerDocument().remove(jrxElement.getXmlElement());
        
        jrxElement.setOwnerDocument(null);

        // Remove xml element
        Element xmlElement = jrxElement.getXmlElement();
        Element xmlParentElement = (Element) xmlElement.getParentNode();
        xmlParentElement.removeChild(xmlElement);

        // remove jrx element link
        if (jrxParentBlock instanceof JrxChoiceGroup) {
            ((JrxChoiceGroup) jrxParentBlock).clearSelection();
        } else {
            jrxParentBlock.getElements().remove(jrxElement);
        }
        jrxElement.setParentBlock(null);
    }

    public boolean removeChoiceElementGroup(JrxElementGroup jrxChoiceGroup) {
        JrxElementGroup jrxParentElementGroup = jrxChoiceGroup.getParentBlock();

        if (!jrxChoiceGroup.isChoiceGroup()) {
            return false;
        }

        // remove jrx element link
        jrxParentElementGroup.getElements().remove(jrxChoiceGroup);
        jrxChoiceGroup.setParentBlock(null);

        return true;
    }

    public JrxDocument removeChoiceHelperElement(JrxDocument jrxDocument) {
        removeChoiceHelperElement(jrxDocument.getRootElement());
        return jrxDocument;
    }

    public boolean removeChoiceHelperElement(JrxChoiceGroup jrxChoiceGroup) {
        if (jrxChoiceGroup == null) {
            return false;
        }

        boolean removed = false;
        if (JrxChoiceGroupUtil.isChoiceHelperElement(jrxChoiceGroup)) {
            removeChoiceElementGroup(jrxChoiceGroup);
            removed = true;
        }

        return removed;
    }

    public boolean removeChoiceHelperElement(JrxElement jrxElement) {
        if (jrxElement == null) {
            return false;
        }

        XSElementDecl xsElement = jrxElement.getXsdDeclaration();
        if (xsElement == null) {
            return false;
        }

        XSType xsType = xsElement.getType();
        if (xsType.isSimpleType()) {
            return false;
        }

        if (!xsType.isComplexType()) {
            log.warn("Unhandled type: " + xsType.getClass().getSimpleName() + "for: " + xsElement.getTargetNamespace()
                    + ":" + xsElement.getName());
            return false;
        }

        boolean removed = false;
        if (JrxChoiceGroupUtil.isChoiceHelperElement(jrxElement)) {
            // Remove empty node in XML Document
            Element xmlElement = jrxElement.getXmlElement();
            Element xmlParentElement = (Element) xmlElement.getParentNode();
            if (xmlParentElement != null) {
                xmlParentElement.removeChild(xmlElement);
            }

            // Remove JRX Element
            if (jrxElement.getParentBlock() != null) {
                jrxElement.getParentBlock().getElements().remove(jrxElement);
                jrxElement.setParentBlock(null);
                removed = true;
            }

        }

        if (jrxElement.getChildrenBlock() != null) {
            int i = 0;
            while (i < jrxElement.getChildrenBlock().getElements().size()) {
                JrxTerm<?> jrxChildTerm = jrxElement.getChildrenBlock().getElements().get(i);
                boolean next = true;
                if (jrxChildTerm instanceof JrxElement) {
                    next = !removeChoiceHelperElement((JrxElement) jrxChildTerm);
                } else if (jrxChildTerm instanceof JrxChoiceGroup) {
                    next = !removeChoiceHelperElement((JrxChoiceGroup) jrxChildTerm);
                }
                if (next) {
                    i++;
                }
            }
        }

        return removed;
    }

    public JrxDocument convertXsdToJrxDoc(URL schemaUrl, String namespacePrefix, String rootElement) throws Exception {
        // Init XSOM Parser
        XSOMParser xsomParser = new XSOMParser(SAXParserFactory.newInstance());
        xsomParser.setAnnotationParser(new DomAnnotationParserFactory());
        xsomParser.parse(schemaUrl);

        XSSchemaSet xsSchemaSet = xsomParser.getResult();
        if (xsSchemaSet == null) {
            throw new SchemaNotAvailableException();
        }

        // Load all namespaces in XSD
        Iterator<XSSchema> it = xsSchemaSet.iterateSchema();
        XSSchema xsDefaultSchema = null;
        while (it.hasNext()) {
            XSSchema xsSchema = (XSSchema) it.next();
            String nameSpace = xsSchema.getTargetNamespace();
            if (nameSpace.equals(XMLConstants.W3C_XML_SCHEMA_NS_URI))
                continue;
            xsDefaultSchema = xsSchema;
            break;
        }

        return convertXsdToJrxDoc(xsSchemaSet, xsDefaultSchema, namespacePrefix, rootElement);
    }

    private JrxDocument convertXsdToJrxDoc(XSSchemaSet xsSchemaSet, XSSchema xsSchema, String namespacePrefix,
            String rootElement) throws Exception {
        Map<String, XSElementDecl> xsElementDeclarations = xsSchema.getElementDecls();

        if (xsElementDeclarations.values().size() < 1) {
            return null;
        }

        XSElementDecl xsRootElement = null;
        if (rootElement == null || rootElement.equals("")) {
            xsRootElement = xsElementDeclarations.values().iterator().next();
        } else {
            xsRootElement = xsElementDeclarations.get(rootElement);
            if (xsRootElement == null) {
                for (XSSchema xsCurrentSchema : xsSchemaSet.getSchemas()) {
                    xsElementDeclarations = xsCurrentSchema.getElementDecls();
                    xsRootElement = xsElementDeclarations.get(rootElement);
                    if (xsRootElement != null) {
                        // found!
                        break;
                    }
                }
            }
        }

        if (xsRootElement == null) {
            return null;
        }

        String namespaceURI = xsRootElement.getTargetNamespace();
        Document xmlDoc = XmlUtils.newDocumentNS(xsRootElement.getName(), namespacePrefix, namespaceURI);

        registerNamespacePrefixes(xmlDoc);

        addSchema(xsSchema);
        JrxDocument jrxDoc = convertXmlToJrxModel(xmlDoc);

        JrxElement jrxRootElement = jrxDoc.getRootElement();
        enrichJrxElement(jrxRootElement);
        enrichJrxElementRecursive(jrxRootElement);

        return jrxDoc;
    }

    private void registerNamespacePrefixes(Document xmlDoc) {
        Element xmlRootElement = xmlDoc.getDocumentElement();

        for (Entry<String, String> namespace : namespaceMap.entrySet()) {
            String namespaceAttribute = XMLConstants.XMLNS_ATTRIBUTE;
            String namespacePrefix = namespace.getKey();
            String namespaceURI = namespace.getValue();
            if (namespacePrefix != null && !namespacePrefix.equals("")) {
                namespaceAttribute += ":" + namespacePrefix;
            }

            xmlRootElement.setAttributeNS(XMLConstants.XMLNS_ATTRIBUTE_NS_URI, namespaceAttribute, namespaceURI);
        }
    }

    public String[][] getNamespacePrefixes() {
        String[][] namespacePrefixes = new String[namespaceMap.size()][2];

        int i = 0;
        for (Iterator<Entry<String, String>> it = namespaceMap.entrySet().iterator(); it.hasNext();) {
            Entry<String, String> entry = it.next();
            namespacePrefixes[i][0] = entry.getKey();
            namespacePrefixes[i][1] = entry.getValue();
            i++;
        }

        return namespacePrefixes;
    }

    public String getNamespaceUriFromNoPrefixNode(String nodeName) {
        return noNamespaceNodeMap.get(nodeName);
    }

    public JrxGroup addRepetitiveGroup(JrxGroup jrxOriginalGroup) {
        JrxDeclaration<?> jrxParentDeclaration = jrxOriginalGroup.getParentBlock().getOwner();
        // JUMIN: un-finished business with schema definition xs:group
        // JrxElement jrxParentElement = jrxOriginalGroup.getParentElement();
        boolean addElement = false;
        if (jrxOriginalGroup.getMaxOccurs() == -1)
            addElement = true;

        if (!addElement) {
            if (jrxOriginalGroup.getMaxOccurs() > 0
                    && jrxOriginalGroup.getMaxOccurs() > countTermWithSameSchemaDefinition(
                            jrxOriginalGroup.getParentBlock(), jrxOriginalGroup.getXsdDeclaration())) {
                addElement = true;
            } else {
                log.debug("Cannot add further element: " + jrxOriginalGroup.getName()
                        + " because max occurs has been reached!");
            }
        }

        if (addElement) {
            // If the multiplicity is OK, create a copy
            JrxGroup jrxNewGroup = convertXsGroupToJrxGroup(jrxOriginalGroup.getXsdDeclaration(), jrxParentDeclaration);

            return jrxNewGroup;
        }

        return null;
    }

    public JrxElement addSimpleRepetitiveElement(JrxElement jrxOriginalElement) {
        JrxDeclaration<?> jrxParentDeclaration = getParentDeclaration(jrxOriginalElement);
        JrxElement jrxParentElement = null;
        if (jrxParentDeclaration instanceof JrxElement) {
            jrxParentElement = (JrxElement) jrxParentDeclaration;
        } else {
            jrxParentElement = jrxParentDeclaration.getParentElement();
        }
        boolean addElement = false;
        if (jrxOriginalElement.getMaxOccurs() == -1)
            addElement = true;

        if (!addElement) {
            if (jrxOriginalElement.getMaxOccurs() > 0
                    && jrxOriginalElement.getMaxOccurs() > countTermWithSameSchemaDefinition(
                            jrxOriginalElement.getParentBlock(), jrxOriginalElement.getXsdDeclaration())) {
                addElement = true;
            } else {
                log.debug("Cannot add further element: " + jrxOriginalElement.getName()
                        + " because max occurs has been reached!");
            }
        }

        if (addElement) {
            // If the multiplicity is OK, create a copy
            JrxElement jrxNewElement = convertXsElementToJrxElement(jrxOriginalElement.getXsdDeclaration(),
                    jrxParentDeclaration);

            Element xmlParentElement = jrxParentElement.getXmlElement();
            Element xmlNewElement = jrxNewElement.getXmlElement();
            Node xmlNextSiblingNode = jrxOriginalElement.getXmlElement().getNextSibling();
            if (xmlNextSiblingNode == null) {
                xmlParentElement.appendChild(xmlNewElement);
            } else {
                xmlParentElement.insertBefore(xmlNewElement, xmlNextSiblingNode);
            }

            jrxNewElement.setMinOccurs(jrxOriginalElement.getMinOccurs());
            jrxNewElement.setMaxOccurs(jrxOriginalElement.getMaxOccurs());

            int originalElementIndex = jrxParentDeclaration.getChildrenBlock().getElements()
                    .indexOf(jrxOriginalElement);

            if (originalElementIndex < 0) {
                // Use the parent block of the original element
                originalElementIndex = jrxOriginalElement.getParentBlock().getElements().indexOf(jrxOriginalElement);
                jrxOriginalElement.getParentBlock().getElements().add(originalElementIndex + 1, jrxNewElement);
                jrxNewElement.setParentBlock(jrxOriginalElement.getParentBlock());
            } else {
                // to ensure the sequence is correct, 1st add to list just before
                // the element and then setParent to avoid
                // proper-index shifting.
                jrxParentDeclaration.getChildrenBlock().getElements().add(originalElementIndex + 1, jrxNewElement);
                jrxNewElement.setParentBlock(jrxParentDeclaration.getChildrenBlock());
            }
            jrxNewElement.setOwnerDocument(jrxParentElement.getOwnerDocument());

            return jrxNewElement;
        }

        return null;
    }

    public void removeSimpleRepetitiveElement(JrxElement jrxElement) {
        removeSimpleRepetitiveElement(jrxElement, false);
    }

    public void removeSimpleRepetitiveElement(JrxElement jrxElement, boolean forceDelete) {
        JrxDeclaration<?> jrxParentDeclaration = jrxElement.getParentBlock().getOwner();

        if (jrxElement.isRepetitive()) {
            boolean delete = true;
            if (countTermWithSameSchemaDefinition(jrxElement.getParentBlock(), jrxElement.getXsdDeclaration()) == 1
                    && !forceDelete) {
                delete = false;
            }

            if (!delete)
                return;

            jrxElement.setOwnerDocument(null);

            Element xmlElement = jrxElement.getXmlElement();
            Element xmlParentElement = (Element) xmlElement.getParentNode();
            xmlParentElement.removeChild(xmlElement);
            if (jrxParentDeclaration.getChildrenBlock() instanceof JrxChoiceGroup) {
                ((JrxChoiceGroup) jrxParentDeclaration.getChildrenBlock()).clearSelection();
            } else {
                jrxParentDeclaration.getChildrenBlock().getElements().remove(jrxElement);
            }
            jrxElement.setParentBlock(null);
        } else {
            throw new IllegalElementOperationException("Remove", jrxElement, "the element is not repetitive");
        }
    }

    public static final String getBaseType(JrxElement jrxElement) {
        if (jrxElement.getXsdDeclaration() == null) {
            return null;
        }

        XSType xsType = jrxElement.getXsdDeclaration().getType();

        if (xsType != null) {
            return getBaseTypeName(xsType);
        }

        return null;
    }

    public static final String getBaseTypeName(XSType xsType) {
        XSSimpleType xsSimpleType = null;
        if (xsType instanceof XSSimpleType) {
            xsSimpleType = xsType.asSimpleType();
        } else {
            xsSimpleType = xsType.getBaseType().asSimpleType();
        }
        return xsSimpleType.getPrimitiveType().getName();
    }

    public static int getElementIndexInFamily(JrxTerm<?> jrxTerm) {
        JrxElementGroup jrxParent = jrxTerm.getParentBlock();

        if (jrxParent == null) {
            return -1;
        }

        return jrxParent.getElements().indexOf(jrxTerm);
    }

    public static JrxChoiceGroup assignNameToChoiceGroup(JrxChoiceGroup jrxChoiceGroup) {
        if (jrxChoiceGroup == null || jrxChoiceGroup.getXsdDeclaration() == null) {
            return jrxChoiceGroup;
        }

        StringBuffer sb = new StringBuffer();
        sb.append("[");
		sb.append(getLabel(jrxChoiceGroup.getXsdDeclaration()));
        sb.append("]");
        jrxChoiceGroup.setName(sb.toString());

        return jrxChoiceGroup;
	}
	
    private static String getLabel(XSModelGroupDecl xsModelGroupDecl) {
        if (xsModelGroupDecl == null)
            return "";

        return getLabel(xsModelGroupDecl.getModelGroup());
    }

    private static String getLabel(XSModelGroup xsModelGroup) {
        if (xsModelGroup == null)
            return "";

        StringBuffer sb = new StringBuffer();

        String separator = ",";
        if (XSModelGroup.Compositor.CHOICE.equals(xsModelGroup.getCompositor())) {
            separator = "|";
        }

        XSParticle[] xsChildParticleArray = xsModelGroup.getChildren();
        for (int i = 0; i < xsChildParticleArray.length; i++) {
            if (i > 0) {
                sb.append(separator);
            }
            XSParticle xsChildParticle = xsChildParticleArray[i];
            XSTerm xsChildTerm = xsChildParticle.getTerm();
            if (xsChildTerm.isElementDecl()) {
                XSElementDecl xsChildElement = xsChildTerm.asElementDecl();
                sb.append(xsChildElement.getName());
            } else if (xsChildTerm.isModelGroupDecl()) {
                sb.append("(");
                sb.append(getLabel(xsChildTerm.asModelGroupDecl()));
                sb.append(")");
            } else if (xsChildTerm.isModelGroup()) {
                sb.append("(");
                sb.append(getLabel(xsChildTerm.asModelGroup()));
                sb.append(")");
            }
        }

        return sb.toString();
    }

    public static String getMultiplicitySymbol(JrxTerm<?> jrxTerm) {
        return getMultiplicitySymbol(jrxTerm.getMinOccurs(), jrxTerm.getMaxOccurs());
    }

    public static String getMultiplicitySymbol(int minOccurs, int maxOccurs) {
        if (maxOccurs > 1 || maxOccurs == -1) {
            if (minOccurs == 1) {
                return "+";
            } else {
                return "*";
            }
        }

        if (minOccurs == 0) {
            return "?";
        }

        return "";
    }

    public List<XmlValidationError> validateDocument(JrxDocument jrxDocument) throws Exception {
        jrxDocument = enrichJrxDocument(jrxDocument);
        if (jrxDocument == null) {
            return new ArrayList<XmlValidationError>();
        }

        JrxElement jrxRootElement = jrxDocument.getRootElement();
        return validateElement(jrxRootElement, "/");
    }

    public List<XmlValidationError> validateElement(JrxElement jrxElement, String path) {
        List<XmlValidationError> validationResult = new ArrayList<XmlValidationError>();

        if (jrxElement == null) {
            return null;
        }

        String currentPath = path + jrxElement.getName() + "/";

        for (JrxAttribute jrxAttribute : jrxElement.getAttributeList()) {
            validationResult.addAll(validateAttribute(jrxAttribute));
        }

        if (jrxElement.getChildrenBlock() == null) {
            return validationResult;
        }

        XSModelGroup xsModelGroup = jrxElement.getChildrenBlock().getXsdDeclaration();

        if (xsModelGroup == null) {
            if (isElementMandatory(jrxElement))
                validationResult.add(new XmlValidationError(jrxElement,
                        XmlValidationMessage.ERROR_NODE_MANDATORY, jrxElement.getName()));
            return validationResult;
        }

        updateElementGroup(jrxElement.getChildrenBlock(), xsModelGroup);

        if (jrxElement.getChildrenBlock() instanceof JrxChoiceGroup) {
            JrxChoiceGroup jrxChoiceGroup = (JrxChoiceGroup) jrxElement.getChildrenBlock();
            if (jrxChoiceGroup.getSelection() != null) {
                validationResult.addAll(validateTerm(jrxElement, jrxChoiceGroup.getSelection(), currentPath));
            }
        } else {
            for (int i = 0; i < jrxElement.getChildrenBlock().getElements().size(); i++) {
                JrxTerm<?> jrxChildTerm = jrxElement.getChildrenBlock().getElements().get(i);
                validationResult.addAll(validateTerm(jrxElement, jrxChildTerm, currentPath));
            }
        }

        return validationResult;
    }

    protected List<XmlValidationError> validateTerm(JrxElement jrxParentElement, JrxTerm<?> jrxTerm,
            String parentPath) {
        List<XmlValidationError> validationResult = new ArrayList<XmlValidationError>();

        if (jrxTerm == null) {
            return null;
        }

        if (jrxTerm instanceof JrxElement) {
            JrxElement jrxChildElement = (JrxElement) jrxTerm;

            if (jrxChildElement.getXsdDeclaration() == null) {
                validationResult.add(new XmlValidationError(jrxChildElement,
                        XmlValidationMessage.ERROR_NODE_INVALID_CHILD, jrxChildElement.getName(), jrxParentElement
                                .getName()));
            } else if (isElementMandatory(jrxChildElement)) {
                if (jrxChildElement.isEmpty()) {
                    validationResult.add(new XmlValidationError(jrxChildElement,
                            XmlValidationMessage.ERROR_NODE_MANDATORY, jrxChildElement.getName()));
                } else {
                    validationResult.addAll(validateElement(jrxChildElement, parentPath));
                    validationResult.addAll(validateElementContents(jrxChildElement.getXsdDeclaration().getType(),
                            jrxChildElement, parentPath));
                }
            } else if (!jrxChildElement.isEmpty()) {
                int occurences = countTermWithSameSchemaDefinition(jrxChildElement.getParentBlock(),
                        jrxChildElement.getXsdDeclaration());

                if (jrxChildElement.getMaxOccurs() != -1 && occurences > jrxChildElement.getMaxOccurs()) {
                    validationResult.add(new XmlValidationError(jrxChildElement,
                            XmlValidationMessage.ERROR_NODE_OCCURANCE,
                            String.valueOf(jrxChildElement.getMaxOccurs()), String.valueOf(jrxChildElement.getName()),
                            String.valueOf(occurences)));
                }
                validationResult.addAll(validateElement(jrxChildElement, parentPath));
                validationResult.addAll(validateElementContents(jrxChildElement.getXsdDeclaration().getType(),
                        jrxChildElement, parentPath));
            }
        } else if (jrxTerm instanceof JrxChoiceGroup) {
            JrxChoiceGroup jrxChildChoiceGroup = (JrxChoiceGroup) jrxTerm;
            if (jrxChildChoiceGroup.getSelection() != null) {
                if (jrxChildChoiceGroup.getSelection() instanceof JrxElement) {
                    JrxElement jrxChildElementFromChoice = (JrxElement) jrxChildChoiceGroup.getSelection();
                    validationResult.addAll(validateElement(jrxChildElementFromChoice, parentPath));
                    validationResult.addAll(validateElementContents(jrxChildElementFromChoice.getXsdDeclaration()
                            .getType(), jrxChildElementFromChoice, parentPath));
                } else if (jrxChildChoiceGroup.getSelection() instanceof JrxChoiceGroup) {
                    JrxChoiceGroup jrxChildChoiceGroupFromChoice = (JrxChoiceGroup) jrxChildChoiceGroup.getSelection();
                    if (jrxChildChoiceGroupFromChoice.getSelection() != null)
                        validationResult.addAll(validateTerm(jrxParentElement,
                                jrxChildChoiceGroupFromChoice.getSelection(), parentPath));
                } else if (jrxChildChoiceGroup.getSelection() instanceof JrxElementGroup) {
                    // This has to be checked after choice group
                    JrxElementGroup jrxChildElementGroupFromChoice = (JrxElementGroup) jrxChildChoiceGroup
                            .getSelection();
                    for (JrxTerm<?> jrxGrandChildTermFromChoice : jrxChildElementGroupFromChoice.getElements()) {
                        validationResult.addAll(validateTerm(jrxParentElement, jrxGrandChildTermFromChoice,
                                parentPath));
                    }
                } else {
                    // Not supported
                }
            } else if (jrxChildChoiceGroup.isMandatory()) {
                validationResult.add(new XmlValidationError(jrxChildChoiceGroup,
                        XmlValidationMessage.ERROR_NODE_CHOICE_NO_SELECTION, jrxChildChoiceGroup.getName(),
                        jrxParentElement.getName()));
            }
        }

        return validationResult;
    }

    protected List<XmlValidationError> validateElementContents(XSType xsType, JrxElement jrxElement,
            String currentPath) {

        List<XmlValidationError> validationResult = new ArrayList<XmlValidationError>();

        if (xsType != null) {
            XSSimpleType xsSimpleType = null;
            if (xsType instanceof XSSimpleType) {
                xsSimpleType = xsType.asSimpleType();
            } else {
                xsSimpleType = xsType.getBaseType().asSimpleType();
            }
            Element element = jrxElement.getXmlElement();
            String content = null;
            if (element != null) {
                Node textNode = element.getFirstChild();
                if (textNode != null)
                    content = textNode.getTextContent();
            }

            if (jrxElement.isLeaf() && isElementMandatory(jrxElement) && (content == null || content.equals(""))) {
                if (isElementMandatory(jrxElement))
                    validationResult.add(new XmlValidationError(jrxElement,
                            XmlValidationMessage.ERROR_NODE_MANDATORY, jrxElement.getName()));
            } else {
                if (content != null && content.length() > 0 && xsSimpleType != null && xsSimpleType.isRestriction()) {
                    validationResult.addAll(validateXmlValue(jrxElement, content, xsSimpleType));
                }
            }

        }
        return validationResult;
    }

    protected List<XmlValidationError> validateXmlValue(JrxElement context, String value, XSSimpleType xsSimpleType) {
        List<XmlValidationError> validationResult = new ArrayList<XmlValidationError>();

        if (xsSimpleType == null || !xsSimpleType.isRestriction())
            return validationResult;

        XSRestrictionSimpleType xsRestrictionSimpleType = xsSimpleType.asRestriction();
        // retrieve enumeration
        List<? extends XSFacet> facets = xsRestrictionSimpleType.getFacets(XSFacet.FACET_ENUMERATION);

        if (facets != null && facets.size() > 0) {

            StringBuilder enumText = new StringBuilder();
            // Enumeration
            boolean found = false;
            for (XSFacet facet : facets) {
                enumText.append(facet.getValue().value).append(",");

                if (!found && value.equals(facet.getValue().value)) {
                    found = true;
                }
            }
            if (!found) {
                validationResult.add(new XmlValidationError(context,
                        XmlValidationMessage.ERROR_INVALID_CONTENT_ALLOWED_VALUE, value, enumText.substring(0,
                                enumText.length() - 1)));
            }
        } else {
            // handle other restrictions such as xs:pattern, xs:minLength, xs:maxLength and set them as
            // constraints to the widget
            validationResult.addAll(validateXmlValueAgainstPatternAndLength(context, value, xsSimpleType));
        }

        return validationResult;
    }

    public List<XmlValidationError> validateAttribute(JrxAttribute jrxAttribute) {
        List<XmlValidationError> validationResult = new ArrayList<XmlValidationError>();

        JrxElement owner = jrxAttribute.getOwner();
        Element xmlOwner = owner.getXmlElement();

        String attributeValue = xmlOwner.getAttribute(jrxAttribute.getSimpleName());

        XSSimpleType xsSimpleType = jrxAttribute.getXsdDeclaration().getType();

        if (jrxAttribute.isMandatory() && attributeValue.equals("")) {
            validationResult.add(new XmlValidationError(owner, XmlValidationMessage.ERROR_ATTRIBUTE_MANDATORY,
                    jrxAttribute.getName(), owner.getName()));

            return validationResult;
        }

        if (!attributeValue.equals("") && xsSimpleType != null && xsSimpleType.isRestriction()) {
            validationResult.addAll(validateXmlValue(owner, attributeValue, xsSimpleType));
        }

        return validationResult;
    }

    public String getNamespaceUri(String prefix) {
        return namespaceMap.get(prefix);
    }

    private JrxElementGroup promoteElementGroupForChoiceGroup(JrxChoiceGroup jrxChoiceGroup, JrxElement jrxChildElement) {
        XSModelGroup xsChoiceGroup = jrxChoiceGroup.getXsdDeclaration();
        if (xsChoiceGroup == null)
            return null;

        XSParticle xsElementGroupParticle = findElementGroupParticle(xsChoiceGroup, jrxChildElement.getXsdDeclaration());
        if (xsElementGroupParticle == null)
            return null;

        XSModelGroup xsElementGroup = xsElementGroupParticle.getTerm().asModelGroup();

        JrxElementGroup jrxElementGroup = new JrxElementGroup();
        jrxElementGroup.setXsdDeclaration(xsElementGroup);
        jrxElementGroup.setCompositor(xsElementGroup.getCompositor());
        jrxElementGroup.setMinOccurs(xsElementGroupParticle.getMinOccurs());
        jrxElementGroup.setMaxOccurs(xsElementGroupParticle.getMaxOccurs());

        jrxElementGroup.setParentBlock(jrxChoiceGroup);
        jrxChoiceGroup.setSelection(jrxElementGroup);

        return jrxElementGroup;
    }

    private XSParticle findElementGroupParticle(XSModelGroup xsChoiceGroup, XSElementDecl xsElement) {
        for (XSParticle xsChildParticle : xsChoiceGroup.getChildren()) {
            if (xsChildParticle.getTerm().isModelGroup()) {
                XSModelGroup xsChildModelGroup = xsChildParticle.getTerm().asModelGroup();
                for (XSParticle xsGrandChildParticle : xsChildModelGroup.getChildren()) {
                    if (xsGrandChildParticle.getTerm().equals(xsElement)) {
                        return xsChildParticle;
                    }
                }
            }
        }
        return null;
    }

    public static boolean isChoiceGroupOfElement(JrxChoiceGroup jrxChoiceGroup) {
        if (jrxChoiceGroup.getXsdDeclaration() == null)
            return false;

        for (XSParticle xsParticle : jrxChoiceGroup.getXsdDeclaration().getChildren()) {
            if (xsParticle.getTerm().isModelGroup()) {
                // check if only single element is inside the model group
                if (xsParticle.getTerm().asModelGroup().getChildren().length > 1)
                    return false; // the model group contains more than 1 child. No support for recursive check.
            } else if (xsParticle.getTerm().isModelGroupDecl()) {
                // check if only single element is inside the model group declaration
                if (xsParticle.getTerm().asModelGroupDecl().getModelGroup().getChildren().length > 1)
                    return false; // the model group declaration contains more than 1 child. No support for recursive
                                  // check.
            }
        }

        return true;
    }

    public void registerNamespace(NamespacePrefixAndUri namespace) {
        if (namespaceMap.containsKey(namespace.getPrefix())
                && namespace.getUri().equals(namespaceMap.get(namespace.getPrefix())))
            return;

        namespaceMap.put(namespace.getPrefix(), namespace.getUri());
    }

    public void registerNamespaces(NamespacePrefixAndUri[] namespaceArray) {
        for (NamespacePrefixAndUri namespace : namespaceArray) {
            registerNamespace(namespace);
        }
    }

    public void registerNamespaces(Collection<NamespacePrefixAndUri> namespaces) {
        for (NamespacePrefixAndUri namespace : namespaces) {
            registerNamespace(namespace);
        }
    }

    private String findProperNamespacePrefixFromTargetNamespace(JrxDeclaration<?> jrxParentDeclaration,
            String namespaceUri) {
        if (namespaceUri == null || namespaceUri.equals(""))
            return "";

        String prefix = "";
        if (jrxParentDeclaration != null) {
            JrxElement jrxReferenceElement = null;
            if (jrxParentDeclaration instanceof JrxElement) {
                jrxReferenceElement = (JrxElement) jrxParentDeclaration;
            } else {
                jrxReferenceElement = jrxParentDeclaration.getParentElement();
            }
            if (jrxReferenceElement != null) {
                Element xmlReferenceElement = jrxReferenceElement.getXmlElement();
                if (xmlReferenceElement != null) {
                    prefix = xmlReferenceElement.lookupPrefix(namespaceUri); // Lookup from XML
                    return prefix == null ? "" : prefix;
                }

                if (jrxReferenceElement.getNamespaceUri() != null
                        && jrxReferenceElement.getNamespaceUri().equals(namespaceUri)) {
                    return jrxReferenceElement.getNamespacePrefix();
                }
                prefix = jrxReferenceElement.getNamespacePrefixFromUri(namespaceUri);
                if (prefix == null) {
                    return findProperNamespacePrefixFromTargetNamespace(jrxReferenceElement.getParentElement(),
                            namespaceUri);
                } else {
                    return prefix;
                }
            }
        }

        if (prefix == null || prefix.equals("")) {
            // last possibilities, search from element hierarchy
            prefix = findProperNamespacePrefixFromNamespaceMap(namespaceUri);
        }

        if (prefix != null) {
            // Find from namespace map. Namespace from XML conversion need to be settled and cleanup.
            // This would not support multi-empty namespace e.g.:
            // <root xmlns="namespace1"><level1 xmlns="namespace2" /></root>
            return prefix;
        }

        return "";
    }

    private String findProperNamespacePrefixFromNamespaceMap(String namespaceUri) {
        if (namespaceUri == null || namespaceUri.equals(""))
            return null;

        for (Entry<String, String> namespaceEntry : namespaceMap.entrySet()) {
            if (namespaceUri.equals(namespaceEntry.getValue())) {
                return namespaceEntry.getKey();
            }
        }

        return null;
    }

    private JrxElementGroup createJrxElementGroup(XSModelGroup xsModelGroup, int minOccurs, int maxOccurs) {
        if (XSModelGroup.CHOICE.equals(xsModelGroup.getCompositor())) {
            return createJrxChoiceGroup(xsModelGroup, minOccurs, maxOccurs);
        }

        JrxElementGroup jrxElementGroup = new JrxElementGroup();
        jrxElementGroup.setXsdDeclaration(xsModelGroup);
        jrxElementGroup.setCompositor(xsModelGroup.getCompositor());
        jrxElementGroup.setMinOccurs(minOccurs);
        jrxElementGroup.setMaxOccurs(maxOccurs);

        XSParticle[] xsChildParticles = xsModelGroup.getChildren();
        if (xsChildParticles.length == 1 && xsChildParticles[0].getTerm().isModelGroup()) {
            JrxElementGroup jrxSingleChildElementGroup = createJrxElementGroup(xsChildParticles[0].getTerm()
                    .asModelGroup(), xsChildParticles[0].getMinOccurs().intValue(), xsChildParticles[0].getMaxOccurs().intValue());
            jrxElementGroup.getElements().add(jrxSingleChildElementGroup);
            jrxSingleChildElementGroup.setParentBlock(jrxElementGroup);
        }

        return jrxElementGroup;
    }

    private JrxElementGroup getScopedElementGroupByElementName(JrxElementGroup jrxStartElementGroup,
            String simpleTagName) {
        XSModelGroup xsStartModelGroup = jrxStartElementGroup.getXsdDeclaration();

        if (xsStartModelGroup == null) {
            return null;
        }

        List<JrxElementGroup> jrxPendingChildElementGroupToScan = new ArrayList<JrxElementGroup>();

        XSParticle[] xsChildParticles = xsStartModelGroup.getChildren();
        for (XSParticle xsChildParticle : xsChildParticles) {
            XSTerm xsChildTerm = xsChildParticle.getTerm();
            if (xsChildTerm instanceof XSElementDecl) {
                XSElementDecl xsChildElement = xsChildTerm.asElementDecl();
                if (xsChildElement.getName().equals(simpleTagName))
                    return jrxStartElementGroup;
            } else if (xsChildTerm instanceof XSModelGroup) {
                // Search the related child JrxElementGroup
                for (JrxTerm<?> jrxChildTerm : jrxStartElementGroup.getElements()) {
                    if (xsChildTerm.equals(jrxChildTerm.getXsdDeclaration())) {
                        // submit to pending list
                        jrxPendingChildElementGroupToScan.add((JrxElementGroup) jrxChildTerm);
                    }
                }
            } else if (xsChildTerm instanceof XSModelGroupDecl) {
                // Not yet supported!
            } else {
                log.info("Unhandled XSTerm: " + xsChildTerm);
            }
        }

        for (JrxElementGroup jrxChildElementGroup : jrxPendingChildElementGroupToScan) {
            JrxElementGroup jrxEffectiveChildElementGroup = getScopedElementGroupByElementName(jrxChildElementGroup,
                    simpleTagName);
            if (jrxEffectiveChildElementGroup != null)
                return jrxEffectiveChildElementGroup;
        }

        return null;
    }

    public static XSParticle getXsElementParticleFromParentBlockByElementName(JrxElementGroup jrxParentElementGroup,
            String simpleTagName) {
        XSModelGroup xsStartModelGroup = jrxParentElementGroup.getXsdDeclaration();

        List<JrxElementGroup> jrxPendingChildElementGroupToScan = new ArrayList<JrxElementGroup>();

        XSParticle[] xsChildParticles = xsStartModelGroup.getChildren();
        for (XSParticle xsChildParticle : xsChildParticles) {
            XSTerm xsChildTerm = xsChildParticle.getTerm();
            if (xsChildTerm instanceof XSElementDecl) {
                XSElementDecl xsChildElement = xsChildTerm.asElementDecl();
                if (xsChildElement.getName().equals(simpleTagName))
                    return xsChildParticle;
            } else if (xsChildTerm instanceof XSModelGroup) {
                // Search the related child JrxElementGroup
                for (JrxTerm<?> jrxChildTerm : jrxParentElementGroup.getElements()) {
                    if (xsChildTerm.equals(jrxChildTerm.getXsdDeclaration())) {
                        // submit to pending list
                        jrxPendingChildElementGroupToScan.add((JrxElementGroup) jrxChildTerm);
                    }
                }
            } else if (xsChildTerm instanceof XSModelGroupDecl) {
                // Not yet supported!
            } else {
                log.info("Unhandled XSTerm: " + xsChildTerm);
            }
        }

        for (JrxElementGroup jrxChildElementGroup : jrxPendingChildElementGroupToScan) {
            XSParticle xsEffectiveChildParticle = getXsElementParticleFromParentBlockByElementName(
                    jrxChildElementGroup, simpleTagName);
            if (xsEffectiveChildParticle != null)
                return xsEffectiveChildParticle;
        }

        return null;
    }

    public static XSParticle[] getXsParticlePathFromParentBlockByElementName(XSModelGroup xsStartModelGroup,
            String simpleTagName) {

        XSParticle[] xsChildParticles = xsStartModelGroup.getChildren();
        for (XSParticle xsChildParticle : xsChildParticles) {
            XSTerm xsChildTerm = xsChildParticle.getTerm();
            if (xsChildTerm instanceof XSElementDecl) {
                XSElementDecl xsChildElement = xsChildTerm.asElementDecl();
                if (xsChildElement.getName().equals(simpleTagName))
                    return new XSParticle[] { xsChildParticle };
            } else if (xsChildTerm instanceof XSModelGroup) {
                XSParticle[] xsSubParticlePath = getXsParticlePathFromParentBlockByElementName(
                        xsChildTerm.asModelGroup(), simpleTagName);
                if (xsSubParticlePath.length > 0) {
                    return (XSParticle[]) ArrayUtils.insert(0, xsSubParticlePath, xsChildParticle);
                }
            } else if (xsChildTerm instanceof XSModelGroupDecl) {
                // Not yet supported!
            } else {
                log.info("Unhandled XSTerm: " + xsChildTerm);
            }
        }

        return new XSParticle[] {};
    }

    public static XSParticle getXsElementParticleFromParentBlockByXsElementDeclaration(
            JrxElementGroup jrxParentElementGroup, XSElementDecl xsElementDecl) {
        XSModelGroup xsStartModelGroup = jrxParentElementGroup.getXsdDeclaration();

        List<JrxElementGroup> jrxPendingChildElementGroupToScan = new ArrayList<JrxElementGroup>();

        XSParticle[] xsChildParticles = xsStartModelGroup.getChildren();
        for (XSParticle xsChildParticle : xsChildParticles) {
            XSTerm xsChildTerm = xsChildParticle.getTerm();
            if (xsChildTerm instanceof XSElementDecl) {
                XSElementDecl xsChildElement = xsChildTerm.asElementDecl();
                if (xsChildElement.equals(xsElementDecl))
                    return xsChildParticle;
            } else if (xsChildTerm instanceof XSModelGroup) {
                // Search the related child JrxElementGroup
                for (JrxTerm<?> jrxChildTerm : jrxParentElementGroup.getElements()) {
                    if (xsChildTerm.equals(jrxChildTerm.getXsdDeclaration())) {
                        // submit to pending list
                        jrxPendingChildElementGroupToScan.add((JrxElementGroup) jrxChildTerm);
                    }
                }
            } else if (xsChildTerm instanceof XSModelGroupDecl) {
                // Not yet supported!
            } else {
                log.info("Unhandled XSTerm: " + xsChildTerm);
            }
        }

        for (JrxElementGroup jrxChildElementGroup : jrxPendingChildElementGroupToScan) {
            XSParticle xsEffectiveChildParticle = getXsElementParticleFromParentBlockByXsElementDeclaration(
                    jrxChildElementGroup, xsElementDecl);
            if (xsEffectiveChildParticle != null)
                return xsEffectiveChildParticle;
        }

        return null;
    }

    public static JrxDeclaration<?> getParentDeclaration(JrxDeclaration<?> jrxDeclaration) {
        if (jrxDeclaration == null)
            return null;

        JrxElementGroup parentBlock = jrxDeclaration.getParentBlock();
        if (parentBlock == null) {
            // Try to check if it is from a choice group
            JrxChoiceGroup jrxParentChoiceGroup = jrxDeclaration.getOrigin();

            if (jrxParentChoiceGroup != null) {
                if (jrxParentChoiceGroup.getOwner() != null)
                    return jrxParentChoiceGroup.getOwner();

                if (jrxParentChoiceGroup.getParentBlock() != null)
                    parentBlock = jrxParentChoiceGroup.getParentBlock();
            }
        }

        return getParentDeclaration(parentBlock);
    }

    public static JrxDeclaration<?> getParentDeclaration(JrxElementGroup parentBlock) {
        if (parentBlock == null) {
            return null;
        }

        // 1. Try from owner
        if (parentBlock.getOwner() != null) {
            return parentBlock.getOwner();
        }

        // 2. Try to check if it is from parent block (this element nested element group)
        JrxDeclaration<?> jrxParentDeclaration = getParentDeclaration(parentBlock.getParentBlock());
        if (jrxParentDeclaration != null) {
            return jrxParentDeclaration;
        }

        // 3. Try to check if it is from a choice group
        JrxChoiceGroup jrxParentChoiceGroup = parentBlock.getOrigin();
        if (jrxParentChoiceGroup != null) {
            if (jrxParentChoiceGroup.getOwner() != null)
                return jrxParentChoiceGroup.getOwner();

            if (jrxParentChoiceGroup.getParentBlock() != null) {
                jrxParentDeclaration = getParentDeclaration(jrxParentChoiceGroup.getParentBlock());
                if (jrxParentDeclaration != null) {
                    return jrxParentDeclaration;
                }
            }
        }

        return null;
    }

    public static JrxElement getParentElement(JrxElementGroup jrxElementGroup) {
        if (jrxElementGroup == null) {
            return null;
        }

        JrxDeclaration<?> jrxParentDeclaration = getParentDeclaration(jrxElementGroup);

        if (jrxParentDeclaration == null) {
            // no more parent element
            return null;
        }

        while (jrxParentDeclaration instanceof JrxGroup) {
            jrxParentDeclaration = getParentDeclaration(jrxParentDeclaration);
            if (jrxParentDeclaration == null) {
                // no more parent element
                return null;
            }
            if (jrxParentDeclaration instanceof JrxElement)
                break;
        }

        return (JrxElement) jrxParentDeclaration;
    }

    private void buildUpNestedElementGroups(JrxElementGroup parentBlock, XSParticle[] xsParticlePath) {
        if (parentBlock == null || xsParticlePath == null || xsParticlePath.length < 1) {
            return;
        }

        XSParticle xsParticle = xsParticlePath[0];
        XSTerm xsTerm = xsParticle.getTerm();
        if (xsParticle.getTerm().isModelGroup()) {
            JrxElementGroup jrxChildElementGroup = createJrxElementGroup(xsTerm.asModelGroup(),
                    xsParticle.getMinOccurs().intValue(), xsParticle.getMaxOccurs().intValue());
            jrxChildElementGroup.setParentBlock(parentBlock);
            if (xsParticlePath.length > 1
                    && (xsParticlePath[1].getTerm().isModelGroup() || xsParticlePath[1].getTerm().isModelGroupDecl())) {
                buildUpNestedElementGroups(jrxChildElementGroup, (XSParticle[]) ArrayUtils.remove(xsParticlePath, 0));
            }
        } else if (xsParticle.getTerm().isModelGroupDecl()) {
            JrxGroup jrxChildGroup = createJrxGroup(xsTerm.asModelGroupDecl(), getParentDeclaration(parentBlock),
                    -1, xsParticle.getMinOccurs().intValue(), xsParticle.getMaxOccurs().intValue());
            jrxChildGroup.setParentBlock(parentBlock);
            if (xsParticlePath.length > 1
                    && (xsParticlePath[1].getTerm().isModelGroup() || xsParticlePath[1].getTerm().isModelGroupDecl())) {
                buildUpNestedElementGroups(jrxChildGroup.getChildrenBlock(),
                        (XSParticle[]) ArrayUtils.remove(xsParticlePath, 0));
            }
        }
    }

    protected boolean hasSchemas() {
        return !xsSchemaMap.isEmpty();
    }

    protected boolean hasSchema(String namespaceUri) {
        return xsSchemaMap.containsKey(namespaceUri);
    }

    public void clearEnrichmentIgnoreList() {
        enrichmentIgnoreList.clear();
    }

    public void setEnrichmentIgnoreList(Collection<String> ignoreList) {
        this.enrichmentIgnoreList.addAll(ignoreList);
    }

    protected boolean isForEnrichmentIgnored(JrxElement jrxElement) {
        if (enrichmentIgnoreList.contains(jrxElement.getSimpleName())) {
            return true;
        }

        return false;
    }

    public JrxDocument enrichJrxDocument(JrxDocument jrxDocument, String[][] enrichmentPoints) throws Exception {
        if (enrichmentPoints == null)
            return jrxDocument;

        for (int i = 0; i < enrichmentPoints.length; i++) {
            // Only enrich the given points, not the whole documents
            String[] enrichmentPoint = enrichmentPoints[i];

            String enrichmentPointNamespacePrefix = enrichmentPoint[0];
            String enrichmentPointTagNameWithPrefix = enrichmentPoint[1];
            JrxElement jrxEnrichmentPointElement = retrieveElementByPathAndPrefix(jrxDocument,
                    enrichmentPointTagNameWithPrefix, enrichmentPointNamespacePrefix);
            if (jrxEnrichmentPointElement == null) {
                // Missing enrichment point: -> create the node based on next sibling
                JrxElement jrxNextSiblingElement = null;
                String enrichmentPointTagName = null;
                if (enrichmentPoint.length > 2) {
                    enrichmentPointTagName = enrichmentPoint[2];
                    int siblingIndex = 3;
                    while (enrichmentPoint.length > siblingIndex) {
                        String nextSiblingNamespacePrefix = enrichmentPoint[siblingIndex];
                        String nextSiblingXpathWithPrefix = enrichmentPoint[siblingIndex + 1];
                        jrxNextSiblingElement = retrieveElementByPathAndPrefix(jrxDocument,
                                nextSiblingXpathWithPrefix, nextSiblingNamespacePrefix);
                        if (jrxNextSiblingElement != null)
                            break;
                        siblingIndex += 2;
                    }
                }

                if (jrxNextSiblingElement != null) {
                    // Create element from sibling
                    Element xmlParentElement = (Element) jrxNextSiblingElement.getXmlElement().getParentNode();
                    Element xmlEnrichmentPointElement = XmlUtils.createSubElement(xmlParentElement,
                            jrxNextSiblingElement.getNamespaceUri(enrichmentPointNamespacePrefix),
                            enrichmentPointTagName);
                    xmlParentElement.insertBefore(xmlEnrichmentPointElement, jrxNextSiblingElement.getXmlElement());
                    jrxEnrichmentPointElement = convertXmlToJrxModel(xmlEnrichmentPointElement,
                            jrxNextSiblingElement.getParentElement());
                    jrxEnrichmentPointElement.setOwnerDocument(jrxDocument);
                }
            }
            if (jrxEnrichmentPointElement != null) {
                enrichJrxElement(jrxEnrichmentPointElement);
                enrichJrxElementRecursive(jrxEnrichmentPointElement);
                addChoiceHelperElement(jrxEnrichmentPointElement);
            }
        }

        return jrxDocument;
    }

    protected JrxElement retrieveElementByPathAndPrefix(JrxDocument jrxDocument, String xpathWithPrefix,
            String prefix) throws Exception {
        if (getNamespacePrefixes() == null || getNamespacePrefixes().length < 1) {
            String enrichmentPointTagNameWithoutPrefix = removeNamespacePrefixFromXpath(xpathWithPrefix);
            return getElementByXPath(jrxDocument, null, enrichmentPointTagNameWithoutPrefix);
        } else {
            String namespaceUri = getNamespaceUri(prefix);
            if (namespaceUri == null) {
                return getElementByXPath(jrxDocument, getNamespacePrefixes(), prefix);
            } else {
                return getElementByXPath(jrxDocument, new String[][] { { prefix, namespaceUri } }, xpathWithPrefix);
            }
        }
    }

    protected String removeNamespacePrefixFromXpath(String xpathWithPrefix) {
        String result = xpathWithPrefix;

        result = result.replaceAll("[/]{2}[\\D][\\w\\-]*[:]", "//");
        result = result.replaceAll("[/][\\D][\\w\\-]*[:]", "/");

        return result;
    }

    public boolean isElementMandatory(JrxElement jrxElement) {
        return jrxElement.isMandatory();
    }

    public boolean isElementEmpty(JrxElement jrxElement) {
        return jrxElement.isEmpty();
    }

    public void setToStringWithValue(boolean toStringWithValue) {
        this.toStringWithValue = toStringWithValue;
    }

    protected List<XmlValidationError> validateXmlValueAgainstPatternAndLength(JrxElement context, String value,
            XSSimpleType xsSimpleType) {
        List<XmlValidationError> validationResult = new ArrayList<XmlValidationError>();

        XSRestrictionSimpleType xsRestrictionSimpleType = xsSimpleType.asRestriction();

        List<? extends XSFacet> facetPattern = xsRestrictionSimpleType.getFacets(XSFacet.FACET_PATTERN);
        if (facetPattern != null && facetPattern.size() == 1) { // if pattern found
            String pattern = facetPattern.get(0).getValue().value;
            if (!value.matches(pattern)) {
                validationResult.add(new XmlValidationError(context,
                        XmlValidationMessage.ERROR_INVALID_CONTENT_PATTERN, value, pattern));
            }

        }

        // minLength and maxLength
        XSFacet maxLengthFacet = xsRestrictionSimpleType.getFacet(XSFacet.FACET_MAXLENGTH);
        XSFacet minLengthFacet = xsRestrictionSimpleType.getFacet(XSFacet.FACET_MINLENGTH);

        if (minLengthFacet != null) { // if minLength/maxLength provided
            int minLength = Integer.parseInt(minLengthFacet.getValue().value);

            if (value.length() < minLength) {
                validationResult.add(new XmlValidationError(context,
                        XmlValidationMessage.ERROR_INVALID_CONTENT_MIN_LENGTH, value, String.valueOf(minLength)));
            }
        }
        if (maxLengthFacet != null) { // if minLength/maxLength provided
            int maxLength = Integer.parseInt(maxLengthFacet.getValue().value);

            if (value.length() > maxLength) {
                validationResult.add(new XmlValidationError(context,
                        XmlValidationMessage.ERROR_INVALID_CONTENT_MAX_LENGTH, value, String.valueOf(maxLength)));
            }
        }

        XSFacet minInclusiveFacet = xsRestrictionSimpleType.getFacet(XSFacet.FACET_MINEXCLUSIVE);

        if (minInclusiveFacet != null) { // if minLength/maxLength provided
            int minInclusive = Integer.parseInt(minInclusiveFacet.getValue().value);

            if (Float.parseFloat(value) < minInclusive) {
                validationResult.add(new XmlValidationError(context,
                        XmlValidationMessage.ERROR_INVALID_CONTENT_SMALL_VALUE, String.valueOf(minInclusive)));
            }
        }

        XSFacet fractionDigitsFacet = xsRestrictionSimpleType.getFacet(XSFacet.FACET_FRACTIONDIGITS);

        if (fractionDigitsFacet != null) { // if minLength/maxLength provided
            int fractionDigits = Integer.parseInt(fractionDigitsFacet.getValue().value);

            if (value.indexOf('.') > -1 && value.substring(value.indexOf('.') + 1).length() > fractionDigits) {
                validationResult.add(new XmlValidationError(context,
                        XmlValidationMessage.ERROR_INVALID_CONTENT_DECIMAL_PLACE, String.valueOf(fractionDigits)));
            }
        }

        return validationResult;
    }

    /**
     * Retrieve child element by simple tag name.
     * 
     * @param jrxParentElement
     *            parent element
     * @param simpleTagName
     *            name of the tag without prefix
     * @return child element
     */
    public static final JrxElement getChildElement(JrxElement jrxParentElement, String simpleTagName) {
        if (jrxParentElement == null || jrxParentElement.getChildrenBlock() == null)
            return null;

        String endingTagName = ":" + simpleTagName;
        for (JrxTerm<?> jrxTerm : jrxParentElement.getChildrenBlock().getElements()) {
            if (!(jrxTerm instanceof JrxElement))
                continue;

            JrxElement jrxElement = (JrxElement) jrxTerm;
            if (jrxElement.getSimpleName().equals(simpleTagName) || jrxElement.getName().endsWith(endingTagName)) {
                return jrxElement;
            }
        }

        return null;
    }

    /**
     * Add child element recursively in a simple manner where all siblings are element (no model group or group).
     * 
     * @param jrxParentElement
     *            parent element
     * @param simpleTagName
     *            tag name of the child element
     * @param respectSequence
     * @return added child element
     */
    public JrxElement addChildElement(JrxElement jrxParentElement, String simpleTagName, boolean respectSequence) {
        return addChildElement(jrxParentElement, simpleTagName, respectSequence, true);
    }

    /**
     * Add child element in a simple manner where all siblings are element (no model group or group).
     * 
     * @param jrxParentElement
     *            parent element
     * @param simpleTagName
     *            tag name of the child element
     * @param respectSequence
     * @param recursive
     * @return added child element
     */
    public JrxElement addChildElement(JrxElement jrxParentElement, String simpleTagName, boolean respectSequence,
            boolean recursive) {
        if (jrxParentElement == null || jrxParentElement.getChildrenBlock() == null
                || jrxParentElement.getChildrenBlock().getXsdDeclaration() == null || simpleTagName == null)
            return null;

        JrxElement jrxChildNextSiblingElement = null;

        XSModelGroup xsModelGroup = jrxParentElement.getChildrenBlock().getXsdDeclaration();
        XSParticle xsChildParticle = null;
        List<String> childNextSiblingList = new ArrayList<String>();
        boolean nextSibling = false;
        for (XSParticle xsParticle : xsModelGroup.getChildren()) {
            if (!xsParticle.getTerm().isElementDecl())
                continue;

            String particleName = xsParticle.getTerm().asElementDecl().getName();
            if (simpleTagName.equals(particleName)) {
                xsChildParticle = xsParticle;
                nextSibling = true;
            } else {
                if (nextSibling) {
                    childNextSiblingList.add(particleName);
                }
            }
        }

        if (respectSequence) {
            // Find child next sibling JrxElement to ensure sequence is correct
            for (JrxTerm<?> jrxChildTerm : jrxParentElement.getChildrenBlock().getElements()) {
                if (!(jrxChildTerm instanceof JrxElement))
                    continue;

                JrxElement jrxScannedChildElement = (JrxElement) jrxChildTerm;
                if (childNextSiblingList.contains(jrxScannedChildElement.getSimpleName())) {
                    jrxChildNextSiblingElement = jrxScannedChildElement;
                    break;
                }
            }
        }

        JrxElement jrxChildElement = convertXsElementToJrxElement(xsChildParticle.getTerm().asElementDecl(),
                jrxParentElement);
        jrxChildElement.setMinOccurs(xsChildParticle.getMinOccurs());
        jrxChildElement.setMaxOccurs(xsChildParticle.getMaxOccurs());

        if (jrxChildNextSiblingElement == null) {
            jrxParentElement.getChildrenBlock().getElements().add(jrxChildElement);
        } else {
            jrxParentElement
                    .getChildrenBlock()
                    .getElements()
                    .add(jrxParentElement.getChildrenBlock().getElements().indexOf(jrxChildNextSiblingElement),
                            jrxChildElement);
        }
        jrxChildElement.setParentBlock(jrxParentElement.getChildrenBlock());

        if (jrxChildNextSiblingElement == null) {
            jrxParentElement.getXmlElement().appendChild(jrxChildElement.getXmlElement());
        } else {
            jrxParentElement.getXmlElement().insertBefore(jrxChildElement.getXmlElement(),
                    jrxChildNextSiblingElement.getXmlElement());
        }

        return jrxChildElement;
    }

    /**
     * Remove child element.
     * 
     * @param jrxParentElement
     *            parent element that contains the element to remove.
     * @param jrxElement
     *            element to remove
     */
    public void removeChildElement(JrxElement jrxParentElement, JrxElement jrxElement) {
        if (jrxParentElement == null || jrxParentElement.getChildrenBlock() == null || jrxElement == null)
            return;

        jrxParentElement.getChildrenBlock().getElements().remove(jrxElement);
        jrxParentElement.getXmlElement().removeChild(jrxElement.getXmlElement());
    }

    public List<JrxAttribute> getAttributeListByXPath(JrxDocument jrxDocument, String[][] namespaces,
            String xPathExpression) throws Exception {
        Document xmlDoc = jrxDocument.getXmlDocument();
        if (xmlDoc == null) {
            return null;
        }
        List<JrxAttribute> jrxAttributeList = new ArrayList<>();

        NodeList xmlNodeList = null;
        if (namespaces == null) {
            xmlNodeList = XmlUtils.getNodeListByXPath(xmlDoc, xPathExpression);
        } else {
            xmlNodeList = XmlUtils.getNodeListByXPath(xmlDoc, namespaces, xPathExpression);
        }
        for (int i = 0; i < xmlNodeList.getLength(); i++) {
            Attr xmlAttr = (Attr) xmlNodeList.item(i);
            Element xmlElement = xmlAttr.getOwnerElement();
            JrxElement jrxElement = jrxDocument.get(xmlElement);
            if (jrxElement == null) {
                continue;
            }
            JrxAttribute jrxAttr = getAttribute(jrxElement, xmlAttr.getName());
            if (jrxAttr == null) {
                continue;
            }
            jrxAttributeList.add(jrxAttr);
        }

        return jrxAttributeList;
    }

    public static JrxAttribute getAttribute(JrxElement jrxElement, String attributeName) {
        if (jrxElement == null || attributeName == null || "".equals(attributeName)) {
            return null;
        }

        for (JrxAttribute jrxAttr : jrxElement.getAttributeList()) {
            if (jrxAttr.getName().equals(attributeName)) {
                return jrxAttr;
            }
        }

        return null;
    }

// @formatter:off
// Jumin: The following methods shall never be used because it breaks the concept of JRX XML model.
// The nested XSModelGroup shall be created as it is defined in the XML.
// Simplification of presentation shall be done in GUI.
//
//	protected static XSModelGroup resolveXsChoiceModelGroup(XSModelGroup xsModelGroup) {
//		if (xsModelGroup == null) return null;
//		
//		if (XSModelGroup.CHOICE.equals(xsModelGroup.getCompositor())) {
//			return xsModelGroup;
//		}
//		
//		XSParticle[] xsChildParticles = xsModelGroup.getChildren();
//		if (xsChildParticles.length == 1 && xsChildParticles[0].getTerm().isModelGroup()) {
//			// Only when it is a single child and the child is model group, search downward
//			return resolveXsChoiceModelGroup(xsChildParticles[0].getTerm().asModelGroup());
//		}
//		
//		return null;
//	}
//	
//	protected static XSParticle resolveXsChoiceModelGroupParticle(XSParticle xsModelGroupParticle) {
//		if (xsModelGroupParticle == null || !xsModelGroupParticle.getTerm().isModelGroup()) return null;
//		
//		XSModelGroup xsModelGroup = xsModelGroupParticle.getTerm().asModelGroup();
//		
//		if (XSModelGroup.CHOICE.equals(xsModelGroup.getCompositor())) {
//			return xsModelGroupParticle;
//		}
//		
//		XSParticle[] xsChildParticles = xsModelGroup.getChildren();
//		if (xsChildParticles.length == 1) {
//			// Only when it is a single child, search downward
//			return resolveXsChoiceModelGroupParticle(xsChildParticles[0]);
//		}
//		
//		return null;
//	}
// @formatter:on
}
