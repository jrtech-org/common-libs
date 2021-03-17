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
package org.jrtech.common.utils;



import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/************************************************************************
 * The class <code>ObjectPropertyUtil</code> defines a set of tools via
 * Java Reflection
 *************************************************************************/
public class ObjectPropertyUtil {

    private static final Logger log = LoggerFactory.getLogger(ObjectPropertyUtil.class);

    public static final String RETRIEVING_METHOD_DEFAULT = "default";

    public static final String RETRIEVING_METHOD_MAP = "RetrieveMap";

    public static final String RETRIEVING_METHOD_EXTENDED_PROPERTY = "getExtendedPropertyValue";

    private MethodPathParser methodPathParser = new MethodPathParser();

    public static ObjectPropertyUtil getInstance() {
        return new ObjectPropertyUtil();
    }

    public String setMethodNameFromAttributeName(String attributeName) {
        if (attributeName == null) {
            return null;
        }
        return "set" + (attributeName.length() > 0 ? attributeName.substring(0, 1).toUpperCase() : "")
                + (attributeName.length() > 1 ? attributeName.substring(1) : "");
    }

    public String getMethodNameFromAttributeName(String attributeName) {
        if (attributeName == null) {
            return null;
        }
        return "get" + (attributeName.length() > 0 ? attributeName.substring(0, 1).toUpperCase() : "")
                + (attributeName.length() > 1 ? attributeName.substring(1) : "");
    }

    public String getBooleanMethodNameFromAttributeName(String attributeName) {
        if (attributeName == null) {
            return null;
        }
        return "is" + (attributeName.length() > 0 ? attributeName.substring(0, 1).toUpperCase() : "")
                + (attributeName.length() > 1 ? attributeName.substring(1) : "");
    }

    public Method getMethodFromAttributeName(Object object, String attributeName) {
        String booleanMethodName = getBooleanMethodNameFromAttributeName(attributeName);
        String getterMethodName = getMethodNameFromAttributeName(attributeName);
        for (Method method : object.getClass().getMethods()) {
            if (method.getName().equals(getterMethodName) || method.getName().equals(booleanMethodName)) {
                return method;
            }
        }
        return null;
    }

    public Field getFieldFromAttributeName(Object object, String attributeName) {
        for (Field field : object.getClass().getFields()) {
            if (field.getName().equals(attributeName)) {
                return field;
            }
        }
        return null;
    }

    public Object setPropertyValue(Object object, String attributeName, Object value) {
        if (object == null)
            return null;
        try {
            String methodName = setMethodNameFromAttributeName(attributeName);
            Method method = null;
            if (value != null) {
                method = object.getClass().getMethod(methodName,
                        new Class[] { value.getClass() == Integer.class ? int.class : value.getClass() });
            } else {
                Method[] methods = object.getClass().getMethods();
                for (Method m : methods) {
                    if (m.getName().equals(methodName)) {
                        method = m;
                        break;
                    }
                }
            }

            if (method == null) {
                log.info("Cannot set property value from object type: '" + object.getClass() + "', property: "
                        + attributeName + " because method: '" + methodName + "' cannot be found on object.");
                return object; // skip
            }

            method.invoke(object, new Object[] { value });
            return object;
        } catch (Exception e) {
            log.info("Cannot set property value from object: '" + object.getClass() + "', property: " + attributeName,
                    e);
        }
        return object;
    }

