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
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.xml.XMLConstants;
import javax.xml.parsers.SAXParserFactory;

import org.apache.log4j.Logger;
import org.jrtech.common.xmlutils.XmlUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.sun.xml.xsom.XSAttributeUse;
import com.sun.xml.xsom.XSComplexType;
import com.sun.xml.xsom.XSContentType;
import com.sun.xml.xsom.XSElementDecl;
import com.sun.xml.xsom.XSModelGroup;
import com.sun.xml.xsom.XSModelGroupDecl;
import com.sun.xml.xsom.XSParticle;
import com.sun.xml.xsom.XSSchema;
import com.sun.xml.xsom.XSSchemaSet;
import com.sun.xml.xsom.XSSimpleType;
import com.sun.xml.xsom.XSTerm;
import com.sun.xml.xsom.XSType;
import com.sun.xml.xsom.parser.XSOMParser;

public class XsdToXmlUtil {

	private static final Logger log = Logger.getLogger(XsdToXmlUtil.class);

	private XSSchemaSet schemaSet = null;

	public static XsdToXmlUtil getInstance() {
		return new XsdToXmlUtil();
	}

	public Document convertSchema(URL schemaUrl, String rootElement) throws Exception {
		return convertSchema(schemaUrl, rootElement, false);
	}

	public Document convertSchema(URL schemaUrl, String rootElement, boolean includeOptional) throws Exception {
		log.info(schemaUrl.getPath());

		// Init XSOM Parser
		XSOMParser xsomParser = new XSOMParser(SAXParserFactory.newInstance());
		xsomParser.parse(schemaUrl);

		schemaSet = xsomParser.getResult();
		if (schemaSet == null) {
			throw new SchemaNotAvailableException();
		}

		// Load all namespaces in XSD
		Iterator<XSSchema> it = schemaSet.iterateSchema();
		XSSchema xsDefaultSchema = null;
		while (it.hasNext()) {
			XSSchema xsSchema = (XSSchema) it.next();
			String nameSpace = xsSchema.getTargetNamespace();
			if (nameSpace.equals(XMLConstants.W3C_XML_SCHEMA_NS_URI))
				continue;
			xsDefaultSchema = xsSchema;
			break;
		}

		return convertSchema(xsDefaultSchema, rootElement, includeOptional);
	}

