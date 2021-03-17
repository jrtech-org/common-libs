package org.jrtech.common.authorization.model;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.SortedSet;
import java.util.TreeSet;

public class Entity implements Serializable, Comparable<Entity> {

    private static final long serialVersionUID = 3466653707163509779L;

    private String name;
    
    private SortedSet<Attribute> attributes;
    
    private Entity parent;

    public Entity() {
        this(null, null);
    }

    public Entity(String name) {
        this(name, null, null);
    }

    public Entity(String name, Collection<Attribute> attributes) {
        this(name, null, attributes);
    }

    public Entity(String name, Entity parent, Collection<Attribute> attributes) {
        this.name = name;
        this.parent = parent;
        this.attributes = new TreeSet<>();
        fromAttributes(attributes);
    }
    
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Collection<Attribute> getAttributes() {
        return Collections.unmodifiableCollection(attributes);
    }

    public void setAttributes(Collection<Attribute> attributes) {
        this.attributes.clear();
        fromAttributes(attributes);
    }

    public Entity getParent() {
        return parent;
    }

    public void setParent(Entity parent) {
        this.parent = parent;
    }
    
    private void fromAttributes(Collection<Attribute> attributes) {
        if (attributes != null && !attributes.isEmpty()) {
            this.attributes.addAll(attributes);
        }
    }

    @Override
    public int compareTo(Entity o) {
        // 1st -> Compare parent
        if (parent != null && o.parent != null) {
            int parentCompareResult = parent.compareTo(o.parent); 
            if (parentCompareResult != 0) {
                return parentCompareResult;
            }
        } else if (parent == null && o.parent != null) {
            return -1;
        } else if (parent != null && o.parent == null) {
            return 1;
        }
        
        // 2nd -> Compare name
        if (name == null && o.name != null) {
            return -1;
        } else if (name != null && o.name == null) {
            return 1;
        }
        
        return name.compareTo(o.name);
    }
}
