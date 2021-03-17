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
package org.jrtech.common.xsutils.labels;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.StringUtils;
//import org.jrtech.common.ehcache.CacheManagerFactory;
//import org.jrtech.common.ehcache.CacheOperationHelper;
import org.jrtech.common.utils.ResourceLocatorUtil;
import org.jrtech.common.xsutils.CacheUtil;
import org.jrtech.common.xsutils.JrxXmlModelUtil;
import org.jrtech.common.xsutils.XsdToXmlUtil;
import org.jrtech.common.xsutils.model.JrxChoiceGroup;
import org.jrtech.common.xsutils.model.JrxDeclaration;
import org.jrtech.common.xsutils.model.JrxElement;
import org.jrtech.common.xsutils.model.JrxElementGroup;
import org.jrtech.common.xsutils.model.JrxGroup;
import org.jrtech.common.xsutils.model.JrxTerm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.cache.LoadingCache;
import com.sun.xml.xsom.XSDeclaration;
import com.sun.xml.xsom.XSParticle;
import com.sun.xml.xsom.XSTerm;
import com.sun.xml.xsom.XSType;

public class XmlTagLabelUtil implements Serializable {

    private static final long serialVersionUID = 4003846332920837832L;

    private static Logger log = LoggerFactory.getLogger(XmlTagLabelUtil.class);

    public static final String NAMESPACE_LABELS_CACHE_NAME = "jrx-msg-namespace-labels-cache";

    public static final String LABELS_RESOURCE_FILE_SUFFIX = ".properties";

    public static final String ELEMENT_PREFIX = "element.";

    public static final String TYPE_PREFIX = "type.";

    public static final String CHOICE_ITEM_SEPARATOR = "or";

    public static final String LABEL_SEGMENT_SEPARATOR = ".";

    public static final String TAG_LABEL_PREFIX = "tag" + LABEL_SEGMENT_SEPARATOR;

    private static LoadingCache<String, Map<String, String>> namespaceLabelsCache = null;

    protected static ConcurrentMap<String, String> labelsResourcePathMap = new ConcurrentHashMap<String, String>();

    protected TagLabelResourceLocationInfo[] schemaNamespacePrefixes;

    public XmlTagLabelUtil(TagLabelResourceLocationInfo[] schemaNamespacePrefixes) {
        if (schemaNamespacePrefixes == null) {
            this.schemaNamespacePrefixes = new TagLabelResourceLocationInfo[] {};
        } else {
            this.schemaNamespacePrefixes = schemaNamespacePrefixes;
        }
    }

