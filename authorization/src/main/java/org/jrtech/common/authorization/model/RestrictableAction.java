package org.jrtech.common.authorization.model;


public class RestrictableAction extends Action {

    private static final long serialVersionUID = 5748426015815439367L;

    private String restrictionDefinition;
    
    public RestrictableAction() {
        this(null);
    }

    public RestrictableAction(String name) {
        super(name);
    }

    public String getRestrictionDefinition() {
        return restrictionDefinition;
    }

    public void setRestrictionDefinition(String restrictionDefinition) {
        this.restrictionDefinition = restrictionDefinition;
    }
    
}
