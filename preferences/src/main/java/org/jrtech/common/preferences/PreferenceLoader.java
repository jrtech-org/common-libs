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
package org.jrtech.common.preferences;

import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentNavigableMap;
import java.util.concurrent.ConcurrentSkipListMap;

import org.apache.log4j.Logger;
import org.jrtech.common.xmlutils.XmlUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class PreferenceLoader {
	public static final String TAG_PREFERENCES = "preferences";
	public static final String TAG_PREFERENCE = "preference";
	public static final String TAG_INCLUDE = "include";

	public static final String ATTRIBUTE_ENTITY = "entity";
	public static final String ATTRIBUTE_ID = "id";
	public static final String ATTRIBUTE_LAST = "lastUserColumnName";
	public static final String ATTRIBUTE_MODULE = "module";
	public static final String ATTRIBUTE_NAME = "name";
	public static final String ATTRIBUTE_OWNER = "ownerColumnName";
	public static final String ATTRIBUTE_SCOPE = "scope";
	public static final String ATTRIBUTE_TYPE = "type";
	public static final String ATTRIBUTE_URL = "url";

	public static final String URL_PREFIX_CLASSPATH = "classpath:";

	private static Logger log = Logger.getLogger(PreferenceLoader.class);

	private static final ConcurrentNavigableMap<String, Class<? extends PreferenceConverter<? extends Preference>>> concretePreferenceConverterMap = new ConcurrentSkipListMap<>();

	/**
	 * Loads preference list from XML string.
	 * 
	 * @param xmlString
	 * @return
	 * @throws Exception
	 */
	public List<Preference> load(String xmlString) throws Exception {
		Document xmlDoc = XmlUtils.createDocument(xmlString);

		List<Preference> prefList = load(xmlDoc);

		return prefList;
	}

	/**
	 * Loads preference list from input stream
	 * 
	 * @param xmlStream
	 * @return
	 * @throws Exception
	 */
	public List<Preference> load(InputStream xmlStream) throws Exception {
		Document xmlDoc = XmlUtils.createDocument(xmlStream);
		List<Preference> prefList = load(xmlDoc);
		return prefList;
	}

	/**
	 * Loads preference list from XML document
	 * 
	 * @param xmlDoc
	 * @return
	 * @throws Exception
	 */
	public List<Preference> load(Document xmlDoc) throws Exception {
		if (xmlDoc == null)
			return null;

		List<Element> xmlPreferenceNodeList = getPreferenceNodeList(xmlDoc);

		List<Preference> prefList = load(xmlPreferenceNodeList);

		List<Element> xmlIncludeNodeList = getIncludeNodeList(xmlDoc);
		for (Element xmlIncludeNode : xmlIncludeNodeList) {
			Document xmlIncludeDoc = getIncludeDoc(xmlIncludeNode);
			if (xmlIncludeDoc != null)
				prefList.addAll(load(xmlIncludeDoc));
		}

		return prefList;
	}

	protected Document getIncludeDoc(Element xmlIncludeNode) {
		if (xmlIncludeNode == null)
			return null;

		String urlString = xmlIncludeNode.getAttribute(ATTRIBUTE_URL);

		if (urlString.equals(""))
			return null;

		URL url = null;
		if (urlString.startsWith(URL_PREFIX_CLASSPATH)) {
			url = getClass().getResource(urlString.substring(URL_PREFIX_CLASSPATH.length()));
		} else {
			try {
				url = new URL(urlString);
			} catch (MalformedURLException e) {
				return null;
			}
		}
		try {
			return XmlUtils.openDocument(url);
		} catch (Exception e) {
			log.warn("Cannot find include url: '" + urlString
			        + "'. Processing will continue by ignoring this include URL.");
			return null;
		}
	}

	private List<Element> collectPreferenceElements(Document xmlDoc) throws Exception {
		List<Element> xmlPreferenceNodeList = new ArrayList<Element>();
		Element xmlRootElement = xmlDoc.getDocumentElement();
		if (xmlRootElement == null)
			return xmlPreferenceNodeList;

		if (xmlRootElement.getTagName().equals(TAG_PREFERENCE)) {
			xmlPreferenceNodeList.add(xmlRootElement);
		} else {
			xmlPreferenceNodeList = XmlUtils.getChildElementListByTagName(xmlRootElement, TAG_PREFERENCE);
		}

		return xmlPreferenceNodeList;
	}

	protected List<Element> getPreferenceNodeList(Document xmlDoc) throws Exception {
		return collectPreferenceElements(xmlDoc);
	}

	protected List<Element> getPreferenceNodeList(Document xmlDoc, String type) throws Exception {
		List<Element> xmlPreferenceNodeList = XmlUtils.getChildElementListByTagNameAndAttributeValues(
		        xmlDoc.getDocumentElement(), TAG_PREFERENCE, new String[] { PreferenceLoader.ATTRIBUTE_TYPE },
		        new String[] { type });

		if (xmlPreferenceNodeList == null || xmlPreferenceNodeList.size() < 1) {
			// Try without root with XPath
			NodeList nodeList = XmlUtils.getNodeListByXPath(xmlDoc, "//" + TAG_PREFERENCE + "[@" + ATTRIBUTE_TYPE + "='"
			        + type + "']");
			for (int i = 0; i < nodeList.getLength(); i++) {
				xmlPreferenceNodeList.add((Element) nodeList.item(i));
			}
		}

		return xmlPreferenceNodeList;
	}

	protected List<Element> getPreferenceNodeList(Document xmlDoc, String type, String module, String entity)
	        throws Exception {
		List<Element> xmlPreferenceNodeList = XmlUtils.getChildElementListByTagNameAndAttributeValues(
		        xmlDoc.getDocumentElement(), TAG_PREFERENCE, new String[] { ATTRIBUTE_TYPE, ATTRIBUTE_MODULE,
		                ATTRIBUTE_ENTITY }, new String[] { type, module, entity });

		if (xmlPreferenceNodeList == null || xmlPreferenceNodeList.size() < 1) {
			// Try without root with XPath
			NodeList nodeList = XmlUtils.getNodeListByXPath(xmlDoc, "//" + TAG_PREFERENCE + "[@" + ATTRIBUTE_TYPE + "='" + type
			        + "' and " + ATTRIBUTE_MODULE + "='" + module + "' and " + ATTRIBUTE_ENTITY + "='" + entity + "']");
			for (int i = 0; i < nodeList.getLength(); i++) {
				xmlPreferenceNodeList.add((Element) nodeList.item(i));
			}
		}

		return xmlPreferenceNodeList;
	}

	protected List<Element> getIncludeNodeList(Document xmlDoc) throws Exception {
		Element xmlRootElement = xmlDoc.getDocumentElement();

		return XmlUtils.getChildElementListByTagName(xmlRootElement, TAG_INCLUDE);
	}

	protected List<Preference> load(List<Element> xmlPreferenceNodeList) throws Exception {
		List<Preference> prefList = new ArrayList<Preference>();

		for (Element xmlPreferenceNode : xmlPreferenceNodeList) {
			Preference pref = xmlToPreference(xmlPreferenceNode);
			if (pref != null) {
				prefList.add(pref);
			}
		}
		return prefList;
	}

	/**
	 * Load preference list from XML Document
	 * 
	 * @param xmlDoc
	 * @param type
	 * @return
	 * @throws Exception
	 */
	public List<Preference> load(Document xmlDoc, String type) throws Exception {
		if (xmlDoc == null)
			return null;

		List<Element> xmlPreferenceNodeList = getPreferenceNodeList(xmlDoc, type);

		List<Preference> prefList = load(xmlPreferenceNodeList);

		List<Element> xmlIncludeNodeList = getIncludeNodeList(xmlDoc);
		for (Element xmlIncludeNode : xmlIncludeNodeList) {
			Document xmlIncludeDoc = getIncludeDoc(xmlIncludeNode);
			if (xmlIncludeDoc != null)
				prefList.addAll(load(xmlIncludeDoc, type));
		}

		return prefList;
	}

	/**
	 * Load single preference from XML string
	 * 
	 * @param xmlString
	 * @param type
	 * @param module
	 * @param entity
	 * @return
	 * @throws Exception
	 */
	public List<Preference> load(String xmlString, String type, String module, String entity) throws Exception {
		Document xmlDoc = XmlUtils.createDocument(xmlString);

		return load(xmlDoc, type, module, entity);
	}

	/**
	 * Load preference list from XML Document
	 * 
	 * @param xmlDoc
	 * @param type
	 * @param module
	 * @param entity
	 * @return
	 * @throws Exception
	 */
	public List<Preference> load(Document xmlDoc, String type, String module, String entity) throws Exception {
		if (xmlDoc == null)
			return null;

		List<Element> xmlPreferenceNodeList = getPreferenceNodeList(xmlDoc, type, module, entity);

		List<Preference> prefList = load(xmlPreferenceNodeList);

		List<Element> xmlIncludeNodeList = getIncludeNodeList(xmlDoc);
		for (Element xmlIncludeNode : xmlIncludeNodeList) {
			Document xmlIncludeDoc = getIncludeDoc(xmlIncludeNode);
			if (xmlIncludeDoc != null)
				prefList.addAll(load(xmlIncludeDoc, type, module, entity));
		}

		return prefList;
	}

	/**
	 * @param xmlString
	 * @return
	 * @throws Exception
	 */
	public Preference loadSingleConfig(String xmlString, String type, String module, String entity) throws Exception {
		Document xmlDoc = XmlUtils.createDocument(xmlString);

		return loadSingleConfig(xmlDoc, type, module, entity);
	}

	public Preference loadSingleConfig(Document xmlDoc, String type, String module, String entity) throws Exception {
		List<Element> nodeList = getPreferenceNodeList(xmlDoc, type, module, entity);

		if (nodeList.size() < 1)
			return null;

		Preference pref = xmlToPreference(nodeList.get(0));
		pref.setDefinitionValue(XmlUtils.documentToString(xmlDoc));

		return pref;
	}

	protected Preference xmlToPreference(Element xmlPreferenceNode) throws Exception {
		if (xmlPreferenceNode == null) {
			return null;
		}

		Preference model = new Preference();

		copyAttributeFromXml(xmlPreferenceNode, model);

		String prefContent = XmlUtils.nodeToString(xmlPreferenceNode);
		model.setDefinitionValue(prefContent);

		return model;
	}
	
	/**
	 * Export preference list to XML document
	 * 
	 * @param preferenceList
	 * @return
	 * @throws Exception
	 */
	public Document export(List<Preference> preferenceList) throws Exception {
		Document xmlDoc = XmlUtils.newDocument();

		Element xmlRootNode = XmlUtils.createElement(xmlDoc, TAG_PREFERENCES);
		xmlDoc.appendChild(xmlRootNode);
		for (Preference preference : preferenceList) {
			xmlFromPreference(xmlRootNode, preference);
		}

		return xmlDoc;
	}

	/**
	 * Export single preference to XML document
	 * 
	 * @param preference
	 * @return
	 * @throws Exception
	 */
	public Document export(Preference preference) throws Exception {
		Document xmlDoc = XmlUtils.newDocument();

		Element xmlRootNode = XmlUtils.createElement(xmlDoc, TAG_PREFERENCES);
		xmlDoc.appendChild(xmlRootNode);

		xmlFromPreference(xmlRootNode, preference);

		return xmlDoc;
	}

	protected Element xmlFromPreference(Element xmlParentNode, Preference preference) throws Exception {
		Element xmlPreferenceNode = XmlUtils.createSubElement(xmlParentNode, TAG_PREFERENCE);
		copyAttributeToXml(preference, xmlPreferenceNode);

		String defValue = preference.getDefinitionValue();
		if (defValue != null && !defValue.equals("")) {
			Document xmlSubDoc = XmlUtils.createDocument(preference.getDefinitionValue());
			Element xmlSubDocRootNode = xmlSubDoc.getDocumentElement();
			copyAttributeToXml(preference, xmlSubDocRootNode);
			if (xmlSubDocRootNode != null) {
				Element xmlCopiedSubDocRootNode = xmlPreferenceNode;
				if (!xmlSubDocRootNode.getTagName().equals(TAG_PREFERENCE)) {
					// when the root tag name is not equal to the preference tag, create sub node!
					xmlCopiedSubDocRootNode = XmlUtils.createSubElement(xmlPreferenceNode,
					        xmlSubDocRootNode.getTagName());
				}
				XmlUtils.copyNode(xmlSubDocRootNode, xmlCopiedSubDocRootNode);
			}
		}

		return xmlPreferenceNode;
	}

	protected void copyAttributeToXml(Preference preference, Element xmlPreferenceNode) {
		xmlPreferenceNode.setAttribute(ATTRIBUTE_NAME, preference.getName());
		xmlPreferenceNode.setAttribute(ATTRIBUTE_ENTITY, preference.getEntity());
		xmlPreferenceNode.setAttribute(ATTRIBUTE_MODULE, preference.getModule());
		xmlPreferenceNode.setAttribute(ATTRIBUTE_SCOPE, preference.getScope().toString());
		xmlPreferenceNode.setAttribute(ATTRIBUTE_TYPE, preference.getType());
	}

	protected void copyAttributeFromXml(Element xmlPreferenceNode, Preference preference) {
		preference.setName(xmlPreferenceNode.getAttribute(ATTRIBUTE_NAME));
		preference.setEntity(xmlPreferenceNode.getAttribute(ATTRIBUTE_ENTITY));
		preference.setModule(xmlPreferenceNode.getAttribute(ATTRIBUTE_MODULE));
		preference.setType(xmlPreferenceNode.getAttribute(ATTRIBUTE_TYPE));
		preference.setScope(ValidityScope.fromString(xmlPreferenceNode.getAttribute(ATTRIBUTE_SCOPE)));
	}

	public static void registerConcretePreferenceLoader(String preferenceType, Class<? extends PreferenceConverter<? extends Preference>> converter) {
		concretePreferenceConverterMap.put(preferenceType, converter);
	}

	public static final PreferenceConverter<? extends Preference> newConcretePreferenceLoader(String preferenceType) {
		Class<? extends PreferenceConverter<? extends Preference>> prefConverterClass = concretePreferenceConverterMap.get(preferenceType);
		if (prefConverterClass == null) return null;
		
		try {
	        return prefConverterClass.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
	        log.error("Fail to instantiate concrete preference converter for preference type: '" + preferenceType + "'", e);
        }
		
		return null;
	}
}
