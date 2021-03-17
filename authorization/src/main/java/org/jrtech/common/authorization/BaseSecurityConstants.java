package org.jrtech.common.authorization;

import java.util.Arrays;

import org.jrtech.common.authorization.model.Application;
import org.jrtech.common.authorization.model.Attribute;
import org.jrtech.common.authorization.model.Entity;
import org.jrtech.common.authorization.model.EntityActions;
import org.jrtech.common.authorization.model.Action;
import org.jrtech.common.authorization.model.ApplicationEntities;

public class BaseSecurityConstants {

    // Action
    public static final Action ACT_APPROVE = new Action("approve", "Approve");
    public static final Action ACT_CREATE = new Action("create", "Create");
    public static final Action ACT_DENY = new Action("deny", "Deny");
    public static final Action ACT_DELETE = new Action("delete", "Delete");
    public static final Action ACT_SAVE = new Action("save", "Save");
    public static final Action ACT_SEARCH = new Action("search", "Search");
    public static final Action ACT_VIEW = new Action("view", "View");

    // Application
    public static final Application APP_SECURITY = new Application("Security");

    // Attribute
    public static final Attribute ATTR_NAME = new Attribute("Name", String.class.getSimpleName());
    public static final Attribute ATTR_OWNER = new Attribute("Owner", String.class.getSimpleName());

    // Entity
    public static final Entity ENT_ORGANIZATION = new Entity("Organization", Arrays.asList(new Attribute[] { ATTR_NAME,
            ATTR_OWNER }));
    public static final Entity ENT_ROLE = new Entity("Role", Arrays.asList(new Attribute[] { ATTR_NAME, ATTR_OWNER }));
    public static final Entity ENT_ROLE_GROUP = new Entity("Role Group", Arrays.asList(new Attribute[] { ATTR_NAME,
            ATTR_OWNER }));
    public static final Entity ENT_USER = new Entity("User", Arrays.asList(new Attribute[] { ATTR_NAME, ATTR_OWNER }));

    // Entity Actions
    public static final EntityActions ENT_ACTS_GROUP_ORGANIZATION = new EntityActions(ENT_ORGANIZATION,
            Arrays.asList(new Action[] {
// @formatter:off
                    ACT_APPROVE,
                    ACT_CREATE,
                    ACT_DENY,
                    ACT_DELETE,
                    ACT_SAVE,
                    ACT_SEARCH,
                    ACT_VIEW
                    // @formatter:on
            }));
    public static final EntityActions ENT_ACTS_GROUP_ROLE = new EntityActions(ENT_ROLE, Arrays.asList(new Action[] {
// @formatter:off
                    ACT_APPROVE,
                    ACT_CREATE,
                    ACT_DENY,
                    ACT_DELETE,
                    ACT_SAVE,
                    ACT_SEARCH,
                    ACT_VIEW
                    // @formatter:on
            }));
    public static final EntityActions ENT_ACTS_GROUP_ROLE_GROUP = new EntityActions(ENT_ROLE_GROUP,
            Arrays.asList(new Action[] {
// @formatter:off
                    ACT_APPROVE,
                    ACT_CREATE,
                    ACT_DENY,
                    ACT_DELETE,
                    ACT_SAVE,
                    ACT_SEARCH,
                    ACT_VIEW
                    // @formatter:on
            }));
    public static final EntityActions ENT_ACTS_GROUP_USER = new EntityActions(ENT_USER, Arrays.asList(new Action[] {
// @formatter:off
                    ACT_APPROVE,
                    ACT_CREATE,
                    ACT_DENY,
                    ACT_DELETE,
                    ACT_SAVE,
                    ACT_SEARCH,
                    ACT_VIEW
                    // @formatter:on
            }));

    // Application Entities
    public static final ApplicationEntities SECURITY_ENTITIES = new ApplicationEntities(APP_SECURITY,
            Arrays.asList(new Entity[] { ENT_ORGANIZATION, ENT_ROLE, ENT_ROLE_GROUP, ENT_USER }));

    // Authorization Model
    public static final AuthorizationModel SECURITY_AUTHORIZATION_MODEL = new AuthorizationModel(
            "Security Authorization Model", APP_SECURITY, Arrays.asList(new EntityActions[] {
// @formatter:off 
                    ENT_ACTS_GROUP_ORGANIZATION, 
                    ENT_ACTS_GROUP_ROLE, 
                    ENT_ACTS_GROUP_ROLE_GROUP, 
                    ENT_ACTS_GROUP_USER
                    // @formatter:on
                    }));

}
