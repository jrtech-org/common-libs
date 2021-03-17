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
package org.jrtech.common.preferences.table;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jrtech.common.preferences.AbstractPreferenceLoaderAndConverter;
import org.jrtech.common.preferences.Preference;
import org.jrtech.common.preferences.PreferenceLoader;
import org.jrtech.common.utils.query.AttributeOrder.SortOrder;
import org.jrtech.common.xmlutils.XmlUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class TableConfigurationLoader extends AbstractPreferenceLoaderAndConverter<TableConfiguration> {

    public static final String NAMESPACE = "http://www.jrtech.org/common/config/table";

    public static final String TAG_TABLE = "table";
    public static final String TAG_OBJECT = "object";
    public static final String TAG_COLUMNS = "columns";
    public static final String TAG_COLUMN = "column";
    public static final String TAG_SORT_COLUMN_LIST = "sortColumnList";

    public static final String ATTRIBUTE_ALIGN = "align";
    public static final String ATTRIBUTE_ALLOWED = "allowed";
    public static final String ATTRIBUTE_FORMAT = "format";
    public static final String ATTRIBUTE_LABEL = "label";
    public static final String ATTRIBUTE_LAST = "lastUserColumnName";
    public static final String ATTRIBUTE_METHOD_PATH = "methodPath";
    public static final String ATTRIBUTE_OWNER = "ownerColumnName";
    public static final String ATTRIBUTE_RETRIEVING_METHOD = "retrievingMethod";
    public static final String ATTRIBUTE_SELENIUM = "selenium";
    public static final String ATTRIBUTE_SORTABLE = "sortable";
    public static final String ATTRIBUTE_SORT_COLUMN_NAME = "sortColumnName";
    public static final String ATTRIBUTE_SORT_DIRECTION = "sortDirection";
    public static final String ATTRIBUTE_TRANSLATOR = "translator";
    public static final String ATTRIBUTE_VISIBLE = "visible";
    public static final String ATTRIBUTE_WIDTH = "width";
    public static final String ATTRIBUTE_RELATED_COLUMN = "relatedColumn";
    public static final String ATTRIBUTE_TOOLTIP = "tooltip";

    static {
        PreferenceLoader.registerConcretePreferenceLoader(TableConfiguration.class.getSimpleName(),
                TableConfigurationLoader.class);
    }

    protected String getType() {
        return TableConfiguration.class.getSimpleName();
    }

    @Override
    protected List<Element> getPreferenceNodeList(Document xmlDoc) throws Exception {
        return getPreferenceNodeList(xmlDoc, getType());
    }

    protected Element xmlFromTableConfiguration(Element xmlPreferenceNode, TableConfiguration preference) {
        Element xmlTableNode = XmlUtils.createSubElement(xmlPreferenceNode, TAG_TABLE);
        xmlTableNode.setAttribute("xmlns", NAMESPACE);

        Element xmlObjectNode = XmlUtils.createSubElement(xmlTableNode, TAG_OBJECT);
        xmlObjectNode.setTextContent(preference.getObject());

        Element xmlColumnsNode = XmlUtils.createSubElement(xmlTableNode, TAG_COLUMNS);
        for (TableColumnConfiguration column : preference.getColumns()) {
            xmlFromTableColumnConfiguration(xmlColumnsNode, column);
        }

        // Sorting definition
        Element xmlSortColumnListNode = XmlUtils.createSubElement(xmlTableNode, TAG_SORT_COLUMN_LIST);
        for (TableColumnSortOrder columnSortOrder : preference.getColumnSortOrderList()) {
            if (columnSortOrder.getColumn() == null || columnSortOrder.getColumn().getName() == null
                    || columnSortOrder.getColumn().getName().equals(""))
                continue;

            Element xmlSortColumnNode = XmlUtils.createSubElement(xmlSortColumnListNode, TAG_COLUMN);
            xmlSortColumnNode.setAttribute(ATTRIBUTE_NAME, columnSortOrder.getColumn().getName());
            xmlSortColumnNode.setAttribute(ATTRIBUTE_SORT_DIRECTION, columnSortOrder.getOrder().toCode());
        }
        
        return xmlPreferenceNode;
    }

    protected TableConfiguration xmlToTableConfiguration(Element xmlPreferenceNode, TableConfiguration preference) throws Exception {
        Element xmlTableNode = XmlUtils.getChildByTagName(xmlPreferenceNode, TAG_TABLE);

        if (xmlTableNode == null) {
            // Preference does not contain table configuration.
            return null; 
        }

        Element xmlObjectNode = XmlUtils.getChildByTagName(xmlTableNode, TAG_OBJECT);
        preference.setObject(xmlObjectNode.getTextContent());

        Map<String, TableColumnConfiguration> columnConfigMap = new HashMap<String, TableColumnConfiguration>();
        Element xmlColumnsNode = XmlUtils.getChildByTagName(xmlTableNode, TAG_COLUMNS);
        if (xmlColumnsNode != null) {
            List<Element> xmlColumnNodeList = XmlUtils.getChildElementListByTagName(xmlColumnsNode, TAG_COLUMN);
            if (xmlColumnNodeList != null) {
                for (Element xmlColumnNode : xmlColumnNodeList) {
                    TableColumnConfiguration columnConfig = xmlToTableColumnConfiguration(xmlColumnNode, preference);
                    preference.getColumns().add(columnConfig);
                    columnConfigMap.put(columnConfig.getName(), columnConfig);
                }
            }
        }

        // Sorting definition
        Element xmlSortColumnListNode = XmlUtils.getChildByTagName(xmlTableNode, TAG_SORT_COLUMN_LIST);
        if (xmlSortColumnListNode != null) {
            List<Element> xmlSortColumnNodeList = XmlUtils.getChildElementListByTagName(xmlSortColumnListNode, TAG_COLUMN);
            if (xmlSortColumnNodeList != null) {
                for (Element xmlSortColumnNode : xmlSortColumnNodeList) {
                    String relatedColumnName = xmlSortColumnNode.getAttribute(ATTRIBUTE_NAME);

                    TableColumnConfiguration columnConfig = columnConfigMap.get(relatedColumnName);
                    if (columnConfig == null)
                        continue; // skip incorrect reference

                    TableColumnSortOrder columnSortOrder = new TableColumnSortOrder();
                    columnSortOrder.setColumn(columnConfig);

                    String sortOrderString = xmlSortColumnNode.getAttribute(ATTRIBUTE_SORT_DIRECTION);
                    columnSortOrder.setOrder(SortOrder.fromCode(sortOrderString, SortOrder.ASCENDING));

                    preference.getColumnSortOrderList().add(columnSortOrder);
                }
            }
        }
        
        return preference;
    }

    public Document export(TableConfiguration preference) throws Exception {
        Document xmlDoc = XmlUtils.newDocument();

        Element xmlRootNode = XmlUtils.createElement(xmlDoc, TAG_PREFERENCES);
        xmlDoc.appendChild(xmlRootNode);

        xmlFromPreference(xmlRootNode, preference);

        return xmlDoc;
    }

    protected Element xmlFromTableColumnConfiguration(Element xmlParentNode, TableColumnConfiguration column) {
        Element xmlColumnNode = XmlUtils.createSubElement(xmlParentNode, TAG_COLUMN);

        xmlColumnNode.setAttribute(ATTRIBUTE_ALIGN, column.getAlign().name().toLowerCase());
        xmlColumnNode.setAttribute(ATTRIBUTE_ALLOWED, "" + column.isAllowed());
        xmlColumnNode.setAttribute(ATTRIBUTE_FORMAT, column.getColumnFormat());
        xmlColumnNode.setAttribute(ATTRIBUTE_LABEL, column.getLabel());
        xmlColumnNode.setAttribute(ATTRIBUTE_NAME, column.getName());
        xmlColumnNode.setAttribute(ATTRIBUTE_RELATED_COLUMN, column.getRelatedColumn());
        xmlColumnNode.setAttribute(ATTRIBUTE_RETRIEVING_METHOD, column.getRetrievingMethod());
        if (column.getSortColumnName() != null && !column.getSortColumnName().equals("")) {
            xmlColumnNode.setAttribute(ATTRIBUTE_SORT_COLUMN_NAME, column.getSortColumnName());
        }
        xmlColumnNode.setAttribute(ATTRIBUTE_SORTABLE, "" + column.isSortable());
        xmlColumnNode.setAttribute(ATTRIBUTE_TRANSLATOR, column.getTranslator());
        xmlColumnNode.setAttribute(ATTRIBUTE_VISIBLE, "" + column.isVisible());
        xmlColumnNode.setAttribute(ATTRIBUTE_WIDTH, column.getWidth());

        return xmlColumnNode;
    }
    
    protected TableColumnConfiguration xmlToTableColumnConfiguration(Element xmlColumnNode, TableConfiguration owner) {
        TableColumnConfiguration columnConfig = new TableColumnConfiguration();
        columnConfig.setOwner(owner);

        try {
            String value = xmlColumnNode.getAttribute(ATTRIBUTE_SORTABLE);
            if (value.equals(""))
                columnConfig.setSortable(true);
            else
                columnConfig.setSortable(Boolean.parseBoolean(value));
        } catch (Exception e) {
            columnConfig.setSortable(true);
        }

        columnConfig.setName(xmlColumnNode.getAttribute(ATTRIBUTE_NAME));
        String label = xmlColumnNode.getAttribute(ATTRIBUTE_LABEL);
        if (!label.equals("")) {
            columnConfig.setLabel(label);
        }

        columnConfig.setVisible(Boolean.parseBoolean(xmlColumnNode.getAttribute(ATTRIBUTE_VISIBLE)));
        boolean allowed = !xmlColumnNode.getAttribute(ATTRIBUTE_ALLOWED).equals("")
                ? Boolean.parseBoolean(xmlColumnNode.getAttribute(ATTRIBUTE_ALLOWED)) : true;
        columnConfig.setAllowed(allowed);

        columnConfig.setRelatedColumn(xmlColumnNode.getAttribute(ATTRIBUTE_RELATED_COLUMN));
        columnConfig.setWidth(xmlColumnNode.getAttribute(ATTRIBUTE_WIDTH));
        columnConfig.setRetrievingMethod(xmlColumnNode.getAttribute(ATTRIBUTE_RETRIEVING_METHOD));

        columnConfig.setSortColumnName(xmlColumnNode.getAttribute(ATTRIBUTE_SORT_COLUMN_NAME));

        columnConfig.setColumnFormat(xmlColumnNode.getAttribute(ATTRIBUTE_FORMAT));
        columnConfig.setSeleniumLabel(xmlColumnNode.getAttribute(ATTRIBUTE_SELENIUM));
        columnConfig.setTranslator(xmlColumnNode.getAttribute(ATTRIBUTE_TRANSLATOR));
        columnConfig.setTooltip(xmlColumnNode.getAttribute(ATTRIBUTE_TOOLTIP));

        columnConfig.setAlign(TableColumnConfiguration.Alignment.fromString(xmlColumnNode.getAttribute(ATTRIBUTE_ALIGN)));
        
        return columnConfig;
    }

    @Override
    public TableConfiguration convert(Preference preference) throws Exception {
        if (preference == null) {
            return null;
        }
        if (!preference.getType().equals(getType())) {
            throw new ClassCastException("The provided preference is not for " + TableConfiguration.class.getName());
        }

        return loadSingleConfig(preference.getDefinitionValue());
    }

    public Preference convert(TableConfiguration configuration) throws Exception {
        if (configuration == null) {
            return null;
        }

        configuration = synchronizeDefinitionFromObject(configuration);
        Preference result = new Preference();
        result.setType(getType());
        result.setDefinitionValue(configuration.getDefinitionValue());
        result.setScope(configuration.getScope());
        result.setModule(configuration.getModule());
        result.setEntity(configuration.getEntity());
        result.setName(configuration.getName());
        result.setId(configuration.getId());

        return result;
    }

    protected Element xmlFromPreference(Element xmlParentNode, Preference preference) throws Exception {
        Element xmlPreferenceNode = super.xmlFromPreference(xmlParentNode, preference);

        if (xmlPreferenceNode == null) {
            return null;
        }

        return xmlFromTableConfiguration(xmlPreferenceNode, (TableConfiguration) preference);
    }

    @Override
    protected TableConfiguration xmlToPreference(Element xmlPreferenceNode) throws Exception {
        TableConfiguration preference = super.xmlToPreference(xmlPreferenceNode);

        if (preference == null) {
            return null;
        }

        return xmlToTableConfiguration(xmlPreferenceNode, preference);
    }

    @Override
    protected TableConfiguration newInstance() {
        return new TableConfiguration();
    }

    @Override
    protected void copySpecificAttributeFromXml(Element xmlPreferenceNode, TableConfiguration preference) {
        ((TableConfiguration) preference).setOwnerColumnName(xmlPreferenceNode.getAttribute(ATTRIBUTE_OWNER));
        ((TableConfiguration) preference).setLastUserColumnName(xmlPreferenceNode.getAttribute(ATTRIBUTE_LAST));
    }

    @Override
    protected void copySpecificAttributeToXml(TableConfiguration preference, Element xmlPreferenceNode) {
        xmlPreferenceNode.setAttribute(ATTRIBUTE_OWNER, ((TableConfiguration) preference).getOwnerColumnName());
        xmlPreferenceNode.setAttribute(ATTRIBUTE_LAST, ((TableConfiguration) preference).getLastUserColumnName());
    }

}
