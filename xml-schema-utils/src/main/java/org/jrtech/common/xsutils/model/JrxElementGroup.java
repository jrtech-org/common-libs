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

import com.sun.xml.xsom.XSModelGroup;
import com.sun.xml.xsom.XSModelGroup.Compositor;

import java.util.ArrayList;
import java.util.List;

/**
 * A facade to the element group in schema.
 */
public class JrxElementGroup extends JrxTerm<XSModelGroup> {

    private static final long serialVersionUID = 7199903008599840864L;
    
    private Compositor compositor;
    private List<JrxTerm<?>> elements;
    private JrxDeclaration<?> owner;


    public JrxElementGroup() {
        super();
        elements = new ArrayList<JrxTerm<?>>();
        compositor = XSModelGroup.Compositor.SEQUENCE;
    }
	
    public Compositor getCompositor() {
        return compositor;
    }
	
    public void setCompositor(Compositor compositor) {
        this.compositor = compositor;
    }
	
    public List<JrxTerm<?>> getElements() {
        return elements;
    }
	
    public void setElements(List<JrxTerm<?>> elements) {
        this.elements = elements;
    }
	
    public JrxDeclaration<?> getOwner() {
        return owner;
    }
	
    public void setOwner(JrxDeclaration<?> owner) {
        this.owner = owner;
        
        // add to new parent
        if (owner != null && (owner.getChildrenBlock() == null || !this.equals(owner.getChildrenBlock()))) {
        	owner.setChildrenBlock(this);
        }
    }
	
    @Override
    public int hashCode() {
        final int prime = 31; 
        int totalHashCode = 1;

        totalHashCode = prime * totalHashCode + ((compositor == null) ? 0 : compositor.hashCode());
        totalHashCode = prime * totalHashCode + ((owner == null) ? 0 : owner.hashCode());
		
        return totalHashCode;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;

        if (!super.equals(obj)) 
            return false;

		if (!(obj instanceof JrxElementGroup))
			return false;

		final JrxElementGroup other = (JrxElementGroup) obj;
		if (compositor == null) {
			if (other.compositor != null)
				return false;
		} else if (!compositor.equals(other.compositor)) {
			return false;
		}
		if (owner == null) {
			if (other.owner != null)
				return false;
		} else if (!owner.equals(other.owner)) {
			return false;
		}

		return true;
	}

	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();

		sb.append(compositor);
		sb.append(super.toString());

		return sb.toString();

	}
	
	public boolean isChoiceGroup() {
		if (getXsdDeclaration() == null) {
			return false;
		}
		
		if (getXsdDeclaration().isModelGroup()) {
			return getXsdDeclaration().asModelGroup().getCompositor().equals(XSModelGroup.CHOICE);
		} else if(getXsdDeclaration().isModelGroupDecl()) {
			//return xsParticle.getTerm().asModelGroupDecl().getModelGroup().getCompositor().equals(XSModelGroup.CHOICE);
		    // to be review in the future.
		}
		
		return false;
	}
	
	@Override
	public long getScopedNameHashCode() {
		if (scopedNameHashCode == 1L) {
			final int prime = 31;

			scopedNameHashCode = prime * scopedNameHashCode + ((getCompositor() == null) ? 0 : getCompositor().name().hashCode());
			scopedNameHashCode = prime * scopedNameHashCode + ((getOwner() == null) ? 0 : getOwner().getScopedNameHashCode());
			scopedNameHashCode = prime * scopedNameHashCode + getIndexInSchema();

		}
	    return scopedNameHashCode;
    }

}
