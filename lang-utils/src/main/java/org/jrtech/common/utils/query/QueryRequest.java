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
package org.jrtech.common.utils.query;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class QueryRequest implements Serializable {
    
    private static final long serialVersionUID = -7057210663136189175L;

    private String criteriaString;

    private List<AttributeOrder> sortingOrder;
    
    private int pageIndex;
    
    private int pageSize;
    
    public QueryRequest() {
        sortingOrder = new ArrayList<>();
        pageIndex = 1;
        pageSize = 50;
    }

    public void setSortingOrder(List<AttributeOrder> sortingOrder) {
        this.sortingOrder = sortingOrder;
    }

    public List<AttributeOrder> getSortingOrder() {
        return sortingOrder;
    }

    public String getCriteriaString() {
        return criteriaString;
    }

    public void setCriteriaString(String criteriaString) {
        this.criteriaString = criteriaString;
    }

    public int getPageIndex() {
        return pageIndex;
    }

    public void setPageIndex(int pageIndex) {
        this.pageIndex = pageIndex;
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

}
