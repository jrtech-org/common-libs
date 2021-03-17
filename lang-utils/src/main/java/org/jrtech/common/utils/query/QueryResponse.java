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
