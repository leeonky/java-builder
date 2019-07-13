package com.github.leeonky.javabuilder;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

class DefaultBuilder<T> implements Builder<T> {
    private final Factory<T> factory;
    private final FactoryConfiguration factoryConfiguration;
    private final DataRepository dataRepository;
    private Map<String, Object> params = new HashMap<>();
    private Map<String, Object> properties = new HashMap<>();

    DefaultBuilder(Factory<T> factory, FactoryConfiguration factoryConfiguration) {
        this.factory = Objects.requireNonNull(factory);
        this.factoryConfiguration = factoryConfiguration;
        dataRepository = factoryConfiguration.getDataRepository();
    }

    private DefaultBuilder<T> copy() {
        DefaultBuilder<T> newBuilder = new DefaultBuilder<>(factory, factoryConfiguration);
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
        T object = factory.createObject(factory.getSequence(), params);
        properties.forEach((k, v) -> factory.getBeanClass().setPropertyValue(object, k, v));
        dataRepository.save(object);
        return object;
    }

    @Override
    public Optional<T> query() {
        return dataRepository.query(factory.getBeanClass(), properties);
    }
}
