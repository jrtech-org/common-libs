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

import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Element;

import com.sun.xml.xsom.XSElementDecl;
import com.sun.xml.xsom.XSParticle;

/**
 * A facade for the element definition of Xml that binds with element in schema.
 *
 */
public class JrxElement extends JrxDeclaration<XSElementDecl> {
    
    private static final long serialVersionUID = 1926820644012114727L;

    private List<JrxAttribute> attributeList;
	private JrxDocument ownerDocument;
	private Element xmlElement;

	private JrxChoiceGroup scopedSingleChoiceGroup;
	private ChoiceDefinedLocationCategory choiceLocationCategory;

	public JrxElement() {
		super();
		attributeList = new ArrayList<JrxAttribute>();
	}

	public List<JrxAttribute> getAttributeList() {
		return attributeList;
	}

	public void setAttributeList(List<JrxAttribute> attributeList) {
		this.attributeList = attributeList;
	}

	public JrxDocument getOwnerDocument() {
		return ownerDocument;
	}

	public void setOwnerDocument(JrxDocument ownerDocument) {
		if (xmlElement != null) {
			if (ownerDocument == null) {
				if (this.ownerDocument != null) {
					this.ownerDocument.remove(xmlElement);
				}
				this.ownerDocument = ownerDocument;
			} else {
				this.ownerDocument = ownerDocument;
				this.ownerDocument.put(xmlElement, this);
			}
		} else {
			this.ownerDocument = ownerDocument;
		}

		if (getChildrenBlock() != null) {
			propagateOwnerDocument(getChildrenBlock());
		}
	}

	public Element getXmlElement() {
		return xmlElement;
	}

	public void setXmlElement(Element xmlElement) {
		this.xmlElement = xmlElement;
	}

	@Override
	public JrxElement getParentElement() {
		if (getParentBlock() == null)
			return null;

		if (getParentBlock().getOwner() == null)
			return null;

		if (getParentBlock().getOwner() instanceof JrxGroup) {
			return ((JrxGroup) getParentBlock().getOwner()).getParentElement();
		}

		return (JrxElement) getParentBlock().getOwner();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int totalHashCode = 1;

		totalHashCode = super.hashCode();
		totalHashCode = prime * totalHashCode + ((xmlElement == null) ? 0 : xmlElement.hashCode());

		return totalHashCode;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;

		if (!super.equals(obj))
			return false;

		if (!(obj instanceof JrxElement))
			return false;

		final JrxElement other = (JrxElement) obj;

		if (xmlElement == null) {
			if (other.xmlElement != null)
				return false;
		} else if (!xmlElement.equals(other.xmlElement)) {
			return false;
		}

		return true;
	}

	public boolean isLeaf() {
		if (getXsdDeclaration() == null) {
			return (getChildrenBlock() == null ? true : getChildrenBlock().getElements().size() < 1);
		}

		if (getXsdDeclaration().getType().isSimpleType()) {
			return true;
		}

		return !(getXsdDeclaration().getType().asComplexType().getContentType() instanceof XSParticle);
	}

	protected void setNamespacePrefix(String prefix) {
		if (prefix == null || prefix.equals("")) {
			setName(getSimpleName());
		} else {
			setName(prefix + ":" + getSimpleName());
		}

	}

