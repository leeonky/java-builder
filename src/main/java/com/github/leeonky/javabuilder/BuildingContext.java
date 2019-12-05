package com.github.leeonky.javabuilder;

import com.github.leeonky.util.BeanClass;

import java.util.Map;

public class BuildingContext<T> {
    private final int sequence;
    private final Map<String, Object> params, properties;
    private final Factory<T> factory;
    private final FactorySet factorySet;
    private final SpecificationBuilder<T> specificationBuilder = new SpecificationBuilder<>(this);

    BuildingContext(int sequence, Map<String, Object> params, Map<String, Object> properties, Factory<T> factory, FactorySet factorySet) {
        this.sequence = sequence;
        this.params = params;
        this.properties = properties;
        this.factory = factory;
        this.factorySet = factorySet;
    }

    public int getCurrentSequence() {
        return sequence;
    }

    @SuppressWarnings("unchecked")
    public <P> P param(String name) {
        return (P) params.get(name);
    }

    public BeanClass<T> getBeanClass() {
        return factory.getBeanClass();
    }

    public boolean isNotSpecified(String name) {
        return !properties.containsKey(name);
    }

    public void assignPropertiesAsDefaultValues(T object) {
        factorySet.getPropertyBuilder().assignPropertiesAsDefaultValues(object, this);
    }

    public FactorySet getFactorySet() {
        return factorySet;
    }

    public SpecificationBuilder<T> getSpecificationBuilder() {
        return specificationBuilder;
    }
}
