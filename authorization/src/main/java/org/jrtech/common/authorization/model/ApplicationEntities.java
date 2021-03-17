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
