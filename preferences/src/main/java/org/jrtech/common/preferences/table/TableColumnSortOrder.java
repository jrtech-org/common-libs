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

import org.jrtech.common.utils.query.AttributeOrder.SortOrder;

public class TableColumnSortOrder implements Serializable {

    private static final long serialVersionUID = -6125954207480501764L;
    
    private TableColumnConfiguration column;
    private SortOrder order;

    public TableColumnSortOrder() {
        super();
        order = SortOrder.ASCENDING;
    }

    public TableColumnConfiguration getColumn() {
        return column;
    }

    public void setColumn(TableColumnConfiguration column) {
        this.column = column;
    }

    public SortOrder getOrder() {
        return order;
    }

    public void setOrder(SortOrder order) {
        this.order = order;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int totalHashCode = 1;

        totalHashCode = prime * totalHashCode + ((column == null) ? 0 : column.hashCode());
        totalHashCode = prime * totalHashCode + ((order == null) ? 0 : order.hashCode());

        return totalHashCode;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;

        if (!(obj instanceof TableColumnSortOrder))
            return false;

        final TableColumnSortOrder other = (TableColumnSortOrder) obj;
        if (column == null) {
            if (other.column != null)
                return false;
        } else if (!column.equals(other.column)) {
            return false;
        }

        return true;
    }

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer();

        sb.append(column == null ? "NULL" : column.getName()).append(" (").append(order.toCode()).append(")");

        return sb.toString();
    }

    public SortOrder switchSortOrder() {
        if (order.equals(SortOrder.ASCENDING)) {
            order = SortOrder.DESCENDING;
        } else {
            order = SortOrder.ASCENDING;
        }
        return order;
    }

}