    public Object getPropertyValue(Object object, String attributeName) {
        if (object == null)
            return null;
        try {
            Method method = getMethodFromAttributeName(object, attributeName);
            if (method != null) {
                return method.invoke(object, new Object[0]);
            }
            Field field = getFieldFromAttributeName(object, attributeName);
            if (field != null) {
                return field.get(object);
            }
            log.info("Invalid property name for object type: '" + object.getClass() + "', property: " + attributeName);
        } catch (Exception e) {
            log.info("Cannot get property value from object type: '" + object.getClass() + "', property: "
                    + attributeName, e);
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    public Object getPropertyValue(Object object, String attributeName, String retrievingMethod)
            throws InvalidMethodPathExpression {
        if (object == null)
            return null;

        if (retrievingMethod == null || retrievingMethod.equals("")
                || retrievingMethod.equalsIgnoreCase(RETRIEVING_METHOD_DEFAULT)) {
            if (attributeName.contains("/") || attributeName.contains(".")) {
                return getPropertyValueFromMethodPath(object, attributeName);
            } else {
                return getPropertyValue(object, attributeName);
            }
        }

        if (retrievingMethod.equalsIgnoreCase(RETRIEVING_METHOD_MAP)) {
            String methodPath = attributeName.substring(0, attributeName.indexOf('@'));
            String attributeKey = attributeName.substring(attributeName.indexOf('@') + 1);
            Map<String, Object> map = (Map<String, Object>) getPropertyValueFromMethodPath(object, methodPath);
            return map.get(attributeKey);
        }

        return getExtendedPropertyValue(object, attributeName, retrievingMethod);
    }

    @SuppressWarnings("unchecked")
    public Object setPropertyValue(Object object, String attributeName, String retrievingMethod, Object value) {
        if (object == null)
            return null;
        try {
            if (retrievingMethod == null || retrievingMethod.equals("")
                    || retrievingMethod.equalsIgnoreCase(RETRIEVING_METHOD_DEFAULT)) {
                if (attributeName.indexOf('/') >= 0) {
                    return object;
                    // return getPropertyValueFromMethodPath(object, attributeName);
                } else {
                    return setPropertyValue(object, attributeName, value);
                }
            }

            if (retrievingMethod.equalsIgnoreCase(RETRIEVING_METHOD_MAP)) {
                String methodPath = attributeName.substring(0, attributeName.indexOf("@"));
                String attributeKey = attributeName.substring(attributeName.indexOf("@") + 1);
                Map<String, Object> map = (Map<String, Object>) getPropertyValueFromMethodPath(object, methodPath);
                return map.put(attributeKey, value);
            }

            String setterMethod = retrievingMethod;
            if (setterMethod.startsWith("get")) {
                setterMethod = "set" + setterMethod.substring(3);
            }
            return setExtendedPropertyValue(object, attributeName, setterMethod, value);
        } catch (Exception e) {
            if (e instanceof RuntimeException) {
                throw (RuntimeException) e;
            } else {
                throw new RuntimeException(e);
            }
        }
    }

    public Object getPropertyValueFromMethodPath(Object object, String attributeName)
            throws InvalidMethodPathExpression {
        String methodPathExpression = attributeName;
        String methodName = methodPathParser.getMethodName(methodPathExpression);
        if (methodName.equals("")) {
            return null;
        }

        Object methodValue = getPropertyValue(object, methodName);

        if (methodValue != null) {
            methodPathExpression = methodPathParser.chopMethodNameFromMethodPathExpression(methodPathExpression,
                    methodName);
            if (!methodPathExpression.equals("")) {
                if (methodValue != null) {
                    if (methodValue instanceof Collection<?>) {
                        Collection<?> itemList = (Collection<?>) methodValue;
                        Iterator<?> it = itemList.iterator();
                        if (it.hasNext()) {
                            Object value = it.next();
                            methodValue = getPropertyValueFromMethodPath(value, methodPathExpression);
                        } else {
                            methodValue = null;
                        }
                    } else {
                        methodValue = getPropertyValueFromMethodPath(methodValue, methodPathExpression);
                    }
                }
            }
        }

        return methodValue;
    }

    public List<Object> getPropertyValuesFromMethodPath(Object object, String attributeName)
            throws InvalidMethodPathExpression {
        String methodPathExpression = attributeName;
        String methodName = methodPathParser.getMethodName(methodPathExpression);
        if (methodName.equals("")) {
            return null;
        }

        List<Object> values = null;
        Object methodValue = getPropertyValue(object, methodName);

        methodPathExpression = methodPathParser
                .chopMethodNameFromMethodPathExpression(methodPathExpression, methodName);
        if (!methodPathExpression.equals("")) {
            if (methodValue != null) {
                values = new ArrayList<Object>();
                if (methodValue instanceof Collection) {
                    Collection<?> itemList = (Collection<?>) methodValue;
                    for (Iterator<?> it = itemList.iterator(); it.hasNext();) {
                        Object value = it.next();
                        Object tempStorage = getPropertyValuesFromMethodPath(value, methodPathExpression);
                        if (tempStorage instanceof Collection) {
                            values.addAll((Collection<?>) tempStorage);
                        } else if (tempStorage != null) {
                            values.add(tempStorage);
                        }
                    }
                } else if (methodValue != null) {
                    Object tempStorage = getPropertyValuesFromMethodPath(methodValue, methodPathExpression);
                    if (tempStorage instanceof Collection) {
                        values.addAll((Collection<?>) tempStorage);
                    } else if (tempStorage != null) {
                        values.add(tempStorage);
                    }
                }
            }
        } else {
            if (methodValue != null) {
                if (values == null) {
                    values = new ArrayList<Object>();
                }
                values.add(methodValue);
            }
        }

        return values;
    }

    public Object getExtendedPropertyValue(Object object, String attributeName, String getterName) {
        Class<?> clazz = object.getClass();
        try {
            Method m = clazz.getMethod(getterName, String.class);
            return m.invoke(object, attributeName);
        } catch (SecurityException e) {
            log.info("Fail in getting extended property for object type: '" + object.getClass() + "' attribute: '"
                    + attributeName + "' getterName: '" + getterName + "'", e);
        } catch (NoSuchMethodException e) {
            log.info("Fail in getting extended property for object type: '" + object.getClass() + "' attribute: '"
                    + attributeName + "' getterName: '" + getterName + "'", e);
        } catch (IllegalArgumentException e) {
            log.info("Fail in getting extended property for object type: '" + object.getClass() + "' attribute: '"
                    + attributeName + "' getterName: '" + getterName + "'", e);
        } catch (IllegalAccessException e) {
            log.info("Fail in getting extended property for object type: '" + object.getClass() + "' attribute: '"
                    + attributeName + "' getterName: '" + getterName + "'", e);
        } catch (InvocationTargetException e) {
            log.info("Fail in getting extended property for object type: '" + object.getClass() + "' attribute: '"
                    + attributeName + "' getterName: '" + getterName + "'", e);
        }

        return null;
    }

    public Object setExtendedPropertyValue(Object object, String attributeName, String setterName, Object value)
            throws NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException,
            InvocationTargetException {
        Class<?> clazz = object.getClass();
        Method m = clazz.getMethod(setterName, new Class[] { attributeName.getClass(), Object.class });
        return m.invoke(object, attributeName, value);
    }

}
