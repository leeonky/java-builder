package com.github.leeonky.javabuilder;

import com.github.leeonky.util.BeanClass;

import java.util.Map;

public class BuildingContext<T> {
    private final int sequence;
    private final Map<String, Object> params, properties;
    private final Factory<T> factory;
    private final FactorySet factorySet;

    BuildingContext(int sequence, Map<String, Object> params, Map<String, Object> properties, Factory<T> factory, FactorySet factorySet) {
        this.sequence = sequence;
        this.params = params;
        this.properties = properties;
        this.factory = factory;
        this.factorySet = factorySet;
    }

    public int getSequence() {
        return sequence;
    }

    @SuppressWarnings("unchecked")
    public <T> T param(String name) {
        return (T) params.get(name);
    }

    public BeanClass<T> getBeanClass() {
        return factory.getBeanClass();
    }

    public boolean notSpecified(String name) {
        return !properties.containsKey(name);
    }

    public void setDefault(T object) {
        factorySet.getPropertyBuilder().buildDefaultProperty(object, this);
    }

    public FactorySet getFactorySet() {
        return factorySet;
    }
}
