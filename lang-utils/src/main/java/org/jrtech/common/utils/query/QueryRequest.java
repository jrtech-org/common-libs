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
