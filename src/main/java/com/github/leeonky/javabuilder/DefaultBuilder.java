package com.github.leeonky.javabuilder;

import com.github.leeonky.util.BeanClass;
import com.github.leeonky.util.PropertyWriter;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

class DefaultBuilder<T> implements Builder<T> {
    private final Factory<T> factory;
    private final FactorySet factorySet;
    private Map<String, Object> params = new HashMap<>();
    private Map<String, Object> properties = new HashMap<>();

    DefaultBuilder(FactorySet factorySet, Factory<T> factory) {
        this.factory = Objects.requireNonNull(factory);
        this.factorySet = factorySet;
    }

    private DefaultBuilder<T> copy() {
        DefaultBuilder<T> newBuilder = new DefaultBuilder<>(factorySet, factory);
        newBuilder.params.putAll(params);
        return newBuilder;
    }

    @Override
    public Builder<T> params(Map<String, ?> params) {
        DefaultBuilder<T> builder = copy();
        builder.params.putAll(params);
        return builder;
    }

    @Override
    public Builder<T> properties(Map<String, ?> properties) {
        DefaultBuilder<T> builder = copy();
        builder.properties.putAll(properties);
        return builder;
    }

    @Override
    public Builder<T> property(String name, Object value) {
        Map<String, Object> map = new HashMap<>();
        map.put(name, value);
        return properties(map);
    }

    @Override
    public T build() {
        T object = factory.createObject(new BuildContext<>(factory.getSequence(),
                properties, params, factory.getBeanClass(), factorySet));
        properties.forEach((k, v) -> assignProperty(factory.getBeanClass(), object, k, v));
        factorySet.getDataRepository().save(object);
        return object;
    }

    @SuppressWarnings("unchecked")
    private void assignProperty(BeanClass<T> beanClass, T object, String name, Object value) {
        if (name.contains(".")) {
            String[] propertyList = name.split("\\.", 2);
            String propertyName = propertyList[0];
            String condition = propertyList[1];
            String factoryName = null;
            if (propertyName.contains("(")) {
                String[] propertyFactory = propertyName.split("\\(");
                propertyName = propertyFactory[0];
                factoryName = propertyFactory[1].split("\\)")[0];
            }
            PropertyWriter<T> propertyWriter = beanClass.getPropertyWriter(propertyName);
            Builder builder = factorySet.type(propertyWriter.getPropertyType(), factoryName).property(condition, value);
            propertyWriter.setValue(object, builder.query().orElseGet(builder::build));
        } else
            beanClass.setPropertyValue(object, name, value);
    }

    @Override
    public Optional<T> query() {
        return factorySet.getDataRepository().query(factory.getBeanClass(), properties);
    }
}