	public boolean isEmpty() {
		if (xmlElement == null)
			return true;

		return xmlElement.getTextContent().trim().equals("");
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

	public String getNamespaceUri() {
		return getNamespace().get(getNamespacePrefix());
	}

	public JrxChoiceGroup getScopedSingleChoiceGroup() {
		if (getXsdDeclaration() == null)
			return null; // no choice definition when there is no schema applied.

		if (ChoiceDefinedLocationCategory.NONE.equals(choiceLocationCategory)) {
			// It has been checked before
			return null;
		}

		if (scopedSingleChoiceGroup == null) {
			// search for scopedSingleChoiceGroup
			scopedSingleChoiceGroup = JrxChoiceGroupUtil.findScopedSingleJrxChoiceGroup(this);
		}

		return scopedSingleChoiceGroup;
	}

	public ChoiceDefinedLocationCategory getChoiceLocationCategory() {
		if (getXsdDeclaration() == null)
			return null; // no choice definition when there is no schema applied.

		if (getChildrenBlock() != null) {
			JrxChoiceGroup jrxChoiceGroup = getScopedSingleChoiceGroup();
			if (jrxChoiceGroup == null) {
				choiceLocationCategory = ChoiceDefinedLocationCategory.NONE;
			} else {
				if (jrxChoiceGroup.equals(getChildrenBlock())) {
					choiceLocationCategory = ChoiceDefinedLocationCategory.DIRECT;
				} else {
					choiceLocationCategory = ChoiceDefinedLocationCategory.INDIRECT;
				}
			}
		}

		return choiceLocationCategory;
	}

	public void resetChoiceLocationCategory() {
		scopedSingleChoiceGroup = null;
		choiceLocationCategory = null;
	}

	private void propagateOwnerDocument(JrxElementGroup jrxElementGroup) {
		if (jrxElementGroup instanceof JrxChoiceGroup) {
			JrxTerm<?> term = ((JrxChoiceGroup) jrxElementGroup).getSelection();
			if (term != null) {
				if (term instanceof JrxElement) {
					((JrxElement) term).setOwnerDocument(ownerDocument);
				} else if (term instanceof JrxElementGroup) {
					propagateOwnerDocument((JrxElementGroup) term);
				}
			}
		} else {
			for (JrxTerm<?> term : jrxElementGroup.getElements()) {
				if (term instanceof JrxElement) {
					((JrxElement) term).setOwnerDocument(ownerDocument);
				} else if (term instanceof JrxElementGroup) {
					propagateOwnerDocument((JrxElementGroup) term);
				}
			}
		}
	}
	
	public enum ChoiceDefinedLocationCategory {

		NONE,

		/**
		 * Considered as a direct choice when the choice element is positioned directly after the followings:
		 * <p>
		 * <b>Case 1</b>
		 * 
		 * <pre>
		 * {@code
		 * <xs:element>
		 *   <xs:choice>...</xs:choice>
		 * </xs:element>
		 * }
		 * </pre>
		 * 
		 * <b>Case 2</b>
		 * 
		 * <pre>
		 * {@code
		 * <xs:complexType>
		 *   <xs:choice>...</xs:choice>
		 * </xs:complexType> 
		 * }
		 * </pre>
		 * 
		 * <b>Case 3</b>
		 * 
		 * <pre>
		 * {@code
		 * <xs:complexType>
		 *   <xs:complexContent>
		 *     <xs:choice>...</xs:choice>
		 *   </xs:complexContent>
		 * </xs:complexType> 
		 * }
		 * </pre>
		 * 
		 * and as the only available single element within it's container.
		 */
		DIRECT,

		/**
		 * Considered as an in-direct choice when the choice element is positioned directly after the followings:
		 * <p>
		 * <b>Case 1</b>
		 * 
		 * <pre>
		 * {@code
		 * <xs:element>
		 *   <xs:sequence>
		 *     <xs:choice>...</xs:choice>
		 *   </xs:sequence>
		 * </xs:element>
		 * }
		 * </pre>
		 * 
		 * <b>Case 2</b>
		 * 
		 * <pre>
		 * {@code
		 * <xs:complexType>
		 *   <xs:sequence>
		 *     <xs:choice>...</xs:choice>
		 *   </xs:sequence>
		 * </xs:complexType> 
		 * }
		 * </pre>
		 * 
		 * <b>Case 3</b>
		 * 
		 * <pre>
		 * {@code
		 * <xs:complexType>
		 *   <xs:complexContent>
		 *     <xs:sequence>
		 *       <xs:choice>...</xs:choice>
		 *     </xs:sequence>
		 *   </xs:complexContent>
		 * </xs:complexType> 
		 * }
		 * </pre>
		 * 
		 * and as the only available single element within it's container. The model group <code>&lt;xs:all&gt;</code>
		 * is equivalent to <code>&lt;xs:sequence&gt;</code>.
		 */
		INDIRECT;

	}

}