    /**
     * Get tag label for a term.<br>
     * 
     * @param jrxTerm
     *            term to be resolved.
     * @return string value for tag label.
     */
    public String getTagLabel(JrxTerm<?> jrxTerm) {
        if (jrxTerm instanceof JrxElementGroup) {
            JrxElementGroup jrxElementGroup = (JrxElementGroup) jrxTerm;
            if (jrxElementGroup.getParentBlock() != null) {
                if (jrxElementGroup.isChoiceGroup()) {
                    XSParticle[] xsChoiceParticleArray = XsdToXmlUtil.getInstance().getChoiceElementParticle(
                            jrxElementGroup.getXsdDeclaration());

                    StringBuffer sb = new StringBuffer();
                    for (int i = 0; i < xsChoiceParticleArray.length; i++) {
                        XSParticle xsChoiceParticle = xsChoiceParticleArray[i];
                        if (i > 0) {
                            if (!CHOICE_ITEM_SEPARATOR.startsWith(" "))
                                sb.append(" ");
                            sb.append(CHOICE_ITEM_SEPARATOR.trim());
                            if (!CHOICE_ITEM_SEPARATOR.endsWith(" "))
                                sb.append(" ");
                        }
                        XSTerm xsChoiceTerm = xsChoiceParticle.getTerm();
                        sb.append(xsChoiceTerm.asElementDecl().getName());
                    }

                    return sb.length() > 0 ? sb.toString() : jrxElementGroup.getCompositor().name();
                }

                jrxElementGroup = jrxElementGroup.getParentBlock();
                return getTagLabel(jrxElementGroup);
            }

            JrxElement jrxParentElement = null;
            if (jrxElementGroup.getOwner() instanceof JrxElement) {
                jrxParentElement = (JrxElement) jrxElementGroup.getOwner();
            }
            if (jrxParentElement == null)
                return jrxElementGroup.getOwner().getName();

            return getTagLabel(jrxParentElement.getNamespaceUri(), jrxElementGroup.getOwner().getName(),
                    jrxElementGroup.getOwner().getName());
        } else if (jrxTerm instanceof JrxGroup) {
            JrxGroup jrxGroup = (JrxGroup) jrxTerm;
            String groupName = jrxGroup.getName();
            JrxElement jrxParentElement = jrxGroup.getParentElement();

            if (jrxParentElement == null)
                return groupName;

            return getTagLabel(jrxParentElement.getNamespaceUri(), groupName, groupName);
        }

        JrxElement jrxElement = (JrxElement) jrxTerm;

        // produce full-scope name from the 0-3 parents
        String fullScopedName = "";
        int i = 0;
        int maxLoop = 2;
        JrxElement jrxCurrentPointer = jrxElement;
        while (true) {
            if (i >= maxLoop || jrxCurrentPointer == null) {
                break;
            }

            if (fullScopedName.equals("")) {
                fullScopedName = jrxCurrentPointer.getSimpleName();
            } else {
                fullScopedName = jrxCurrentPointer.getSimpleName() + LABEL_SEGMENT_SEPARATOR + fullScopedName;
            }

            if (jrxCurrentPointer.getParentBlock() == null) {
                break;
            }

            JrxElement jrxEffectiveParentElement = JrxXmlModelUtil.getParentElement(jrxCurrentPointer.getParentBlock());

            if (jrxEffectiveParentElement == null) {
                break;
            }
            if (jrxEffectiveParentElement.getNamespaceUri() == null || jrxCurrentPointer.getNamespaceUri() == null) {
                break;
            }

            if (!jrxEffectiveParentElement.getNamespaceUri().equals(jrxCurrentPointer.getNamespaceUri())) {
                break;
            }

            jrxCurrentPointer = jrxEffectiveParentElement;

            i++;
        }

        String label = getTagLabel(jrxElement.getNamespaceUri(), ELEMENT_PREFIX + fullScopedName, "");

        if (label.equals("")) {
            // try retrieve from it's complex type if any
            if (jrxElement.getXsdDeclaration() != null) {
                XSType xsType = jrxElement.getXsdDeclaration().getType();
                if (xsType != null) {
                    label = getTagLabel(jrxElement.getNamespaceUri(), TYPE_PREFIX + xsType.getName(), "");
                }
            } else {
                // Try with the XML node name
                String possibleTypeName = jrxElement.getName().substring(0, 1).toUpperCase()
                        + jrxElement.getName().substring(1);
                label = getTagLabel(jrxElement.getNamespaceUri(), TYPE_PREFIX + possibleTypeName, "");
            }
        }

        if (label.equals("")) {
            label = getTagLabel(jrxElement.getNamespaceUri(), ELEMENT_PREFIX + jrxElement.getSimpleName(), "");
        }

        if (label.equals("")) {
            // just use the XML tag name
            label = jrxElement.getSimpleName();
        }

        return label;
    }

    public String getChoiceSelectionTagLabel(JrxChoiceGroup jrxChoiceGroup, String selectionTagName) {
        String label = null;
        if (jrxChoiceGroup != null && selectionTagName != null) {
            JrxDeclaration<?> jrxParentDeclaration = JrxXmlModelUtil.getParentDeclaration(jrxChoiceGroup);
            if (jrxParentDeclaration != null) {
                String targetNamespaceUri = ((XSDeclaration) jrxParentDeclaration.getXsdDeclaration())
                        .getTargetNamespace();
                label = getTagLabel(
                        targetNamespaceUri,
                        ELEMENT_PREFIX
                                + (jrxParentDeclaration instanceof JrxElement ? ((JrxElement) jrxParentDeclaration)
                                        .getSimpleName() : jrxParentDeclaration.getName()) + LABEL_SEGMENT_SEPARATOR
                                + selectionTagName, "");
                if (label.equals("")) {
                    label = getTagLabel(targetNamespaceUri, ELEMENT_PREFIX + selectionTagName, "");
                }
            }

            if (label.equals(""))
                label = selectionTagName;
        }

        return label;
    }

