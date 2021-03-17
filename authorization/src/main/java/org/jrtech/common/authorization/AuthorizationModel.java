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
