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
package org.jrtech.common.authorization;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.text.MessageFormat;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentNavigableMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.function.BiConsumer;
import java.util.function.Predicate;

import org.jrtech.common.authorization.model.Attribute;
import org.jrtech.common.authorization.model.Entity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BaseEntityCatalog {

    private static final Logger LOGGER = LoggerFactory.getLogger(BaseEntityCatalog.class);

    private static final ConcurrentNavigableMap<String, AuthorizationModel> AUTHORIZATION_MODEL_CATALOG = new ConcurrentSkipListMap<>();

    private static final ConcurrentNavigableMap<String, Entity> ENTITY_CATALOG = new ConcurrentSkipListMap<>();

    private static final ConcurrentNavigableMap<String, Attribute> ATTRIBUTE_CATALOG = new ConcurrentSkipListMap<>();

    public static final Map<String, AuthorizationModel> getAuthorizationModelCatalog() {
        if (AUTHORIZATION_MODEL_CATALOG.isEmpty()) {
            AUTHORIZATION_MODEL_CATALOG.putAll(new ReflectionConstantValueCollector<String, AuthorizationModel>()
                    .collect(BaseSecurityConstants.class.getFields(), "AuthorizationModel", field -> field.getName()
                            .endsWith("_AUTHORIZATION_MODEL"), (objectValue, catalog) -> {
                        AuthorizationModel model = (AuthorizationModel) objectValue;
                        catalog.put(model.getApplication().getName(), model);
                    }));
        }

        return Collections.unmodifiableNavigableMap(AUTHORIZATION_MODEL_CATALOG);
    }

    public static final Map<String, Entity> getEntityCatalog() {
        if (ENTITY_CATALOG.isEmpty()) {
            ENTITY_CATALOG.putAll(new ReflectionConstantValueCollector<String, Entity>().collect(
                    BaseSecurityConstants.class.getFields(), "Entity", field -> field.getName().startsWith("ENT_")
                            && !field.getName().startsWith("ENT_ACTS_"), (objectValue, catalog) -> {
                        Entity entity = (Entity) objectValue;
                        catalog.put(entity.getName(), entity);
                    }));
        }
        return Collections.unmodifiableNavigableMap(ENTITY_CATALOG);
    }

    public static final Map<String, Attribute> getAttributeCatalog() {
        if (ATTRIBUTE_CATALOG.isEmpty()) {
            ATTRIBUTE_CATALOG.putAll(new ReflectionConstantValueCollector<String, Attribute>().collect(
                    BaseSecurityConstants.class.getFields(), "Attribute", field -> field.getName().startsWith("ATTR_"),
                    (objectValue, catalog) -> {
                        Attribute attribute = (Attribute) objectValue;
                        catalog.put(attribute.getName(), attribute);
                    }));
        }
        return Collections.unmodifiableNavigableMap(ATTRIBUTE_CATALOG);
    }

    static class ReflectionConstantValueCollector<K, V> {

        public Map<K, V> collect(Field[] fields, String entityName, Predicate<Field> checkField,
                BiConsumer<Object, Map<K, V>> consumer) {
            Map<K, V> result = new LinkedHashMap<>();
            for (Field field : BaseSecurityConstants.class.getFields()) {
                if (Modifier.isPublic(field.getModifiers()) && Modifier.isStatic(field.getModifiers())
                        && Modifier.isFinal(field.getModifiers())) {
                    // public static final
                    if (checkField.test(field)) {
                        try {
                            boolean accessible = field.isAccessible();
                            field.setAccessible(true);
                            Object objValue = field.get(null);
                            consumer.accept(objValue, result);
                            if (!accessible) {
                                field.setAccessible(false);
                            }
                        } catch (Exception e) {
                            LOGGER.error(
                                    MessageFormat.format("Failure initializing the catalog of [{0}].", entityName), e);
                        }
                    }
                }
            }

            return result;
        }
    }

}