    /**
     * Get tag label of the element within the catalog according to the given namespace URI. <br>
     * 
     * @param namespaceUri
     *            identity of label catalog shall be use for search.
     * @param scopedElementName
     *            scoped name of the tag name.
     * @param defaultLabel
     *            default label to be used when there is nothing found.
     * @return string value for tag label.
     */
    public String getTagLabel(String namespaceUri, String scopedElementName, String defaultLabel) {
        if (namespaceUri == null || namespaceUri.equals("") || scopedElementName == null
                || scopedElementName.equals(""))
            return defaultLabel;

        Map<String, String> labelsCatalog = getNamespaceLabelCatalog(namespaceUri);

        if (labelsCatalog == null) {
            // load catalog
            labelsCatalog = loadCatalogByNamespaceUri(namespaceUri);
        }

        if (labelsCatalog == null) {
            log.info("Resource for namespaceUri: '" + namespaceUri + "' is not available or provided.");
            return defaultLabel;
        }

        String foundLabel = labelsCatalog.get(scopedElementName);
        if (foundLabel == null)
            return defaultLabel;

        return foundLabel;
    }

    protected Map<String, String> loadCatalogByNamespaceUri(String namespaceUri) {
        String prefix = "";
        int prefixLength = 0;
        for (int i = 0; i < schemaNamespacePrefixes.length; i++) {
            if (namespaceUri.startsWith((String) schemaNamespacePrefixes[i].getNamespaceUriPrefix())) {
                prefix = (String) schemaNamespacePrefixes[i].getNamespaceUriPrefix();
                prefixLength = (Integer) schemaNamespacePrefixes[i].getResourceBeginIndex();
                break;
            }
        }

        if (prefix.equals("")) {
            log.info("Could not match prefix for namespace URI: '" + namespaceUri
                    + "'. Current SCHEMA_NAMESPACE_PREFIX is: " + Arrays.toString(schemaNamespacePrefixes));
            return null;
        }

        Properties props = new Properties();
        ResourceLocatorUtil locUtil = new ResourceLocatorUtil();

        // Try loading specific label resource (e.g. pacs.008.001.01.properties)
        InputStream is = getLabelResource(formulateLabelsResourceLocation(namespaceUri, prefixLength), locUtil);

        if (is != null) {
            try {
                props.load(new InputStreamReader(is, StandardCharsets.UTF_8));
            } catch (IOException e) {
                log.info("Cannot load property file for namespace URI: '" + namespaceUri + "'", e);
            } finally {
                if (is != null) {
                    try {
                        is.close();
                    } catch (IOException e) {
                    }
                }
            }
        }

        Map<String, String> labelsCatalog = new Hashtable<String, String>();
        for (Entry<Object, Object> entry : props.entrySet()) {
            labelsCatalog.put((String) entry.getKey(), (String) entry.getValue());
        }
        registerNamespaceLabelCatalog(namespaceUri, labelsCatalog);

        return labelsCatalog;
    }

    protected InputStream getLabelResource(String labelsResourceLocation, ResourceLocatorUtil locUtil) {
        InputStream is = null;
        try {
            URL labelsResourceUrl = null;
            try {
                labelsResourceUrl = locUtil.resolveUrlFromLocationString(labelsResourceLocation);
            } catch (Exception e) {
                labelsResourceUrl = getClass().getResource(labelsResourceLocation);
            }

            is = labelsResourceUrl.openStream();
            return is;
        } catch (Exception e) {
            log.info("Could not load labels resource from location: '" + labelsResourceLocation + "': "
                    + e.getMessage());
        }
        return null;
    }

