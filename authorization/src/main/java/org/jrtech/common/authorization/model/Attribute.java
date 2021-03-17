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
package org.jrtech.common.authorization.model;

import java.io.Serializable;

public class Attribute implements Comparable<Attribute>, Cloneable, Serializable {

    private static final long serialVersionUID = -4113477394716221140L;

    private String name;
    
    private String dataType;
    
    public Attribute() {
    }
    
    public Attribute(String name, String dataType) {
        this.name = name;
        this.dataType = dataType;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDataType() {
        return dataType;
    }

    public void setDataType(String dataType) {
        this.dataType = dataType;
    }

    @Override
    public int compareTo(Attribute o) {
        // 1st -> Compare name
        if (name != null && o.name != null) {
            int nameCompareResult = name.compareTo(o.name); 
            if (nameCompareResult != 0) {
                return nameCompareResult;
            }
        } else if (name == null && o.name != null) {
            return -1;
        } else if (name != null && o.name == null) {
            return 1;
        }

        // 2nd -> Compare Data type
        if (dataType == null && o.dataType != null) {
            return -1;
        } else if (dataType != null && o.dataType == null) {
            return 1;
        }
        
        return dataType.compareTo(o.dataType);
    }
}