	private Document convertSchema(XSSchema xsSchema, String rootElement, boolean includeOptional) throws Exception {
		Document xmlDoc = XmlUtils.newDocument();
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
				for (XSSchema xsCurrentSchema : schemaSet.getSchemas()) {
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

		Element xmlElement = convertElement(xmlDoc, xsRootElement, includeOptional);
		xmlElement.setAttribute(XMLConstants.XMLNS_ATTRIBUTE, xsSchema.getTargetNamespace());
		xmlDoc.appendChild(xmlElement);

		return xmlDoc;
	}

	public Element convertElement(Element xmlParentElement, XSElementDecl xsElementDeclaration, boolean includeOptional) {
		Element xmlElement = convertElement(xmlParentElement.getOwnerDocument(), xsElementDeclaration, includeOptional);
		xmlParentElement.appendChild(xmlElement);

		return xmlElement;
	}

	public Element convertElement(Document xmlDoc, XSElementDecl xsElementDeclaration, boolean includeOptional) {
		if (xmlDoc == null || xsElementDeclaration == null) {
			return null;
		}

		Element xmlElement = XmlUtils.createElement(xmlDoc, xsElementDeclaration.getName());

		XSType xsType = xsElementDeclaration.getType();
		if (xsType instanceof XSComplexType) {
			applyComplexTypeToElement(xmlElement, (XSComplexType) xsType, includeOptional);
		} else if (xsType instanceof XSSimpleType) {
			applySimpleTypeToElement(xmlElement, (XSSimpleType) xsType);
		}

		return xmlElement;
	}

	/**
	 * @param xmlElement
	 * @param type
	 */
	private void applySimpleTypeToElement(Element xmlElement, XSSimpleType xsSimpleType) {
		// TODO:
	}

	/**
	 * @param xmlElement
	 * @param type
	 */
	private void applyComplexTypeToElement(Element xmlElement, XSComplexType xsComplexType, boolean includeOptional) {
		if (xmlElement == null || xsComplexType == null) {
			return;
		}
		// Attribute
		for (Iterator<? extends XSAttributeUse> it = xsComplexType.iterateAttributeUses(); it.hasNext();) {
			XSAttributeUse xsAttr = it.next();
			xmlElement.setAttribute(xsAttr.getDecl().getName(), "");
		}

		// Sub element
		XSContentType xsContentType = xsComplexType.getContentType();
		if (xsContentType instanceof XSParticle) {
			XSParticle xsContentTypeParticle = xsContentType.asParticle();
			XSTerm xsContentTypeTerm = xsContentTypeParticle.getTerm();
			if (xsContentTypeTerm.isModelGroup()) {
				XSModelGroup xsModelGroup = (XSModelGroup) xsContentTypeTerm;
				List<XSElementDecl> xsSubElementList = getElementDeclarationList(xsModelGroup, false, includeOptional);
				for (int i = 0; i < xsSubElementList.size(); i++) {
					XSElementDecl xsSubElement = xsSubElementList.get(i);
					convertElement(xmlElement, xsSubElement, includeOptional);
				}
			} else {
				log.warn("Unhandled Complex Type Term: " + xsContentTypeTerm.getClass().getSimpleName());
			}
		} else {
			log.warn("Unhandlex Complex Type Content: " + xsContentType.getClass().getSimpleName());
		}
	}

	public List<XSElementDecl> getElementDeclarationList(XSModelGroup xsModelGroup, boolean includeChoice,
			boolean includeOptional) {
		List<XSElementDecl> elementList = new ArrayList<XSElementDecl>();

		for (int i = 0; i < xsModelGroup.getSize(); i++) {
			XSParticle xsModelGroupParticle = xsModelGroup.getChild(i);
			if (includeOptional || (xsModelGroupParticle.getMinOccurs().intValue() != 0)) {
				XSTerm xsModelGroupTerm = xsModelGroupParticle.getTerm();
				if (xsModelGroupTerm.isElementDecl()) {
					elementList.add((XSElementDecl) xsModelGroupTerm);
				} else if (xsModelGroupTerm.isModelGroup()) {
					XSModelGroup xsSubModelGroup = (XSModelGroup) xsModelGroupTerm;
					if (includeChoice || !XSModelGroup.Compositor.CHOICE.equals(xsSubModelGroup.getCompositor())) {
						elementList.addAll(getElementDeclarationList(xsSubModelGroup, includeChoice, includeOptional));
					}
				} else if (xsModelGroupTerm.isModelGroupDecl()) {
					elementList.addAll(getElementDeclarationList(((XSModelGroupDecl) xsModelGroupTerm).getModelGroup(),
							includeChoice, includeOptional));
				} else {
					log.warn("Unhandled Model Group Term: " + xsModelGroupTerm.getClass().getSimpleName());
				}
			}
		}

		return elementList;
	}

	public List<XSParticle> getElementParticleList(XSModelGroup xsModelGroup, boolean includeChoice,
			boolean includeOptional) {
		List<XSParticle> elementList = new ArrayList<XSParticle>();

		for (int i = 0; i < xsModelGroup.getSize(); i++) {
			XSParticle xsParticle = xsModelGroup.getChild(i);
			if (includeOptional || (xsParticle.getMinOccurs().intValue() != 0)) {
				XSTerm xsModelGroupTerm = xsParticle.getTerm();
				if (xsModelGroupTerm.isElementDecl()) {
					elementList.add(xsParticle);
				} else if (xsModelGroupTerm.isModelGroup()) {
					XSModelGroup xsSubModelGroup = (XSModelGroup) xsModelGroupTerm;
					if (includeChoice || !XSModelGroup.Compositor.CHOICE.equals(xsSubModelGroup.getCompositor())) {
						elementList.addAll(getElementParticleList(xsSubModelGroup, includeChoice, includeOptional));
					}
				} else if (xsModelGroupTerm.isModelGroupDecl()) {
					elementList.add(xsParticle);
				} else {
					log.warn("Unhandled Model Group Term: " + xsModelGroupTerm.getClass().getSimpleName());
				}
			}
		}

		return elementList;
	}

	public XSElementDecl getSchema(Element xmlElement, XSSchema xsSchema) {
		Node[] nodePath = XmlUtils.getPath(xmlElement);
		XSElementDecl xsElement = null;
		for (int i = 0; i < nodePath.length; i++) {
			Element xmlNode = (Element) nodePath[i];
			if (i < 1) {
				xsElement = xsSchema.getElementDecl(xmlNode.getNodeName());
			} else {
				xsElement = getSchema(xmlNode, (XSComplexType) xsElement.getType());
			}
		}

		return xsElement;
	}

	public XSElementDecl getSchema(Element xmlElement, XSComplexType xsComplexType) {
		if (xmlElement == null || xsComplexType == null) {
			return null;
		}

		// Sub element
		XSContentType xsContentType = xsComplexType.getContentType();
		if (xsContentType instanceof XSParticle) {
			XSParticle xsContentTypeParticle = xsContentType.asParticle();
			XSTerm xsContentTypeTerm = xsContentTypeParticle.getTerm();
			if (xsContentTypeTerm.isModelGroup()) {
				XSModelGroup xsModelGroup = (XSModelGroup) xsContentTypeTerm;
				for (int i = 0; i < xsModelGroup.getSize(); i++) {
					XSParticle xsModelGroupParticle = xsModelGroup.getChild(i);
					XSTerm xsModelGroupTerm = xsModelGroupParticle.getTerm();
					if (xsModelGroupTerm instanceof XSElementDecl) {
						XSElementDecl xsSubElement = (XSElementDecl) xsModelGroupTerm;
						if (xsSubElement.getName().equals(xmlElement.getNodeName())) {
							return xsSubElement;
						}
					} else {
						log.warn("Unhandled Model Group Term: " + xsModelGroupTerm.getClass().getSimpleName());
					}
				}
			} else {
				log.warn("Unhandled Complex Type Term: " + xsContentTypeTerm.getClass().getSimpleName());
			}
		} else {
			log.warn("Unhandlex Complex Type Content: " + xsContentType.getClass().getSimpleName());
		}
		return null;
	}

	public Element addChoiceXmlDocument(Element xmlElement, XSSchema xsSchema, int index) throws Exception {
		log.debug("Start...");
		XSElementDecl xsTargetElement = getSchema(xmlElement, xsSchema);

		log.debug("Get option index: " + index);
		// Get the i option from choice
		XSElementDecl xsChoiceElement = getChoiceElement((XSComplexType) xsTargetElement.getType(), index);

		log.debug("Convert schema element to xml element");
		// Add the option as element type
		convertElement(xmlElement, xsChoiceElement, true);
		log.debug("Done");
		return xmlElement;
	}

	public XSParticle getChoiceElementParticle(XSModelGroup xsModelGroup, int index) {
		if (index < 0)
			index = 0;

		XSParticle[] choiceParticleArray = getChoiceElementParticle(xsModelGroup);
		if (choiceParticleArray != null && choiceParticleArray.length > index) {
			return choiceParticleArray[index];
		}
		return null;
	}
	
	public int countChoiceElementParticle(XSModelGroup xsModelGroup) {
		XSParticle[] xsChoiceParticles = getChoiceElementParticle(xsModelGroup);
		
		if (xsChoiceParticles == null) return -1;
		
		return xsChoiceParticles.length;
	}

	public XSParticle[] getChoiceElementParticle(XSModelGroup xsModelGroup) {
		if (!xsModelGroup.getCompositor().equals(XSModelGroup.CHOICE)) {
			return null;
		}
		List<XSParticle> elementParticleList = getElementParticleList(xsModelGroup, true, true);
		return elementParticleList.toArray(new XSParticle[] {});
	}

	public XSElementDecl getChoiceElement(XSComplexType xsComplexType, int index) {
		if (index < 0)
			index = 0;

		XSElementDecl[] choiceElementArray = getChoiceElement(xsComplexType);
		if (choiceElementArray != null && choiceElementArray.length > index) {
			return choiceElementArray[index];
		}
		return null;
	}

	public int getChoiceElementIndex(XSModelGroup xsModelGroup, String elementName) {
		if (elementName == null || elementName.trim().length() < 1)
			return -1;

		List<XSElementDecl> elementList = getElementDeclarationList(xsModelGroup, true, true);
		if (elementList != null && elementList.size() > 0) {
			for (int i = 0; i < elementList.size(); i++) {
				XSElementDecl xsElementDecl = elementList.get(i);
				if (xsElementDecl.getName().equals(elementName))
					return i;
			}
		}
		return -1;
	}

	public XSElementDecl[] getChoiceElement(XSComplexType xsComplexType) {
		XSContentType xsContentType = xsComplexType.getContentType();
		if (xsContentType instanceof XSParticle) {
			XSParticle xsContentTypeParticle = xsContentType.asParticle();
			XSTerm xsContentTypeTerm = xsContentTypeParticle.getTerm();
			if (xsContentTypeTerm.isModelGroup()) {
				XSModelGroup xsModelGroup = (XSModelGroup) xsContentTypeTerm;
				List<XSElementDecl> elementList = getElementDeclarationList(xsModelGroup, true, true);
				return elementList.toArray(new XSElementDecl[] {});
			} else {
				log.warn("Unhandled Complex Type Term: " + xsContentTypeTerm.getClass().getSimpleName());
			}
		} else {
			log.warn("Unhandlex Complex Type Content: " + xsContentType.getClass().getSimpleName());
		}

		return null;
	}

	public boolean isChoiceType(XSComplexType xsComplexType) {
		XSContentType xsContentType = xsComplexType.getContentType();
		if (xsContentType instanceof XSParticle) {
			XSParticle xsContentTypeParticle = xsContentType.asParticle();
			XSTerm xsContentTypeTerm = xsContentTypeParticle.getTerm();
			if (xsContentTypeTerm.isModelGroup()) {
				XSModelGroup xsModelGroup = (XSModelGroup) xsContentTypeTerm;
				if (xsModelGroup.getCompositor().equals(XSModelGroup.CHOICE)) {
					return true;
				}

				if (xsModelGroup.getSize() == 1) {
					XSParticle xsChildParticle = xsModelGroup.getChild(0);
					if (xsChildParticle.getTerm() instanceof XSModelGroup) {
						if (((XSModelGroup) xsChildParticle.getTerm()).getCompositor().equals(XSModelGroup.CHOICE)) {
							return true;
						}
					}
				}
			} else {
				log.warn("Unhandled Complex Type Term: " + xsContentTypeTerm.getClass().getSimpleName());
			}
		} else {
			log.warn("Unhandlex Complex Type Content: " + xsContentType.getClass().getSimpleName());
		}

		return false;
	}

	public XSModelGroup getChoiceType(XSComplexType xsComplexType) {
		XSContentType xsContentType = xsComplexType.getContentType();
		if (xsContentType instanceof XSParticle) {
			XSParticle xsContentTypeParticle = xsContentType.asParticle();
			XSTerm xsContentTypeTerm = xsContentTypeParticle.getTerm();
			if (xsContentTypeTerm.isModelGroup()) {
				XSModelGroup xsModelGroup = (XSModelGroup) xsContentTypeTerm;
				if (xsModelGroup.getCompositor().equals(XSModelGroup.CHOICE)) {
					return xsModelGroup;
				}

				if (xsModelGroup.getSize() == 1) {
					XSParticle xsChildParticle = xsModelGroup.getChild(0);
					if (xsChildParticle.getTerm() instanceof XSModelGroup) {
						if (((XSModelGroup) xsChildParticle.getTerm()).getCompositor().equals(XSModelGroup.CHOICE)) {
							return (XSModelGroup) xsChildParticle.getTerm();
						}
					}
				}
			} else {
				log.warn("Unhandled Complex Type Term: " + xsContentTypeTerm.getClass().getSimpleName());
			}
		} else {
			log.warn("Unhandlex Complex Type Content: " + xsContentType.getClass().getSimpleName());
		}

		return null;
	}

}
