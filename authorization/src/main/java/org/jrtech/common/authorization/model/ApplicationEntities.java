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
package org.jrtech.common.authorization.model;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.SortedSet;
import java.util.TreeSet;

public class ApplicationEntities implements Cloneable, Serializable {

    private static final long serialVersionUID = -6836297309021861572L;

    private Application application;

    private SortedSet<Entity> entities;

    public ApplicationEntities() {
        this(null, null);
    }

    public ApplicationEntities(Application application, Collection<Entity> entities) {
        this.application = application;
        this.entities = new TreeSet<>();
        fromEntities(entities);
    }

    public Application getApplication() {
        return application;
    }

    public void setApplication(Application application) {
        this.application = application;
    }

    public Collection<Entity> getEntities() {
        return Collections.unmodifiableSet(entities);
    }

    public void setEntities(Collection<Entity> entities) {
        this.entities.clear();
        fromEntities(entities);
    }

    private void fromEntities(Collection<Entity> entities) {
        if (entities != null && !entities.isEmpty()) {
            this.entities.addAll(entities);
        }
    }
}
