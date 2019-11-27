package com.github.leeonky.javabuilder;

import com.github.leeonky.util.BeanClass;

import java.util.HashMap;
import java.util.Map;

public class Builder<T> {
    private final Factory<T> factory;
    private final FactorySet factorySet;
    private final Map<String, Object> properties = new HashMap<>();

    public Builder(Factory<T> factory, FactorySet factorySet) {
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

    @SuppressWarnings("unchecked")
    public T build() {
        T object = factory.newInstance(new BuildContext<>(factorySet.getSequence(factory.getBeanClass().getType())));
        BeanClass<T> beanClass = (BeanClass<T>) BeanClass.create(object.getClass());
        properties.forEach((k, v) -> beanClass.setPropertyValue(object, k, v));
        return object;
    }

    public Builder<T> properties(Map<String, Object> properties) {
        Builder<T> builder = copy();
        builder.properties.putAll(properties);
        return builder;
    }
}
