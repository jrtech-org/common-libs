package org.jrtech.common.utils.query;


import java.io.Serializable;
import java.util.List;

public class QueryResponse<T> implements Serializable {

    private static final long serialVersionUID = -4696957216554563983L;

    private List<T> rows;
    
    private long elapsedTime; 
    
    private long totalNumberFound; 
    
    private String effectivQueryString;

    /**
     * @return The rows of data 
     */
    public List<T> getRows() {
        return rows;
    }

    public void setRows(List<T> rows) {
        this.rows = rows;
    }

    /**
     * @return The elapsed time of this query.
     */
    public long getElapsedTime() {
        return elapsedTime;
    }

    public void setElapsedTime(long elapsedTime) {
        this.elapsedTime = elapsedTime;
    }

    /**
     * @return The total number data found based on the effective query criteria.
     */
    public long getTotalNumberFound() {
        return totalNumberFound;
    }

    public void setTotalNumberFound(long totalNumberFound) {
        this.totalNumberFound = totalNumberFound;
    }

    /**
     * @return The effective query string of this query.
     */
    public String getEffectivQueryString() {
        return effectivQueryString;
    }

    /**
     * @param effectivQueryString
     */
    public void setEffectivQueryString(String effectivQueryString) {
        this.effectivQueryString = effectivQueryString;
    } 

}
