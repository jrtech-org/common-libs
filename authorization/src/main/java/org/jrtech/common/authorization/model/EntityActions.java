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
