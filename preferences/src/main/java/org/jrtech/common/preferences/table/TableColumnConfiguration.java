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

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

public class TableColumnConfiguration implements Serializable {
    private static final long serialVersionUID = -5571468204526909198L;

    public enum Alignment {
        LEFT, RIGHT, CENTER;

        public String toString() {
            return name().toLowerCase();
        }

        public static Alignment fromString(String value) {
            if (value == null || value.equals(""))
                return LEFT;

            for (Alignment align : values()) {
                if (align.name().equalsIgnoreCase(value)) {
                    return align;
                }
            }

            return LEFT;
        }
    };

    private String label;
    private String name;
    private String sortColumnName;
    private TableConfiguration owner;
    private boolean allowed;
    private boolean visible;
    private String retrievingMethod;
    private String width;
    private String seleniumLabel;
    private String columnFormat;
    private String translator;
    private Alignment align = Alignment.LEFT;
    private boolean sortable = true;
    private String relatedColumn;
    private String tooltip;

    public TableColumnConfiguration() {
        super();
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
        if (label == null && name != null && name.length() > 1) {
            label = name.substring(0, 1).toUpperCase() + name.substring(1);
        } else if (label == null) {
            label = "";
        }
    }

    public String getSortColumnName() {
        return sortColumnName;
    }

    public void setSortColumnName(String sortColumnName) {
        this.sortColumnName = sortColumnName;
    }

    public TableConfiguration getOwner() {
        return owner;
    }

    public void setOwner(TableConfiguration owner) {
        this.owner = owner;
    }

    public boolean isAllowed() {
        return allowed;
    }

    public void setAllowed(boolean allowed) {
        this.allowed = allowed;
    }

    public boolean isVisible() {
        return visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    public String getRetrievingMethod() {
        return retrievingMethod;
    }

    public void setRetrievingMethod(String retrievingMethod) {
        this.retrievingMethod = retrievingMethod;
    }

    public String getWidth() {
        return width;
    }

    public void setWidth(String width) {
        this.width = width;
    }

    public void setSeleniumLabel(String seleniumLabel) {
        this.seleniumLabel = seleniumLabel;
    }

    public String getSeleniumLabel() {
        return seleniumLabel;
    }

    public void setColumnFormat(String format) {
        this.columnFormat = format;
    }

    public String getColumnFormat() {
        return columnFormat;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int totalHashCode = 1;

        totalHashCode = prime * totalHashCode + ((label == null) ? 0 : label.hashCode());
        totalHashCode = prime * totalHashCode + ((name == null) ? 0 : name.hashCode());
        totalHashCode += prime * totalHashCode + (allowed ? 1231 : 1237);
        totalHashCode += prime * totalHashCode + (visible ? 1231 : 1237);
        totalHashCode = prime * totalHashCode + ((retrievingMethod == null) ? 0 : retrievingMethod.hashCode());
        totalHashCode = prime * totalHashCode + ((width == null) ? 0 : width.hashCode());

        return totalHashCode;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;

        if (!(obj instanceof TableColumnConfiguration))
            return false;

        final TableColumnConfiguration other = (TableColumnConfiguration) obj;
        if (label == null) {
            if (other.label != null)
                return false;
        } else if (!label.equals(other.label)) {
            return false;
        }
        if (name == null) {
            if (other.name != null)
                return false;
        } else if (!name.equals(other.name)) {
            return false;
        }
        if (allowed != other.allowed)
            return false;
        if (visible != other.visible)
            return false;
        if (retrievingMethod == null) {
            if (other.retrievingMethod != null)
                return false;
        } else if (!retrievingMethod.equals(other.retrievingMethod)) {
            return false;
        }
        if (width == null) {
            if (other.width != null)
                return false;
        } else if (!width.equals(other.width)) {
            return false;
        }

        return true;
    }

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer();

        sb.append("label: ").append(label).append("\n");
        sb.append("name: ").append(name).append("\n");
        sb.append("owner: ").append(owner.getName()).append("\n");
        sb.append("allowed: ").append(allowed).append("\n");
        sb.append("visible: ").append(visible).append("\n");
        sb.append("retrievingMethod: ").append(retrievingMethod).append("\n");
        sb.append("format: ").append(columnFormat).append("\n");
        sb.append("sortColumnName: ").append(sortColumnName).append("\n");
        sb.append("sortable: ").append(sortable).append("\n");
        sb.append("width: ").append(width).append("\n");
        sb.append("relatedColumn: ").append(relatedColumn).append("\n");

        return sb.toString();
    }

    public String getTranslator() {
        return translator;
    }

    public void setTranslator(String translator) {
        this.translator = translator;
    }

    public boolean isSortable() {
        return sortable;
    }

    public void setSortable(boolean sortable) {
        this.sortable = sortable;
    }

    public Alignment getAlign() {
        return align;
    }

    public void setAlign(Alignment align) {
        this.align = align;
    }

    public String getRelatedColumn() {
        return relatedColumn;
    }

    public void setRelatedColumn(String relatedColumn) {
        this.relatedColumn = relatedColumn;
    }

    private Map<String, Object> extendedValues = new HashMap<String, Object>();

    public void resetExtendedValues() {
        extendedValues = new HashMap<String, Object>();
    }

    public void addExtendedValue(String key, Object value) {
        extendedValues.put(key, value);
    }

    public void addExtendedValues(Map<String, Object> values) {
        extendedValues.putAll(values);
    }

    @SuppressWarnings("unchecked")
    public <T> T getExtendedValue(String key) {
        return (T) extendedValues.get(key);
    }

    @SuppressWarnings("unchecked")
    public TableColumnConfiguration copy() {
        TableColumnConfiguration copy = new TableColumnConfiguration();
        copy.align = this.align;
        copy.allowed = this.allowed;
        copy.columnFormat = this.columnFormat;
        if (this.extendedValues != null) {
            try {
                copy.extendedValues = this.extendedValues.getClass().newInstance();
                for (Entry<String, Object> entry : this.extendedValues.entrySet()) {
                    copy.extendedValues.put(entry.getKey(), entry.getValue());
                }
            } catch (Exception e) {
                // ignore this attribute
            }
        }
        copy.label = this.label;
        copy.name = this.name;
        copy.owner = this.owner;
        copy.relatedColumn = this.relatedColumn;
        copy.retrievingMethod = this.retrievingMethod;
        copy.seleniumLabel = this.seleniumLabel;
        copy.sortable = this.sortable;
        copy.sortColumnName = this.sortColumnName;
        copy.translator = this.translator;
        copy.visible = this.visible;
        copy.width = this.width;
        copy.tooltip = this.tooltip;

        return copy;
    }

    public String getTooltip() {
        return tooltip;
    }

    public void setTooltip(String tooltip) {
        this.tooltip = tooltip;
    }

}
