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

import java.util.ArrayList;
import java.util.List;

import org.jrtech.common.xmlutils.XmlUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public abstract class AbstractPreferenceLoaderAndConverter<T extends Preference> extends PreferenceLoader
        implements PreferenceConverter<T> {

    private static Logger log = LoggerFactory.getLogger(AbstractPreferenceLoaderAndConverter.class);

    protected abstract T newInstance();

    protected abstract void copySpecificAttributeFromXml(Element xmlPreferenceNode, T preference);

    protected abstract void copySpecificAttributeToXml(T preference, Element xmlPreferenceNode);

    protected String getType() {
        T dummyInstance = newInstance();

        if (dummyInstance == null) {
            return null;
        }

        return dummyInstance.getClass().getSimpleName();
    }

    @Override
    protected List<Element> getPreferenceNodeList(Document xmlDoc) throws Exception {
        return super.getPreferenceNodeList(xmlDoc, getType());
    }

    public T loadSingleConfig(String xmlString) throws Exception {
        Document xmlDoc = XmlUtils.createDocument(xmlString);
        return loadSingleConfig(xmlDoc);
    }

    public T loadSingleConfig(Document xmlDoc) throws Exception {
        List<Element> nodeList = getPreferenceNodeList(xmlDoc, getType());

        if (nodeList.size() < 1)
            return null;

        T pref = xmlToPreference((Element) nodeList.get(0));
        // pref.setDefinitionValue(XmlUtils.documentToString(xmlDoc));

        return pref;
    }

    @SuppressWarnings("unchecked")
    @Override
    public T convert(Preference preference) throws Exception {
        if (preference == null)
            return null;

        try {
            if (getType().equals(preference.getClass().getSimpleName()))
                return (T) preference;
        } catch (ClassCastException e) {
            if (!preference.getType().equals(getType())) {
                throw new ClassCastException("The provided preference is not from " + getType());
            }
        }

        // trying to parse the detail value.
        T result = loadSingleConfig(preference.getDefinitionValue());
        result.setId(preference.getId());
        return result;
    }

    @SuppressWarnings("unchecked")
    @Override
    protected Element xmlFromPreference(Element xmlParentNode, Preference preference) throws Exception {
        if (preference == null || xmlParentNode == null) {
            return null;
        }

        T dummyConcretePreference = newInstance();
        if (dummyConcretePreference == null) {
            return null;
        }
        T concretePreference = null;
        if (dummyConcretePreference.getClass().isInstance(preference)) {
            concretePreference = (T) preference;
        } else {
            concretePreference = convert(preference);
        }
        if (concretePreference == null) {
            return null;
        }

        Element xmlPreferenceNode = xmlParentNode;

        if (!TAG_PREFERENCE.equals(xmlPreferenceNode.getTagName())) {
            xmlPreferenceNode = XmlUtils.createSubElement(xmlParentNode, TAG_PREFERENCE);
        }

        copyAttributeToXml((T) concretePreference, xmlPreferenceNode);

        return xmlPreferenceNode;
    }

    @Override
    protected T xmlToPreference(Element xmlPreferenceNode) throws Exception {
        if (!(xmlPreferenceNode.getTagName().equals(TAG_PREFERENCE)
                && xmlPreferenceNode.getAttribute(ATTRIBUTE_TYPE).equals(getType()))) {
            return null;
        }

        T preference = newInstance();

        String prefContent = XmlUtils.nodeToString(xmlPreferenceNode);
        preference.setDefinitionValue(prefContent);

        copyAttributeFromXml(xmlPreferenceNode, (T) preference);

        return preference;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.jrtech.common.preferences.PreferenceLoader#load(org.w3c.dom.Document)
     */
    @SuppressWarnings("unchecked")
    @Override
    public List<Preference> load(Document xmlDoc) throws Exception {
        return (List<Preference>) convert(super.load(xmlDoc, getType()));
    }

    public List<T> load(Document xmlDoc, String module, String entity) throws Exception {
        return convert(super.load(xmlDoc, getType(), module, entity));
    }

    public List<T> load(String xmlString, String module, String entity) throws Exception {
        return convert(super.load(xmlString, getType(), module, entity));
    }

    protected List<T> convert(List<Preference> preferenceList) {
        List<T> concretePrefList = new ArrayList<T>();
        for (int i = 0; i < preferenceList.size(); i++) {
            try {
                concretePrefList.add(convert(preferenceList.get(i)));
            } catch (Exception e) {
                log.warn(e.getMessage(), e);
            }
        }
        return concretePrefList;
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void copyAttributeFromXml(Element xmlPreferenceNode, Preference preference) {
        super.copyAttributeFromXml(xmlPreferenceNode, preference);

        copySpecificAttributeFromXml(xmlPreferenceNode, (T) preference);
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void copyAttributeToXml(Preference preference, Element xmlPreferenceNode) {
        super.copyAttributeToXml(preference, xmlPreferenceNode);

        copySpecificAttributeToXml((T) preference, xmlPreferenceNode);
    }

    /**
     * Synchronize definition from
     * 
     * @param model
     * @return
     * @throws Exception
     */
    public T synchronizeDefinitionFromObject(T model) throws Exception {
        Document xmlDoc = export(model);

        Element xmlPrefNode = null;

        if (xmlDoc.getDocumentElement().getTagName().equals(TAG_PREFERENCE)
                && getType().equals(xmlDoc.getDocumentElement().getAttribute(ATTRIBUTE_TYPE))) {
            xmlPrefNode = xmlDoc.getDocumentElement();
        } else if (xmlDoc.getDocumentElement().getTagName().equals(TAG_PREFERENCES)) {
            xmlPrefNode = XmlUtils.getChildByTagNameAndAttributeValues(xmlDoc.getDocumentElement(), TAG_PREFERENCE,
                    new String[] { ATTRIBUTE_TYPE }, new String[] { getType() });
        }

        copyAttributeToXml(model, xmlPrefNode);

        model.setDefinitionValue(XmlUtils.nodeToString(xmlPrefNode));
        return model;
    }

}