    protected String formulateLabelsResourceLocation(String namespaceUri, int prefixLength) {
        String labelsResourceName = namespaceUri.substring(prefixLength);
        int lastDoubleColonIndex = labelsResourceName.lastIndexOf(":");
        String packageName = labelsResourcePathMap.get(labelsResourceName.substring(0, lastDoubleColonIndex));
        packageName = StringUtils.replace(packageName, "$", "");
        labelsResourceName = StringUtils.replace(
                labelsResourceName.substring(lastDoubleColonIndex + 1).replace(':', '.'), "$", "")
                + LABELS_RESOURCE_FILE_SUFFIX;

        return packageName + "/" + labelsResourceName;
    }

    /**
     * Check the availability of a label catalog for the given namespace URI in the cache. <br>
     * 
     * @param namespaceUri
     * @return <li><b>true</b> if label catalog exists for the provided namespace URI</li><br>
     *         <li><b>false</b> if label catalog does not exist for the provided namespace URI</li>
     */
    public static boolean hasNamespaceLabelCatalog(String namespaceUri) {
    	return getNamespaceLabelsCache().asMap().containsKey(namespaceUri);
    }

    /**
     * Register a label catalog to the cache with the given namespace URI as key. <br>
     * 
     * @param namespaceUri
     *            identity of label catalog shall be use for search.
     * @param labelCatalog
     *            label catalog to store.
     */
    public static void registerNamespaceLabelCatalog(String namespaceUri, Map<String, String> labelCatalog) {
    	getNamespaceLabelsCache().put(namespaceUri, labelCatalog);
    }

    /**
     * Retrieve label catalog according to a namespace URI.
     * 
     * @param namespaceUri
     *            identity of label catalog shall be use for search.
     * @return
     */
    protected Map<String, String> getNamespaceLabelCatalog(String namespaceUri) {
        return getNamespaceLabelsCache().getIfPresent(namespaceUri);
    }

    protected static LoadingCache<String, Map<String, String>> getNamespaceLabelsCache() {
        if (namespaceLabelsCache != null)
            return namespaceLabelsCache;

        namespaceLabelsCache = createCache();

        return namespaceLabelsCache;
    }

    /**
     * @param relevantApplicationNamespaceUri
     * @return
     */
    public static String getLabelsResourcePath(String relevantApplicationNamespaceUri) {
        return labelsResourcePathMap.get(relevantApplicationNamespaceUri);
    }

    /**
     * @param relevantApplicationNamespaceUri
     * @param path
     */
    public static void addLabelsResourcePath(String relevantApplicationNamespaceUri, String path) {
        synchronized (labelsResourcePathMap) {
            labelsResourcePathMap.put(relevantApplicationNamespaceUri, path);
        }
    }

    /**
     * @param relevantApplicationNamespaceUri
     */
    public static void removeLabelsResourcePath(String relevantApplicationNamespaceUri) {
        synchronized (labelsResourcePathMap) {
            labelsResourcePathMap.remove(relevantApplicationNamespaceUri);
        }
    }

    public static Map<String, String> loadCatalogFromResource(String propertiesFileUrlString) throws IOException {
        ResourceLocatorUtil locUtil = new ResourceLocatorUtil();

        return loadCatalogFromResource(locUtil.resolveUrlFromLocationString(propertiesFileUrlString));
    }

    public static Map<String, String> loadCatalogFromResource(URL propertiesFileUrl) throws IOException {
        if (propertiesFileUrl == null) {
            throw new IOException("Invalid URL: [NULL]");
        }

        Properties props = new Properties();

        InputStream is = null;
        try {
            is = propertiesFileUrl.openStream();
        } catch (IOException e) {
            throw new IOException("Invalid URL: '" + propertiesFileUrl + "'");
        }
        try {
            props.load(is);
        } catch (IOException e) {
            throw new IOException("Cannot load property file: '" + propertiesFileUrl + "'", e);
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                }
            }
        }

        Map<String, String> labelsCatalog = new Hashtable<String, String>();
        for (Entry<Object, Object> entry : props.entrySet()) {
            labelsCatalog.put((String) entry.getKey(), (String) entry.getValue());
        }

        return labelsCatalog;
    }
    
	private static LoadingCache<String, Map<String, String>> createCache() {
		return CacheUtil.createCache(10000, 300, TimeUnit.SECONDS);
	}
}
