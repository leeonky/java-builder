package com.github.leeonky.javabuilder.spec;

import com.github.leeonky.util.BeanClass;

import java.util.LinkedList;
import java.util.List;

public class PropertyChain {
    private final List<String> names;

    public PropertyChain(List<String> names) {
        this.names = names;
    }

    public void setTo(Object object, Object property) {
        setTo(new LinkedList<>(names), object, property);
    }

    public Object getFrom(Object object) {
        return getFrom(new LinkedList<>(names), object);
    }

    @SuppressWarnings("unchecked")
    private void setTo(LinkedList<String> properties, Object object, Object property) {
        String targetPropertyName = properties.removeLast();
        Object targetObject = getFrom(properties, object);
        BeanClass.create((Class) targetObject.getClass()).setPropertyValue(targetObject, targetPropertyName, property);
    }

    @SuppressWarnings("unchecked")
    private Object getFrom(LinkedList<String> properties, Object object) {
        if (properties.isEmpty())
            return object;
        String firstProperty = properties.removeFirst();
        return getFrom(properties, BeanClass.create((Class) object.getClass()).getPropertyValue(object, firstProperty));
    }

    public String getRootName() {
        return names.get(0);
    }

    @Override
    public int hashCode() {
        return names.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof PropertyChain)
            return names.equals(((PropertyChain) obj).names);
        return super.equals(obj);
    }
}
