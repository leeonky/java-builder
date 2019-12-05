package com.github.leeonky.javabuilder;

import com.github.leeonky.util.PropertyWriter;

import java.util.*;
import java.util.function.Consumer;

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

    public T build() {
        Map<String, Object> processedProperties = processReferenceProperties();
        BuildingContext<T> buildingContext = new BuildingContext<>(factorySet.getSequence(factory.getBeanClass().getType()),
                params, processedProperties, factory, factorySet);
        T object = factory.newInstance(buildingContext);
        processedProperties.forEach((k, v) -> factory.getBeanClass().setPropertyValue(object, k, v));
        factory.getSpecifications().accept(buildingContext.getSpecificationBuilder());
        factory.combine(buildingContext, combinations);
        specifications.accept(buildingContext.getSpecificationBuilder());
        buildingContext.getSpecificationBuilder().collectSpecifications().forEach(spec -> spec.apply(object));
        return factorySet.getDataRepository().save(object);
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> processReferenceProperties() {
        Map<String, Object> processedProperties = new HashMap<>();
        properties.forEach((k, v) -> {
            if (k.contains(".")) {
                PropertyChain propertyChain = PropertyChain.parse(k);
                PropertyWriter<T> propertyWriter = factory.getBeanClass().getPropertyWriter(propertyChain.getName());
                Builder builder = propertyChain.toBuilder(factorySet, propertyWriter.getPropertyType(), v);
                processedProperties.put(propertyWriter.getName(), builder.query().stream().findFirst().orElseGet(builder::build));
            } else
                processedProperties.put(k, v);
        });
        return processedProperties;
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
}
