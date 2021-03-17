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
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.jrtech.common.preferences.IncompatiblePreferenceTypeException;
import org.jrtech.common.preferences.Preference;
import org.jrtech.common.preferences.PreferenceLoader;

public class TableConfiguration extends Preference {

    private static final long serialVersionUID = -6749481334653180296L;

    private String ownerColumnName;
    private String lastUserColumnName;
    private List<TableColumnConfiguration> columns;
    private String object;
    private List<TableColumnSortOrder> sortOrderList;

    private Map<String, TableColumnConfiguration> columnMap;

    public TableConfiguration() {
        super();
        columns = new ArrayList<TableColumnConfiguration>();
        sortOrderList = new ArrayList<TableColumnSortOrder>();
        super.setType(getType());
    }

    public List<TableColumnConfiguration> getColumns() {
        return columns;
    }

    public void setColumns(List<TableColumnConfiguration> columns) {
        this.columns = columns;
        columnMap = new ConcurrentHashMap<String, TableColumnConfiguration>();
        if (columns != null) {
            for (TableColumnConfiguration column : columns) {
                columnMap.put(column.getName(), column);
            }
        }
    }

    public String getObject() {
        return object;
    }

    public void setObject(String object) {
        this.object = object;
    }

    public List<TableColumnSortOrder> getColumnSortOrderList() {
        return sortOrderList;
    }

    public String getOwnerColumnName() {
        return ownerColumnName;
    }

    public void setOwnerColumnName(String ownerPredicate) {
        this.ownerColumnName = ownerPredicate;
    }

    public String getLastUserColumnName() {
        return lastUserColumnName;
    }

    public void setLastUserColumnName(String lastUserColumnName) {
        this.lastUserColumnName = lastUserColumnName;
    }

    public TableColumnSortOrder getColumnSortOrder(String columnName) {
        TableColumnConfiguration theColumnConfig = null;

        for (TableColumnConfiguration columnConfig : columns) {
            if (columnConfig.getName().equals(columnName)) {
                theColumnConfig = columnConfig;
            }
        }

        if (theColumnConfig == null)
            return null;

        return getColumnSortOrder(theColumnConfig);
    }

    public TableColumnSortOrder getColumnSortOrder(TableColumnConfiguration columnConfig) {
        if (columnConfig == null) {
            return null;
        }

        TableColumnSortOrder columnSortOrder = new TableColumnSortOrder();
        columnSortOrder.setColumn(columnConfig);

        int i = sortOrderList.indexOf(columnSortOrder);

        if (i < 0) {
            return null;
        }
        return sortOrderList.get(i);
    }

    public TableColumnSortOrder addColumnSortOrder(TableColumnConfiguration columnconfig) {
        TableColumnSortOrder columnSortOrder = getColumnSortOrder(columnconfig);

        if (columnSortOrder == null) {
            // new column to sort
            columnSortOrder = new TableColumnSortOrder();
            columnSortOrder.setColumn(columnconfig);
        } else {
            int index = sortOrderList.indexOf(columnSortOrder);
            sortOrderList.remove(columnSortOrder);
            if (index == 0)
                columnSortOrder.switchSortOrder();
        }
        sortOrderList.add(0, columnSortOrder);

        return columnSortOrder;
    }

    public String getColumnSortOrderListString() {
        StringBuffer sb = new StringBuffer();

        for (int i = 0; i < sortOrderList.size(); i++) {
            TableColumnSortOrder columnSortOrder = sortOrderList.get(i);

            if (i > 0)
                sb.append(", ");
            sb.append(columnSortOrder.getColumn().getName()).append(" ")
                    .append(columnSortOrder.getOrder().toCode().toLowerCase());
        }

        return sb.toString();
    }

    public void resetSortOrder() {
        sortOrderList.clear();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int totalHashCode = 1;

        totalHashCode = prime * totalHashCode + ((object == null) ? 0 : object.hashCode());
        totalHashCode = prime * totalHashCode + ((columns == null) ? 0 : columns.hashCode());
        totalHashCode = prime * totalHashCode + ((sortOrderList == null) ? 0 : sortOrderList.hashCode());

        return totalHashCode;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;

        if (!(obj instanceof TableConfiguration))
            return false;

        final TableConfiguration other = (TableConfiguration) obj;
        if (object == null) {
            if (other.object != null)
                return false;
        } else if (!object.equals(other.object)) {
            return false;
        }
        if (columns == null) {
            if (other.columns != null)
                return false;
        } else if (!columns.equals(other.columns)) {
            return false;
        }
        if (sortOrderList == null) {
            if (other.sortOrderList != null)
                return false;
        } else if (!sortOrderList.equals(other.sortOrderList)) {
            return false;
        }

        return true;
    }

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer();

        sb.append("object: ").append(object).append("\n");
        sb.append("columns: ").append(columns).append("\n");
        sb.append("sortConfiguration: ").append(sortOrderList).append("\n");

        return sb.toString();
    }

    @Override
    public String getType() {
        return getClass().getSimpleName();
    }

    @Override
    protected PreferenceLoader getPreferenceLoader() {
        return super.getPreferenceLoader();
    }

    public TableColumnConfiguration getColumnByName(String columnName) {
        if (columns == null || columns.size() < 1)
            return null;

        if (columnMap == null || columnMap.size() != columns.size()) {
            columnMap = new ConcurrentHashMap<String, TableColumnConfiguration>();
            for (TableColumnConfiguration column : columns) {
                columnMap.put(column.getName(), column);
            }
        }
        return columnMap.get(columnName);
    }

    @Override
    protected Preference copyTo(Preference preference) throws IncompatiblePreferenceTypeException {
        TableConfiguration concretePreference = (TableConfiguration) super.copyTo(preference);
        concretePreference.columnMap = columnMap;
        concretePreference.columns = columns;
        concretePreference.sortOrderList = sortOrderList;

        return concretePreference;
    }

}
