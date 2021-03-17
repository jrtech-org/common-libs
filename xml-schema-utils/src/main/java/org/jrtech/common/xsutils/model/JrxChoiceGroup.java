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

import com.sun.xml.xsom.XSModelGroup;
import com.sun.xml.xsom.XSParticle;
import com.sun.xml.xsom.XSTerm;
import com.sun.xml.xsom.XSModelGroup.Compositor;

/**
 * The facade for the choice group in Schema.
 */
public class JrxChoiceGroup extends JrxElementGroup implements JrxNode {

    private static final long serialVersionUID = 5354564101515425125L;

    private String name;

    public JrxChoiceGroup() {
        super();
        setCompositor(Compositor.CHOICE);
        setName(Compositor.CHOICE.name());
    }

    public JrxTerm<? extends XSTerm> getSelection() {
        if (super.getElements().size() > 0)
            return super.getElements().get(0);
        else
            return null;
    }

    public void setSelection(JrxTerm<? extends XSTerm> selection) {
        if (super.getElements().size() == 0)
            super.getElements().add(selection);
        else {
            if (selection == null) {
                clearSelection();
            } else {
                super.getElements().set(0, selection);
            }
        }
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

        if (!(obj instanceof JrxChoiceGroup))
            return false;

        final JrxChoiceGroup other = (JrxChoiceGroup) obj;
        if (getSelection() == null) {
            if (other.getSelection() != null)
                return false;
        } else if (!getSelection().equals(other.getSelection())) {
            return false;
        }

        return true;
    }

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer();

        if (name == null || name.equals(""))
            sb.append(super.toString());
        else
            sb.append(name);
        sb.append(" selection: ").append(getSelection()).append("\n");

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

    @Override
    public List<JrxTerm<?>> getElements() {
        return new ArrayList<JrxTerm<?>>();
    }

    @Override
    public void setElements(List<JrxTerm<?>> elements) {
        // do nothing
    }

    @Override
    public long getScopedNameHashCode() {
        if (scopedNameHashCode == 1L) {
            JrxDeclaration<?> jrxParentDeclaration = getOwner();
            final int prime = 31;

            scopedNameHashCode = prime * scopedNameHashCode
                    + ((getCompositor() == null) ? 0 : getCompositor().name().hashCode());
            scopedNameHashCode = prime * scopedNameHashCode
                    + ((getParentBlock() == null || getParentBlock().getElements() == null) ? 0
                            : getParentBlock().getElements().indexOf(this));
            scopedNameHashCode = prime * scopedNameHashCode
                    + ((jrxParentDeclaration == null) ? 0 : jrxParentDeclaration.getScopedNameHashCode());
            scopedNameHashCode = prime * scopedNameHashCode + ((getName() == null) ? 0 : getName().hashCode());

        }
        return scopedNameHashCode;
    }

    public void clearSelection() {
        if (super.getElements().size() > 0) {
            super.getElements().clear();
        }
    }

    @Override
    public boolean isMandatory() {
        boolean mandatory = super.isMandatory();
        if (mandatory && getXsdDeclaration() != null) {
            // Check if any of its child particle mandatory
            XSModelGroup xsModelGroup = getXsdDeclaration();
            XSParticle[] xsChildParticles = xsModelGroup.getChildren();
            for (XSParticle xsChildParticle : xsChildParticles) {
                if (xsChildParticle.getMinOccurs().intValue() != 0) {
                    return mandatory;
                }
            }

            // All child particles are optional -> choice is also optional!
            return false;
        }

        return mandatory;
    }

}
