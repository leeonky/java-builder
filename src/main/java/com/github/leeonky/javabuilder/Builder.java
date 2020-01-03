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
    private Consumer<SpecificationBuilder<T>> specifications = specificationBuilder -> {
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
        newBuilder.specifications = specifications;
        return newBuilder;
    }


    public Builder<T> property(String property, Object value) {
        Builder<T> builder = copy();
        builder.properties.put(property, value);
        return builder;
    }

    public Builder<T> properties(Map<String, Object> properties) {
        Builder<T> builder = copy();
        builder.properties.putAll(properties);
        return builder;
    }

    public Stream<Builder<T>> properties(Collection<Map<String, Object>> properties) {
        return properties.stream().map(this::properties);
    }

    public Builder<T> param(String paramName, Object value) {
        Builder<T> builder = copy();
        builder.params.put(paramName, value);
        return builder;
    }

    public Builder<T> specifications(Consumer<SpecificationBuilder<T>> specifications) {
        Builder<T> builder = copy();
        builder.specifications = Objects.requireNonNull(specifications);
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

    T build(BeanContext<T> beanContext) {
        T object = factory.newInstance(beanContext);
        beanContext.assignProperties(object);
        return object;
    }

    public T create() {
        BuildingContext buildingContext = new BuildingContext(factorySet);
        BeanContext<T> beanContext = buildingContext.createBeanContext(factory, params, properties, specifications, combinations);
        beanContext.collectAllSpecifications();
        T object = build(beanContext);
        beanContext.getSpecificationBuilder().applySpecifications(object);
        buildingContext.applyAllSpecifications(object);
        buildingContext.saveCachedObjects();
        return factorySet.getDataRepository().save(object);
    }

    T subCreate(BeanContext<?> beanContext, String propertyName) {
        BeanContext<T> subContext = beanContext.createSubContext(factory,
                factorySet.getSequence(factory.getBeanClass().getType()),
                params, properties, propertyName, specifications, combinations);
        subContext.collectAllSpecifications();
        return subCreate(subContext);
    }

    public BeanContext<T> createSubBeanContext(BeanContext<?> parent, String propertyName) {
        BeanContext<T> subContext = parent.createSubContext(factory,
                factorySet.getSequence(factory.getBeanClass().getType()),
                params, properties, propertyName, specifications, combinations);
        return subContext;
    }

    public T subCreate(BeanContext<T> subContext) {
        T object = build(subContext);
        subContext.getSpecificationBuilder().applySpecifications(object);
        subContext.cacheForSaving(object);
        return object;
    }
}
