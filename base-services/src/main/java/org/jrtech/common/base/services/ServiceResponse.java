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
package org.jrtech.common.base.services;

import java.io.Serializable;

public class ServiceResponse<T> implements Serializable {

    private static final long serialVersionUID = 3201552571977976301L;

    private long id;

    private ServiceRequest relatedRequest;

    private DataLocation<T> resultValue;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public ServiceRequest getRelatedRequest() {
        return relatedRequest;
    }
    
    public void setRelatedRequest(ServiceRequest relatedRequest) {
        this.relatedRequest = relatedRequest;
    }
    
    public DataLocation<T> getResultValue() {
        return resultValue;
    }

    public void setResultValue(DataLocation<T> resultValue) {
        this.resultValue = resultValue;
    }
}
