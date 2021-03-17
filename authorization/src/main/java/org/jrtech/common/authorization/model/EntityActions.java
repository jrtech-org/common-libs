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

public class EntityActions implements Cloneable, Serializable {

    private static final long serialVersionUID = 159343537530414855L;
    
    private Entity entity;
    
    private SortedSet<Action> actions;

    public EntityActions() {
        this(null, null);
    }
    
    public EntityActions(Entity entity, Collection<Action> actions) {
        this.entity = entity;
        
        this.actions = new TreeSet<>();
        addActions(actions);
    }
    
    public Entity getEntity() {
        return entity;
    }
    
    public void setEntity(Entity entity) {
        this.entity = entity;
    }
    
    public Collection<Action> getActions() {
        return Collections.unmodifiableCollection(this.actions);
    }

    public void setActions(Collection<Action> actions) {
        this.actions.clear();
        addActions(actions);
    }
    
    public void addActions(Collection<Action> actions) {
        if (actions != null && !actions.isEmpty()) {
            this.actions.addAll(actions);
        }
    }
    
}
