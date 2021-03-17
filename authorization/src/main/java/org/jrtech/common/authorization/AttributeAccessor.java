package org.jrtech.common.authorization;

public interface AttributeAccessor<T> {

    public <V> V getAttributeValue(T object, String attributeName);
    
}
