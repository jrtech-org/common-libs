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

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import java.util.SortedMap;
import java.util.TreeMap;

import org.jrtech.common.authorization.model.Application;
import org.jrtech.common.authorization.model.Entity;
import org.jrtech.common.authorization.model.EntityActions;

public class AuthorizationModel implements Serializable {

    private static final long serialVersionUID = -8058333805295261461L;

    private String name;

    private Application application;

    private SortedMap<String, EntityActions> entityActionsCatalog;

    public AuthorizationModel() {
        this(null, null, null);
    }

    public AuthorizationModel(String name, Application application, Collection<EntityActions> entityActionsCollection) {
        this.name = name;
        this.application = application;
        this.entityActionsCatalog = new TreeMap<>();

        fromEntityActionsCollection(entityActionsCollection);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Application getApplication() {
        return application;
    }

    public void setApplication(Application application) {
        this.application = application;
    }

    public Collection<EntityActions> getEntityActionsCollection() {
        return Collections.unmodifiableCollection(this.entityActionsCatalog.values());
    }

    public Optional<EntityActions> getEntityActions(Entity entity) {
        if (entity == null)
            return Optional.empty();
        
        EntityActions ea = this.entityActionsCatalog.get(entity.getName());
        return ea == null ? Optional.empty() : Optional.of(ea);
    }

    public void setEntityActionsCollection(Collection<EntityActions> entityActionsCollection) {
        this.entityActionsCatalog.clear();
        fromEntityActionsCollection(entityActionsCollection);
    }

    private void fromEntityActionsCollection(Collection<EntityActions> entityActionsCollection) {
        if (entityActionsCollection != null && !entityActionsCollection.isEmpty()) {
            for (EntityActions eas : entityActionsCollection) {
                this.entityActionsCatalog.put(eas.getEntity().getName(), eas);
            }
        }
    }
}
