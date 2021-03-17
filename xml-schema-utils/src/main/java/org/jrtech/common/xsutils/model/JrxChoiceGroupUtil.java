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

import org.apache.log4j.Logger;

import com.sun.xml.xsom.XSComplexType;
import com.sun.xml.xsom.XSContentType;
import com.sun.xml.xsom.XSModelGroup;
import com.sun.xml.xsom.XSParticle;
import com.sun.xml.xsom.XSTerm;

public class JrxChoiceGroupUtil {
	
	private static final Logger log = Logger.getLogger(JrxChoiceGroupUtil.class);

    public static JrxElement.ChoiceDefinedLocationCategory identifyChoiceLocationCategory(JrxElement jrxElement) {
        if (jrxElement == null || jrxElement.getChildrenBlock() == null) {
            return null;
        }

        if (jrxElement.getChildrenBlock() instanceof JrxChoiceGroup) {
            return JrxElement.ChoiceDefinedLocationCategory.DIRECT;
        }

        // Indirect location
        return identifyChoiceLocationCategory(jrxElement.getChildrenBlock());
    }

    public static JrxElement.ChoiceDefinedLocationCategory identifyChoiceLocationCategory(JrxElementGroup jrxElementGroup) {
        if (jrxElementGroup == null)
            return null;

        if (jrxElementGroup instanceof JrxChoiceGroup) {
            return JrxElement.ChoiceDefinedLocationCategory.INDIRECT;
        }

        if (jrxElementGroup.getElements().size() == 1
                && jrxElementGroup.getElements().get(0) instanceof JrxElementGroup) {
            return identifyChoiceLocationCategory((JrxElementGroup) jrxElementGroup.getElements().get(0));
        }

        return null;
    }

	public static JrxChoiceGroup findScopedSingleJrxChoiceGroup(JrxElement jrxElement) {
		if (jrxElement == null || jrxElement.getChildrenBlock() == null) {
			return null;
		}
		
		if (jrxElement.getChildrenBlock() instanceof JrxChoiceGroup) {
			return (JrxChoiceGroup) jrxElement.getChildrenBlock();
		}
		
		// Indirect location
		return findScopedSingleJrxChoiceGroup(jrxElement.getChildrenBlock());
	}
	
	public static JrxChoiceGroup findScopedSingleJrxChoiceGroup(JrxElementGroup jrxElementGroup) {
		if (jrxElementGroup == null) return null;
		
		if (jrxElementGroup instanceof JrxChoiceGroup) {
			return (JrxChoiceGroup) jrxElementGroup;
		}
		
		if (jrxElementGroup.getElements().size() == 1 && jrxElementGroup.getElements().get(0) instanceof JrxElementGroup) {
			return findScopedSingleJrxChoiceGroup((JrxElementGroup) jrxElementGroup.getElements().get(0));
		}

		return null;
	}
	

	public static boolean isChoiceHelperElement(JrxElement jrxElement) {
		JrxChoiceGroup jrxChoiceGroup = findScopedSingleJrxChoiceGroup(jrxElement);
		// Direct choice helper element
		if (jrxChoiceGroup == null) {
			return false;
		}

		return isChoiceHelperElement(jrxChoiceGroup);
	}

	public static boolean isChoiceHelperElement(JrxChoiceGroup jrxChoiceGroup) {
		if (jrxChoiceGroup.getSelection() == null) {
			return true;
		}

		return false;
	}
	
	public static XSParticle getSimpleChoiceTypeParticle(XSComplexType xsComplexType) {
		XSContentType xsContentType = xsComplexType.getContentType();
		if (xsContentType instanceof XSParticle) {
			XSParticle xsContentTypeParticle = xsContentType.asParticle();
			XSTerm xsContentTypeTerm = xsContentTypeParticle.getTerm();
			if (xsContentTypeTerm.isModelGroup()) {
				XSModelGroup xsModelGroup = (XSModelGroup) xsContentTypeTerm;
				if (xsModelGroup.getSize() == 1) {
					XSParticle xsChildParticle = xsModelGroup.getChild(0);
					XSTerm xsTerm = xsChildParticle.getTerm();
					if (xsTerm instanceof XSModelGroup) {
						if (((XSModelGroup) xsTerm).getCompositor().equals(XSModelGroup.CHOICE)) {
							return xsChildParticle;
						}
					}
				}
			} else {
				log.warn("Unhandled Complex Type Term: " + xsContentTypeTerm.getClass().getSimpleName());
			}
		} else {
			// Simple type, no problem!
		}

		return null;
	}
}
