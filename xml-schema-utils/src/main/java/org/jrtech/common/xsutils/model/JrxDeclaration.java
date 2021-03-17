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

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.sun.xml.xsom.XSTerm;

/**
 * A facade to the term declaration of the schema.
 *
 * @param <XSDecl>
 */
public abstract class JrxDeclaration<XSDecl extends XSTerm> extends JrxTerm<XSDecl> implements JrxNode {

    private static final long serialVersionUID = 657455048610963084L;
    
    private JrxElementGroup childrenBlock;
    private String name;
    private Map<String, String> namespace;

    public JrxDeclaration() {
        super();
        namespace = new HashMap<>();
    }
	
    public abstract JrxElement getParentElement();
	
    public JrxElementGroup getChildrenBlock() {
        return childrenBlock;
    }
	
    public void setChildrenBlock(JrxElementGroup childrenBlock) {
        this.childrenBlock = childrenBlock;
        
        if (childrenBlock != null && (childrenBlock.getOwner() == null || !this.equals(childrenBlock.getOwner()))) {
        	childrenBlock.setOwner(this);
        }
    }
	
    public String getName() {
        return name;
    }
	
    public void setName(String name) {
        this.name = name;
    }
	
    public Map<String, String> getNamespace() {
        return namespace;
    }
	
    public void setNamespace(Map<String, String> namespace) {
        this.namespace = namespace;
    }
	
    @Override
    public int hashCode() {
        final int prime = 31; 
        int totalHashCode = 1;

        totalHashCode = prime * totalHashCode + ((getParentElement() == null) ? 0 : getParentElement().hashCode());
        totalHashCode = prime * totalHashCode + ((name == null) ? 0 : name.hashCode());
        totalHashCode = prime * totalHashCode + ((namespace == null) ? 0 : namespace.hashCode());
		
        return totalHashCode;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;

        if (!super.equals(obj)) 
            return false;

		if (!(obj instanceof JrxDeclaration))
			return false;

		final JrxDeclaration<?> other = (JrxDeclaration<?>) obj;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name)) {
			return false;
		}
		if (namespace == null) {
			if (other.namespace != null)
				return false;
		} else if (!namespace.equals(other.namespace)) {
			return false;
		}

		return true;
	}

	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();

		sb.append(name).append(" ");
		sb.append(super.toString());

		return sb.toString();
	}
	
    public String getNamespaceUri(String namespacePrefix) {
        return namespace.get(namespacePrefix);
    }
	
    public void setNamespaceUri(String namespacePrefix, String namespaceUri) {
        this.namespace.put(namespacePrefix, namespaceUri);
    }
	
    public String getNamespacePrefixFromUri(String namespaceUri) {
    	if (namespaceUri == null) return null;
    	
    	for (Entry<String, String> namespaceEntry : namespace.entrySet()) {
    		if (namespaceUri.equals(namespaceEntry.getValue())) {
    			return namespaceEntry.getKey();
    		}
    	}
    	
    	return null;
    }

    @Override
	public long getScopedNameHashCode() {
		if (scopedNameHashCode == 1L) {
			final int prime = 31;

			scopedNameHashCode = prime * scopedNameHashCode + ((name == null) ? 0 : name.hashCode());
			JrxElementGroup parentBlock = getParentBlock();
			long parentBlockHashCode = 0;
			if (parentBlock != null)
				parentBlockHashCode = parentBlock.getScopedNameHashCode();
			scopedNameHashCode = prime * scopedNameHashCode + parentBlockHashCode;
			scopedNameHashCode = prime * scopedNameHashCode + getIndexInSchema();
		}
	    return scopedNameHashCode;
    }

}
