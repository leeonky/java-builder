package com.github.leeonky.javabuilder;

import java.util.HashMap;
import java.util.Map;

public class Builder<T> {
    private final Factory<T> factory;
    private final FactorySet factorySet;
    private final Map<String, Object> properties = new HashMap<>();
    private final Map<String, Object> params = new HashMap<>();

    Builder(Factory<T> factory, FactorySet factorySet) {
        this.factory = factory;
        this.factorySet = factorySet;
    }

    private Builder<T> copy() {
        Builder<T> newBuilder = new Builder<>(factory, factorySet);
        newBuilder.properties.putAll(properties);
        return newBuilder;
    }


    public Builder<T> property(String property, Object value) {
        Builder<T> builder = copy();
        builder.properties.put(property, value);
        return builder;
    }

    public T build() {
        BuildingContext<T> buildingContext = new BuildingContext<>(factorySet.getSequence(factory.getBeanClass().getType()),
                params, properties, factory, factorySet);
        T object = factory.newInstance(buildingContext);
        properties.forEach((k, v) -> factory.getBeanClass().setPropertyValue(object, k, v));
        return factory.postProcess(buildingContext, object);
    }

    public Builder<T> properties(Map<String, Object> properties) {
        Builder<T> builder = copy();
        builder.properties.putAll(properties);
        return builder;
    }

    public Builder<T> param(String paramName, Object value) {
        Builder<T> builder = copy();
        builder.params.put(paramName, value);
        return builder;
    }
}
