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
package org.jrtech.common.xsutils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jrtech.common.xsutils.model.JrxChoiceGroup;
import org.jrtech.common.xsutils.model.JrxDeclaration;
import org.jrtech.common.xsutils.model.JrxElement;
import org.jrtech.common.xsutils.model.JrxElementGroup;
import org.jrtech.common.xsutils.model.JrxTerm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.xml.xsom.XSComplexType;
import com.sun.xml.xsom.XSElementDecl;
import com.sun.xml.xsom.XSModelGroup;
import com.sun.xml.xsom.XSModelGroup.Compositor;
import com.sun.xml.xsom.XSModelGroupDecl;
import com.sun.xml.xsom.XSParticle;
import com.sun.xml.xsom.XSTerm;
import com.sun.xml.xsom.XSType;

/**
 * The Xml Element content matching based on Schema.
 *
 */
public class JrxXmlModelMatchingUtil implements Serializable {

    private static final long serialVersionUID = 2487479808286439111L;

    private static final Logger log = LoggerFactory.getLogger(JrxXmlModelMatchingUtil.class);

    private XSParticle[] leftValueArray;

    private Map<String, Integer> leftValueIndexTable;

    private List<JrxTerm<?>>[] rightExistingParticleArray;

    private XSParticle[] rightNewParticleArray;

    public static JrxXmlModelMatchingUtil getInstance() {
        return new JrxXmlModelMatchingUtil();
    }

    public boolean performMatching(JrxElement jrxElement) {
        return performMatching(jrxElement.getChildrenBlock());
    }

    @SuppressWarnings({ "unchecked", "unused" })
    private boolean buildMatchingTable(XSModelGroup xsModelGroup) {
        // Jumin: This method is not yet used but preserve for future feature.
        if (xsModelGroup == null) {
            return false;
        }

        leftValueArray = retrieveParticles(xsModelGroup).toArray(new XSParticle[] {});
        rightExistingParticleArray = new List[leftValueArray.length];
        for (int i = 0; i < rightExistingParticleArray.length; i++) {
            rightExistingParticleArray[i] = new ArrayList<JrxTerm<?>>();
        }
        rightNewParticleArray = new XSParticle[leftValueArray.length];
        leftValueIndexTable = indexValueList(leftValueArray);

        return true;
    }

    @SuppressWarnings("unchecked")
    private boolean buildMatchingTable(JrxElementGroup jrxElementGroup) {
        if (jrxElementGroup == null) {
            return false;
        }

        XSModelGroup xsModelGroup = jrxElementGroup.getXsdDeclaration();
        if (xsModelGroup == null) {
            return false;
        }

        if (XSModelGroup.CHOICE.equals(xsModelGroup.getCompositor())) {
            XSParticle xsModelGroupParticle = retrieveParticleOfChoiceElementGroup((JrxChoiceGroup) jrxElementGroup);
            if (xsModelGroupParticle != null) {
                leftValueArray = new XSParticle[] { xsModelGroupParticle };
            }
        } else {
            leftValueArray = retrieveParticles(xsModelGroup).toArray(new XSParticle[] {});
        }

        rightExistingParticleArray = new List[leftValueArray.length];
        for (int i = 0; i < rightExistingParticleArray.length; i++) {
            rightExistingParticleArray[i] = new ArrayList<JrxTerm<?>>();
        }
        rightNewParticleArray = new XSParticle[leftValueArray.length];
        leftValueIndexTable = indexValueList(leftValueArray);

        return true;
    }

    private XSParticle retrieveParticleOfChoiceElementGroup(JrxChoiceGroup jrxChoiceGroup) {
        if (jrxChoiceGroup.getOwner() != null) {
            JrxDeclaration<?> jrxDeclaration = jrxChoiceGroup.getOwner();
            if (!(jrxDeclaration instanceof JrxElement)) {
                return null;
            }
            JrxElement jrxElement = (JrxElement) jrxDeclaration;
            XSType xsType = jrxElement.getXsdDeclaration().getType();
            XSComplexType xsComplexType = xsType.asComplexType();
            XSParticle xsParticle = xsComplexType.getContentType().asParticle();

            if (xsParticle.getTerm().equals(jrxChoiceGroup.getXsdDeclaration())) {
                return xsParticle;
            }
        } else {
            JrxElementGroup jrxParentBlock = jrxChoiceGroup.getParentBlock();
            if (jrxParentBlock == null)
                return null;

            XSModelGroup xsParentModelGroup = jrxParentBlock.getXsdDeclaration();
            for (XSParticle xsParticle : xsParentModelGroup.getChildren()) {
                if (xsParticle.getTerm().equals(jrxChoiceGroup.getXsdDeclaration())) {
                    return xsParticle;
                }
            }
        }
        return null;
    }

