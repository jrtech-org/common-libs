package org.jrtech.common.authorization.model;

import java.io.Serializable;
import java.util.Collections;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

public class UserRole implements Serializable {

	private static final long serialVersionUID = 4078570173563952439L;
	
	private String objectId;

	private String name;

	private Map<String, Set<Action>> entityActionsCatalog;

	public UserRole() {
		entityActionsCatalog = new TreeMap<>();
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Map<String, Set<Action>> getEntityActionsCatalog() {
		return entityActionsCatalog;
	}

	public void setEntityActionsCatalog(Map<String, Set<Action>> entityActionsCatalog) {
		this.entityActionsCatalog = entityActionsCatalog;
	}

	public void addEntityActions(UserRole otherRole) {
		if (otherRole == null)
			return;

		Map<String, Set<Action>> otherActionsCatalog = otherRole.getEntityActionsCatalog();
		for (Entry<String, Set<Action>> en : otherActionsCatalog.entrySet()) {
			Set<Action> existingActions = this.entityActionsCatalog.get(en.getKey());
			if (existingActions == null) {
				existingActions = new TreeSet<>();
			}

			existingActions.addAll(en.getValue());
		}
	}

	public void addEntityActions(EntityActions entityActions) {
		if (entityActions == null)
			return;

		Set<Action> existingActions = this.entityActionsCatalog.get(entityActions.getEntity().getName());
		if (existingActions == null) {
			existingActions = new TreeSet<>();
		}

		existingActions.addAll(entityActions.getActions());
	}

	public Set<Action> getActions(Entity entity) {
		return getActions(entity.getName());
	}

	public Set<Action> getActions(String entityName) {
		if (entityName == null)
			return null;

		Set<Action> actions = entityActionsCatalog.get(entityName);
		if (actions == null) {
			return null;
		}

		return Collections.unmodifiableSet(actions);
	}

	public String getObjectId() {
		return objectId;
	}

	public void setObjectId(String objectId) {
		this.objectId = objectId;
	}
	
	public boolean contains(String entityName, String actionKey) {
		Set<Action> actions = entityActionsCatalog.get(entityName);
		if (actions == null || actions.isEmpty()) {
			return false;
		}
		
		for (Action act : actions) {
			if (act.getKey().equalsIgnoreCase(actionKey))
				return true;
		}
		
		// not found
		return false;
	}

}
