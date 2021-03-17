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

import java.util.ArrayList;
import java.util.List;

import org.jrtech.common.preferences.GlobalPreferenceCatalog;
import org.jrtech.common.preferences.Preference;
import org.jrtech.common.preferences.PreferenceService;
import org.jrtech.common.utils.query.AttributeOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TableConfigurationUtil {

    private static Logger log = LoggerFactory.getLogger(TableConfigurationUtil.class);

    public static final String DEFAULT_NAME = "Default";

    private PreferenceService preferenceService;

    public TableConfigurationUtil(PreferenceService preferenceService) {
        this.preferenceService = preferenceService;
    }

    public static TableConfigurationUtil getInstance(PreferenceService preferenceService) {
        return new TableConfigurationUtil(preferenceService);
    }

    public String loadSystemTableConfiguration(String module, String entity) {
        return loadSystemTableConfiguration(module, entity, DEFAULT_NAME);
    }

    public String loadSystemTableConfiguration(String module, String entity, String name) {
        String sysConfig = "";
        try {
            Preference foundPref = GlobalPreferenceCatalog.retrieveGlobalPreference(
                    TableConfiguration.class, module, entity, name);
            if (foundPref != null)
                sysConfig = convertToTableConfiguration(foundPref);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return sysConfig;
    }

    public String loadOrganizationUnitTableConfiguration(String orgUnitId, String module, String entity) {
        return loadOrganizationUnitTableConfiguration(orgUnitId, module, entity, DEFAULT_NAME);
    }

    public String loadOrganizationUnitTableConfiguration(String orgUnitId, String module, String entity,
            String name) {
        String orgUnitConfig = "";
        try {
            // Load preferences from back-end service.
            Preference orgPref = preferenceService.retrieveOrganizationPreference(orgUnitId,
                    TableConfiguration.class.getSimpleName(), module, entity, name);

            orgUnitConfig = convertToTableConfiguration(orgPref);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return orgUnitConfig;
    }

    public String loadUserTableConfiguration(String module, String entity) {
        return loadUserTableConfiguration(module, entity, DEFAULT_NAME);
    }

    public String loadUserTableConfiguration(String module, String entity, String name) {
        String config = "";
        try {
            // Load preferences from back-end service.
            Preference userPref = preferenceService.retrieveUserPreference(
                    TableConfiguration.class.getSimpleName(), module, entity, name);

            config = convertToTableConfiguration(userPref);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return config;
    }

    public void saveUserTableConfiguration(TableConfiguration config) {
        try {
            // Check existing filter with same name
            Preference existingPref = preferenceService.retrieveUserPreference(
                    TableConfiguration.class.getSimpleName(), config.getModule(), config.getEntity(),
                    config.getName());

            TableConfigurationLoader loader = new TableConfigurationLoader();
            config = loader.synchronizeDefinitionFromObject(config);
            if (existingPref == null) {
                preferenceService.createUserPreference(config);
                log.debug("Add new table configuration: " + config.getLogicalUniqueIdentification());
            } else {
                config.setId(existingPref.getId());
                preferenceService.modifyUserPreference(config);
                log.debug("Replace table configuration: " + config.getLogicalUniqueIdentification());
            }

        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    public static String convertToTableConfiguration(List<Preference> prefList) throws Exception {
        if (prefList != null && prefList.size() > 0) {
            return convertToTableConfiguration(prefList.get(0));
        }

        return "";
    }

    public static String convertToTableConfiguration(Preference preference) throws Exception {
        if (preference != null) {
            // trim-out envelop of the configuration
            TableConfigurationLoader resultConfigLoader = new TableConfigurationLoader();
            TableConfiguration ssrConfig = resultConfigLoader.convert(preference);
            if (ssrConfig != null) {
                ssrConfig.synchronizeObjectToDefinitionValue();
                return ssrConfig.getDefinitionValue();
            }
        }

        return "";
    }

    public static List<AttributeOrder> convertColumnSortOrder(List<TableColumnSortOrder> columnSortOrderList) {
        List<AttributeOrder> attributeOrderList = new ArrayList<AttributeOrder>();

        if (columnSortOrderList != null) {
            for (TableColumnSortOrder columnSortOrder : columnSortOrderList) {
                String sortColumnName = columnSortOrder.getColumn().getSortColumnName();
                if (sortColumnName == null || sortColumnName.equals(""))
                    sortColumnName = columnSortOrder.getColumn().getName();
                String[] sortColumns = sortColumnName.split(",");
                for (String column : sortColumns) {
                    AttributeOrder ao = new AttributeOrder(column, columnSortOrder.getOrder());
                    attributeOrderList.add(ao);
                }
            }
        }

        return attributeOrderList;
    }

	public static TableColumnConfiguration cloneTableColumnConfiguration(TableColumnConfiguration original) {
	    TableColumnConfiguration clone = new TableColumnConfiguration();
	    clone.setAlign(original.getAlign());
	    clone.setAllowed(original.isAllowed());
	    clone.setColumnFormat(original.getColumnFormat());
	    clone.setLabel(original.getLabel());
	    clone.setName(original.getName());
	    clone.setOwner(original.getOwner());
	    clone.setRelatedColumn(original.getRelatedColumn());
	    clone.setRetrievingMethod(original.getRetrievingMethod());
	    clone.setSeleniumLabel(original.getSeleniumLabel());
	    clone.setSortable(original.isSortable());
	    clone.setSortColumnName(original.getSortColumnName());
	    clone.setTranslator(original.getTranslator());
	    clone.setVisible(original.isVisible());
	    clone.setWidth(original.getWidth());
	    
	    return clone;
	}
}