    public boolean performMatching(JrxElementGroup jrxElementGroup) {
        if (jrxElementGroup == null || jrxElementGroup.getXsdDeclaration() == null) {
            return false;
        }

        if (!buildMatchingTable(jrxElementGroup))
            return false;

        // Fill table by existing elements
        if (jrxElementGroup.isChoiceGroup()) {
            if (jrxElementGroup instanceof JrxChoiceGroup) {
                fillExistingParticle(((JrxChoiceGroup) jrxElementGroup).getSelection(), jrxElementGroup.getOwner());
            } else {
                // TODO: unsupported case
            }
        } else {
            for (JrxTerm<?> jrxTerm : jrxElementGroup.getElements()) {
                fillExistingParticle(jrxTerm, jrxElementGroup.getOwner());
            }
        }

        // Identify new elements or choice-blocks
        for (int i = 0; i < leftValueArray.length; i++) {
            if (rightExistingParticleArray[i].size() > 0) {
                // if (leftValueArray[i].getTerm() instanceof XSModelGroup &&
                // ((XSModelGroup)
                // leftValueArray[i].getTerm()).getCompositor().equals(XSModelGroup.CHOICE))
                // {
                // rightNewParticleArray[i] = leftValueArray[i];
                // }
            } else {
                if (!jrxElementGroup.isChoiceGroup()) {
                    rightNewParticleArray[i] = leftValueArray[i];
                }
            }
        }

        return true;
    }

    private List<XSParticle> retrieveParticles(XSModelGroup xsModelGroup) {
        if (xsModelGroup == null) {
            return null;
        }

        List<XSParticle> jrxParticleList = new ArrayList<XSParticle>();

        for (int i = 0; i < xsModelGroup.getSize(); i++) {
            XSParticle xsChildParticle = xsModelGroup.getChild(i);
            XSTerm xsChildTerm = xsChildParticle.getTerm();
            if (xsChildTerm instanceof XSElementDecl) {
                jrxParticleList.add(xsChildParticle);
            } else if (xsChildTerm instanceof XSModelGroup) {
                XSModelGroup xsChildModelGroup = (XSModelGroup) xsChildTerm;
                if (XSModelGroup.CHOICE.equals(xsChildModelGroup.getCompositor())) {
                    jrxParticleList.add(xsChildParticle);
                } else {
                    jrxParticleList.addAll(retrieveParticles(xsChildModelGroup));
                }
            } else if (xsChildTerm instanceof XSModelGroupDecl) { // xs:group
                jrxParticleList.add(xsChildParticle);
            } else {
                log.warn("Unhandled Model Group Term: " + xsChildTerm.getClass().getSimpleName() + " -> "
                        + xsModelGroup.getLocator().getLineNumber());
            }
        }

        return jrxParticleList;
    }

    private Map<String, Integer> indexValueList(XSParticle[] valueArray) {
        if (valueArray == null) {
            return null;
        }

        Map<String, Integer> result = new HashMap<String, Integer>();
        for (int i = 0; i < valueArray.length; i++) {
            XSParticle xsParticle = valueArray[i];
            XSTerm xsTerm = xsParticle.getTerm();
            if (xsTerm.isElementDecl()) {
                result.put(xsTerm.asElementDecl().getName(), i);
            } else if (xsTerm.isModelGroup()) {
                XSModelGroup xsModelGroup = xsTerm.asModelGroup();
                List<XSElementDecl> xsSubElementList = retrieveElementsFromModelGroup(xsModelGroup);
                for (XSElementDecl xsSubElement : xsSubElementList) {
                    result.put(xsSubElement.getName(), i);
                }
            } else if (xsTerm.isModelGroupDecl()) {
                XSModelGroupDecl xsModelGroupDecl = xsTerm.asModelGroupDecl();
                List<XSElementDecl> xsSubElementList = retrieveElementsFromModelGroupDecl(xsModelGroupDecl);
                for (XSElementDecl xsSubElement : xsSubElementList) {
                    result.put(xsSubElement.getName(), i);
                }
            } else {
                log.warn("Unhandled Model Group Term: " + xsTerm.getClass().getSimpleName());
            }
        }

        return result;
    }

