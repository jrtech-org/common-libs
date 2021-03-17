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
import java.math.BigInteger;

import org.jrtech.common.xmlutils.XmlUtils;
import org.w3c.dom.Element;

import com.sun.xml.xsom.XSAnnotation;
import com.sun.xml.xsom.XSElementDecl;
import com.sun.xml.xsom.XSModelGroup;
import com.sun.xml.xsom.XSParticle;
import com.sun.xml.xsom.XSTerm;
import com.sun.xml.xsom.XSType;

/**
 * A delegate to the Schema Term
 */
public abstract class JrxTerm<XST extends XSTerm> implements Serializable {

    private static final long serialVersionUID = 672194520754460948L;
    private JrxElementGroup parentBlock;
    private JrxChoiceGroup origin;
    private int minOccurs;
    private int maxOccurs;
    private XST xsdDeclaration;

    public static final String ANNOTATION_APPINFO = "xs:appinfo";
    public static final String ANNOTATION_DOCUMENTATION = "xs:documentation";

    private String schemaDocumentation;
    private Element schemaAppInfoElement;
    protected long scopedNameHashCode = 1L;

    public JrxTerm() {
        super();
        minOccurs = 1;
        maxOccurs = 1;
    }

    public boolean isMandatory() {
        if (isRepetitive() && minOccurs > 0 && parentBlock != null) {
            // Count occurence within the parent block
            if (xsdDeclaration != null) {
                int occuranceCount = 0;
                int currentTermIndex = parentBlock.getElements().indexOf(this);
                // Count previous sibling
                int i = currentTermIndex - 1;
                while (i >= 0) {
                    JrxTerm<?> jrxPreviousSiblingTerm = parentBlock.getElements().get(i);
                    if (xsdDeclaration.equals(jrxPreviousSiblingTerm.getXsdDeclaration())) {
                        occuranceCount++;
                    } else {
                        // Stop
                        break;
                    }
                    i--;
                }

                return (occuranceCount < minOccurs);
            }
        }
        return minOccurs != 0;
    }

    public boolean isRepetitive() {
        return maxOccurs > 1 || maxOccurs == -1;
    }

    public JrxElementGroup getParentBlock() {
        return parentBlock;
    }

    public void setParentBlock(JrxElementGroup parentBlock) {
        // remove from old parent
        if (this.parentBlock != null && (parentBlock == null || !this.parentBlock.equals(parentBlock))) {
            this.parentBlock.getElements().remove(this);
        }

        this.parentBlock = parentBlock;

        // add to new parent
        if (parentBlock != null && (!parentBlock.getElements().contains(this))) {
            parentBlock.getElements().add(this);
        }
    }

    public JrxChoiceGroup getOrigin() {
        return origin;
    }

    public void setOrigin(JrxChoiceGroup origin) {
        this.origin = origin;
    }

    public int getMinOccurs() {
        return minOccurs;
    }

    public void setMinOccurs(BigInteger minOccurs) {
        this.minOccurs = minOccurs != null ? minOccurs.intValue() : 1;
    }
    
    public void setMinOccurs(int minOccurs) {
        this.minOccurs = minOccurs;
    }

    public int getMaxOccurs() {
        return maxOccurs;
    }

    public void setMaxOccurs(BigInteger maxOccurs) {
        this.maxOccurs = maxOccurs != null ? maxOccurs.intValue() : 1;
    }
    
    public void setMaxOccurs(int maxOccurs) {
        this.maxOccurs = maxOccurs;
    }

    public XST getXsdDeclaration() {
        return xsdDeclaration;
    }

    public void setXsdDeclaration(XST xsdDeclaration) {
        this.xsdDeclaration = xsdDeclaration;

        if (xsdDeclaration != null && (schemaAppInfoElement == null)) {
            XSAnnotation xsAnnotation = xsdDeclaration.getAnnotation();

            if (xsAnnotation == null && xsdDeclaration.isElementDecl()) {
                // Element Declaration does not have annotation -> use
                // annotation from type
                XSElementDecl xsElementDecl = xsdDeclaration.asElementDecl();
                XSType xsType = xsElementDecl.getType();
                xsAnnotation = xsType.getAnnotation();
            }

            if (xsAnnotation != null) {
                Object annoObject = xsAnnotation.getAnnotation();
                if (annoObject != null && annoObject instanceof Element) {
                    Element xmlAnnotationElement = (Element) annoObject;
                    schemaAppInfoElement = XmlUtils.getChildByTagName(xmlAnnotationElement, ANNOTATION_APPINFO);
                    Element xmlSchemaDocumentationElement = XmlUtils.getChildByTagName(xmlAnnotationElement,
                            ANNOTATION_DOCUMENTATION);
                    if (xmlSchemaDocumentationElement != null) {
                        schemaDocumentation = xmlSchemaDocumentationElement.getTextContent();
                    }
                }
            }

        }
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int totalHashCode = 1;

        totalHashCode = prime * totalHashCode + ((parentBlock == null) ? 0 : parentBlock.hashCode());
        totalHashCode = prime * totalHashCode + ((origin == null) ? 0 : origin.hashCode());
        totalHashCode = prime * totalHashCode + minOccurs;
        totalHashCode = prime * totalHashCode + maxOccurs;

        return totalHashCode;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;

        if (!(obj instanceof JrxTerm))
            return false;

        final JrxTerm<?> other = (JrxTerm<?>) obj;
        if (parentBlock == null) {
            if (other.parentBlock != null)
                return false;
        } else if (!(parentBlock instanceof JrxChoiceGroup) && !parentBlock.equals(other.parentBlock)) {
            return false;
        }
        if (minOccurs != other.minOccurs)
            return false;
        if (maxOccurs != other.maxOccurs)
            return false;
        if (xsdDeclaration == null) {
            if (other.xsdDeclaration != null)
                return false;
        } else if (!xsdDeclaration.equals(other.xsdDeclaration)) {
            return false;
        }

        return true;
    }

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer();

        sb.append(" ").append(minOccurs);
        sb.append("..").append(maxOccurs);

        return sb.toString();
    }

    public Element getSchemaAppInfoElement() {
        return schemaAppInfoElement;
    }

    public void setSchemaAppInfoElement(Element schemaAppInfoElement) {
        this.schemaAppInfoElement = schemaAppInfoElement;
    }

    public String getSchemaDocumentation() {
        return schemaDocumentation;
    }

    public void setSchemaDocumentation(String schemaDocumentation) {
        this.schemaDocumentation = schemaDocumentation;
    }

    protected int getIndexInSchema() {
        int schemaIndex = 0;
        if (parentBlock == null || parentBlock.getXsdDeclaration() == null)
            return schemaIndex;

        XSModelGroup xsModelGroup = getParentBlock().getXsdDeclaration();
        XSTerm xsTerm = getXsdDeclaration();
        if (xsTerm == null || xsModelGroup == null)
            return schemaIndex; // No schema

        XSParticle[] xsChildParticles = xsModelGroup.getChildren();
        if (xsChildParticles != null) {
            for (int i = 0; i < xsChildParticles.length; i++) {
                if (xsTerm.equals(xsChildParticles[i].getTerm())) {
                    schemaIndex = i;
                    break;
                }
            }
        }

        return schemaIndex;
    }

    public abstract long getScopedNameHashCode();
}
