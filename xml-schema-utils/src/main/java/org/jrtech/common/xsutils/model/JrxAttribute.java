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
package org.jrtech.common.xsutils.model;

import java.io.Serializable;

import com.sun.xml.xsom.XSAttributeDecl;

/**
 * The binding class for Xml Attribute and its Schema.
 *
 */
public class JrxAttribute implements Serializable, JrxNode {

    private static final long serialVersionUID = -6364274615029272526L;
    
    private String name;
    private JrxElement owner;
    private XSAttributeDecl xsdDeclaration;
    private boolean mandatory = false;

    public JrxAttribute() {
        super();
    }

    public JrxElement getOwner() {
        return owner;
    }

    public void setOwner(JrxElement owner) {
        this.owner = owner;
    }

    public XSAttributeDecl getXsdDeclaration() {
        return xsdDeclaration;
    }

    public void setXsdDeclaration(XSAttributeDecl xsdDeclaration) {
        this.xsdDeclaration = xsdDeclaration;
    }

    public boolean isMandatory() {
        return mandatory;
    }

    public void setMandatory(boolean mandatory) {
        this.mandatory = mandatory;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int totalHashCode = 1;

        totalHashCode = prime * totalHashCode + ((owner == null) ? 0 : owner.hashCode());
        totalHashCode = prime * totalHashCode + ((xsdDeclaration == null) ? 0 : xsdDeclaration.hashCode());
        totalHashCode += prime * totalHashCode + (mandatory ? 1231 : 1237);

        return totalHashCode;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;

        if (!(obj instanceof JrxAttribute))
            return false;

        final JrxAttribute other = (JrxAttribute) obj;
        if (owner == null) {
            if (other.owner != null)
                return false;
        } else if (!owner.equals(other.owner)) {
            return false;
        }
        if (xsdDeclaration == null) {
            if (other.xsdDeclaration != null)
                return false;
        } else if (!xsdDeclaration.equals(other.xsdDeclaration)) {
            return false;
        }
        if (mandatory != other.mandatory)
            return false;

        return true;
    }

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer();

        sb.append("name: ").append(name).append("\n");
        sb.append("owner: ").append(owner).append("\n");
        sb.append("xsdDeclaration: ").append(xsdDeclaration).append("\n");
        sb.append("mandatory: ").append(mandatory).append("\n");

        return sb.toString();
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    public String getSimpleName() {
        int index = getName().lastIndexOf(":");
        if (index >= 0) {
            return getName().substring(index + 1);
        }
        return getName();
    }

    public String getNamespacePrefix() {
        int index = getName().lastIndexOf(":");
        if (index >= 0) {
            return getName().substring(0, index);
        }
        return "";
    }

}