    private List<XSElementDecl> retrieveElementsFromModelGroup(XSModelGroup xsModelGroup) {
        if (xsModelGroup == null) {
            return null;
        }

        List<XSElementDecl> jrxTermList = new ArrayList<XSElementDecl>();

        // if (!xsModelGroup.getCompositor().equals(XSModelGroup.CHOICE)) {
        // return jrxTermList;
        // }

        for (int i = 0; i < xsModelGroup.getSize(); i++) {
            XSParticle xsChildParticle = xsModelGroup.getChild(i);
            XSTerm xsChildTerm = xsChildParticle.getTerm();
            if (xsChildTerm instanceof XSElementDecl) {
                jrxTermList.add(xsChildTerm.asElementDecl());
            } else if (xsChildTerm instanceof XSModelGroup) {
                XSModelGroup xsChildModelGroup = (XSModelGroup) xsChildTerm;
                jrxTermList.addAll(retrieveElementsFromModelGroup(xsChildModelGroup));
            } else if (xsChildTerm instanceof XSModelGroupDecl) {
                XSModelGroupDecl xsChildModelGroupDecl = (XSModelGroupDecl) xsChildTerm;
                jrxTermList.addAll(retrieveElementsFromModelGroupDecl(xsChildModelGroupDecl));
            } else {
                log.warn("Unhandled Model Group Term: " + xsChildTerm.getClass().getSimpleName() + " -> "
                        + xsModelGroup.getLocator().getLineNumber());
            }
        }

        return jrxTermList;
    }

    private List<XSElementDecl> retrieveElementsFromModelGroupDecl(XSModelGroupDecl xsModelGroupDecl) {
        if (xsModelGroupDecl == null) {
            return null;
        }

        return retrieveElementsFromModelGroup(xsModelGroupDecl.getModelGroup());
    }

    public XSParticle[] getNewParticleArray() {
        return rightNewParticleArray;
    }

    public List<JrxTerm<?>>[] getExistingTermArray() {
        return rightExistingParticleArray;
    }

    public XSParticle[] getSchemaParticleArray() {
        return leftValueArray;
    }

    public boolean isMergeRequired() {
        for (int i = 0; i < rightNewParticleArray.length; i++) {
            if (rightNewParticleArray[i] != null) {
                return true;
            }
        }

        return false;
    }

