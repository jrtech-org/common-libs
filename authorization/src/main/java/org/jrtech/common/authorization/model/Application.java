package org.jrtech.common.authorization.model;

import java.io.Serializable;

public class Application implements Cloneable, Serializable {

    private static final long serialVersionUID = -3352705105637973436L;

    private String name;
    
    private String displayName;
    
    public Application() {
    }
    
    public Application(String name) {
        this(name, name);
    }
    
    public Application(String name, String displayName) {
        this.name = name;
        this.displayName = displayName;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }
}
