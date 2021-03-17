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
package org.jrtech.common.utils.model;


import java.io.Serializable;

public class LabelDefinition implements Serializable {

    private static final long serialVersionUID = -7248356583048245171L;

    private String labelKey;
    
    private String defaultLabel;

    public LabelDefinition(String labelKey, String defaultLabel) {
        super();
        this.labelKey = labelKey;
        this.defaultLabel = defaultLabel;
    }

    public String getLabelKey() {
        return labelKey;
    }

    public String getDefaultLabel() {
        return defaultLabel;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int totalHashCode = 1;

        totalHashCode = prime * totalHashCode + ((labelKey == null) ? 0 : labelKey.hashCode());
        totalHashCode = prime * totalHashCode + ((defaultLabel == null) ? 0 : defaultLabel.hashCode());

        return totalHashCode;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;

        if (!(obj instanceof LabelDefinition))
            return false;

        final LabelDefinition other = (LabelDefinition) obj;
        if (labelKey == null) {
            if (other.labelKey != null)
                return false;
        } else if (!labelKey.equals(other.labelKey)) {
            return false;
        }
        if (defaultLabel == null) {
            if (other.defaultLabel != null)
                return false;
        } else if (!defaultLabel.equals(other.defaultLabel)) {
            return false;
        }

        return true;
    }

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer();

        sb.append("labelKey: ").append(labelKey).append(" -> '");
        sb.append(defaultLabel);

        return sb.toString();
    }

}