    private void fillExistingParticle(JrxTerm<?> jrxTerm, JrxDeclaration<?> parentDeclaration) {
        if (jrxTerm == null)
            return;

        if (jrxTerm instanceof JrxElement) {
            JrxElement jrxElement = (JrxElement) jrxTerm;
            Integer termIndex = leftValueIndexTable.get(jrxElement.getSimpleName());
            if (termIndex == null) {
                log.warn("Sub-element: " + jrxElement.getSimpleName() + " is not defined in the schema.");
                return;
            }

            XSParticle xsParticle = leftValueArray[termIndex];
            XSTerm xsTerm = xsParticle.getTerm();
            if (xsTerm.isElementDecl()) {
                rightExistingParticleArray[termIndex].add(jrxElement);
            } else if (xsTerm.isModelGroup()) {
                XSModelGroup xsModelGroup = xsTerm.asModelGroup();
                if (xsModelGroup.getCompositor().equals(Compositor.CHOICE)) {
                    // choice
                    log.debug("Sub-element: " + jrxElement.getSimpleName() + " is positioned inside a choice");
                    rightExistingParticleArray[termIndex].add(jrxElement);
                } else {
                    // Very rare case, but who knows when someone define nested
                    // ALL and SEQUENCE
                    rightExistingParticleArray[termIndex].add(jrxElement);
                }
            } else if (xsTerm.isModelGroupDecl()) {
                // choice
                log.debug("Sub-element: " + jrxElement.getSimpleName() + " shall be positioned inside a group");
                rightExistingParticleArray[termIndex].add(jrxElement);
            } else {
                log.warn("Sub-element: " + jrxElement.getSimpleName() + " is defined differently in the schema as: "
                        + xsTerm.getClass());
            }
        } else if (jrxTerm instanceof JrxElementGroup) {
            JrxElementGroup jrxSubElementGroup = (JrxElementGroup) jrxTerm;
            boolean matchGroup = false;
            boolean nestedElementGroupCheckAgainstResolvedParticles = false;
            for (int i = 0; i < leftValueArray.length; i++) {
                if (leftValueArray[i].getTerm().equals(jrxSubElementGroup.getXsdDeclaration())) {
                    rightExistingParticleArray[i].add(jrxTerm);
                    matchGroup = true;
                    break;
                }
            }

            if (!matchGroup && jrxSubElementGroup.getXsdDeclaration() != null
                    && !Compositor.CHOICE.equals(jrxSubElementGroup.getXsdDeclaration().getCompositor())) {
                nestedElementGroupCheckAgainstResolvedParticles = true;
            }
            if (nestedElementGroupCheckAgainstResolvedParticles) {
                List<XSParticle> xsSubSubParticleList = retrieveParticles(jrxSubElementGroup.getXsdDeclaration());
                int matchCount = 0;
                for (int i = 0; i < xsSubSubParticleList.size(); i++) {
                    XSParticle xsSubSubParticle = xsSubSubParticleList.get(i);
                    if (xsSubSubParticle.getTerm() instanceof XSElementDecl) {
                        XSElementDecl xsSubSubElementDecl = xsSubSubParticle.getTerm().asElementDecl();
                        if (leftValueIndexTable.containsKey(xsSubSubElementDecl.getName())) {
                            int existingSubElementIndex = leftValueIndexTable.get(xsSubSubElementDecl.getName());
                            JrxDeclaration<?> jrxSubSubDeclaration = retrieveSubJrxDeclarationByName(jrxSubElementGroup,
                                    xsSubSubElementDecl.getName());
                            rightExistingParticleArray[existingSubElementIndex].add(jrxSubSubDeclaration);
                            matchCount++;
                        } else {
                            rightNewParticleArray[i] = xsSubSubParticle;
                        }
                    }
                }

                if (matchCount == xsSubSubParticleList.size()) {
                    // all sub declarations matched
                    if (log.isDebugEnabled()) {
                        log.debug("FMS Element sub group matched! -> " + parentDeclaration.getName());
                    }
                    matchGroup = true;
                } else {
                    // TODO: What are we going to do with this case???
                    if (log.isDebugEnabled()) {
                        log.debug("FMS Element sub group matched! -> " + parentDeclaration.getName());
                    }
                }
            }

            if (!matchGroup) {
                log.warn("FMS Element sub group cannot be found! -> " + parentDeclaration.getName());
            }
        } else {
            log.warn("Matching cannot be performed for: " + jrxTerm.getXsdDeclaration() + " child of element: "
                    + parentDeclaration.getName());
        }
    }

    private JrxDeclaration<?> retrieveSubJrxDeclarationByName(JrxElementGroup jrxElementGroup,
            String subDeclarationName) {
        for (JrxTerm<?> jrxSubTerm : jrxElementGroup.getElements()) {
            if (jrxSubTerm instanceof JrxElement) {
                JrxElement jrxSubElement = (JrxElement) jrxSubTerm;
                if (jrxSubElement.getSimpleName().equals(subDeclarationName)) {
                    return jrxSubElement;
                }
            } else if (jrxSubTerm instanceof JrxDeclaration<?>) {
                JrxDeclaration<?> jrxSubDeclaration = (JrxDeclaration<?>) jrxSubTerm;
                if (jrxSubDeclaration.getName().equals(jrxSubDeclaration)) {
                    return jrxSubDeclaration;
                }
            } else if (jrxSubTerm instanceof JrxElementGroup) {
                JrxDeclaration<?> jrxFoundSubDeclaration = retrieveSubJrxDeclarationByName((JrxElementGroup) jrxSubTerm,
                        subDeclarationName);
                if (jrxFoundSubDeclaration != null) {
                    return jrxFoundSubDeclaration;
                }
            }
        }

        return null;
    }
}
