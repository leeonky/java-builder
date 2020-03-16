package com.github.leeonky.javabuilder;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Stream;

public class Builder<T> {
    final Factory<T> factory;
    final FactorySet factorySet;
    final Map<String, Object> properties = new HashMap<>();
    final Map<String, Object> params = new HashMap<>();
    String[] combinations = new String[]{};
    Consumer<BeanContext<T>> spec = beanContext -> {
    };

    Builder(Factory<T> factory, FactorySet factorySet) {
        this.factory = factory;
        this.factorySet = factorySet;
    }

    public Builder<T> copy() {
        Builder<T> newBuilder = new Builder<>(factory, factorySet);
        newBuilder.properties.putAll(properties);
        newBuilder.params.putAll(params);
        newBuilder.combinations = Arrays.copyOf(combinations, combinations.length);
        newBuilder.spec = spec;
        return newBuilder;
    }


    public Builder<T> property(String property, Object value) {
        Builder<T> builder = copy();
        builder.properties.put(property, value);
        return builder;
    }

    public Builder<T> properties(Map<String, ?> properties) {
        Builder<T> builder = copy();
        builder.properties.putAll(properties);
        return builder;
    }

    public Stream<Builder<T>> properties(Collection<Map<String, ?>> properties) {
        return properties.stream().map(this::properties);
    }

    public Builder<T> param(String paramName, Object value) {
        Builder<T> builder = copy();
        builder.params.put(paramName, value);
        return builder;
    }

    public Builder<T> spec(Consumer<BeanContext<T>> spec) {
        Builder<T> builder = copy();
        builder.spec = Objects.requireNonNull(spec);
        return builder;
    }

    public List<T> query() {
        return factorySet.getDataRepository().query(factory.getBeanClass(), properties);
    }

    public Builder<T> combine(String... combinations) {
        Builder<T> builder = copy();
        builder.combinations = Objects.requireNonNull(combinations);
        return builder;
    }

    public T create() {
        BuildingContext buildingContext = new BuildingContext(factorySet);
        T object = new BeanContextImpl<>(buildingContext, null, null, this).build();
        buildingContext.submitCached(object);
        factorySet.getDataRepository().save(object);
        return object;
    }

    public T build() {
        return new BeanContextImpl<>(new BuildingContext(factorySet), null, null, this).build();
    }
}
