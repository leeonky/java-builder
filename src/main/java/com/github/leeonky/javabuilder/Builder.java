package com.github.leeonky.javabuilder;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Stream;

public class Builder<T> {
    private final Factory<T> factory;
    private final FactorySet factorySet;
    private final Map<String, Object> properties = new HashMap<>();
    private final Map<String, Object> params = new HashMap<>();
    private String[] combinations = new String[]{};
    private Consumer<BeanContext<T>> spec = beanContext -> {
    };

    Builder(Factory<T> factory, FactorySet factorySet) {
        this.factory = factory;
        this.factorySet = factorySet;
    }

    private Builder<T> copy() {
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
        T object = build();
        factorySet.getDataRepository().save(object);
        return object;
    }

    public T build() {
        BuildingContext buildingContext = new BuildingContext(factorySet);
        BeanContext<T> beanContext = buildingContext.createBeanContext(factory, params, properties, spec, combinations);
        beanContext.queryOrCreateReferenceBeansAndCollectAllSpecs();
        T object = build(beanContext);
        buildingContext.applyAllSpecsAndSaveCached(object, beanContext);
        return object;
    }

    T build(BuildingContext buildingContext) {
        BeanContext<T> beanContext = buildingContext.createBeanContext(factory, params, properties, spec, combinations);
        beanContext.queryOrCreateReferenceBeansAndCollectAllSpecs();
        return build(beanContext);
    }

    private T build(BeanContext<T> beanContext) {
        return beanContext.assignProperties(factory.newInstance(beanContext));
    }

    BeanContext<T> createSubBeanContext(BeanContext<?> parent, String propertyName) {
        return parent.createSubContext(factory,
                propertyName, factorySet.getSequence(factory.getBeanClass().getType()),
                params, properties, spec, combinations);
    }

    T subCreate(BeanContext<T> subContext) {
        T object = build(subContext);
        subContext.cacheSave(object);
        return object;
    }
}
