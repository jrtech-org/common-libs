package org.jrtech.common.authorization;

import org.jrtech.common.authorization.model.UserRole;

public class AuthorizationUtil {
	
	public static final boolean isAuthorized(String entityName, String actionKey, UserRole userRole) {
		if (userRole == null) return false;
		
		return userRole.contains(entityName, actionKey);
	}

}
