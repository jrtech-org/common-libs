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

import com.sun.xml.xsom.XSModelGroupDecl;

/**
 * A facade to the content group in the schema.
 *
 */
public class JrxGroup extends JrxDeclaration<XSModelGroupDecl> {

    private static final long serialVersionUID = -3276034733050059557L;

    public JrxGroup() {
        super();
    }
	
    @Override
    public int hashCode() {
        int totalHashCode = 1;

        totalHashCode = super.hashCode();
		
        return totalHashCode;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;

        if (!super.equals(obj)) 
            return false;

		if (!(obj instanceof JrxGroup))
			return false;

		return super.equals(obj);
	}

	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();

		sb.append(super.toString());

		return sb.toString();
	}
	
	@Override
	public JrxElement getParentElement() {
		if (getParentBlock() == null) return null;
		
		if (getParentBlock().getOwner() == null) return null;
		
		if (getParentBlock().getOwner() instanceof JrxGroup) {
			return ((JrxGroup) getParentBlock().getOwner()).getParentElement();
		}
		
		return (JrxElement) getParentBlock().getOwner();
	}
	
	@Override
	public String getName() {
		if (getXsdDeclaration() == null)
			return "";
		
		return getXsdDeclaration().getName();
	}

}
