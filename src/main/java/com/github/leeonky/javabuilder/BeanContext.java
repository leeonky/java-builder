package com.github.leeonky.javabuilder;

import com.github.leeonky.util.BeanClass;
import com.github.leeonky.util.PropertyWriter;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class BeanContext<T> {
    private final Factory<T> factory;
    private final FactorySet factorySet;
    private final int sequence;
    private final Map<String, Object> params;
    private final Map<String, Object> properties = new LinkedHashMap<>(), originalProperties;
    private final Consumer<SpecificationBuilder<T>> specifications;
    private final String[] combinations;
    private final BuildingContext buildingContext;
    private final SpecificationBuilder<T> specificationBuilder = new SpecificationBuilder<>(this);
    private final BeanContext<?> parent;
    private final String currentPropertyName;

    BeanContext(FactorySet factorySet, Factory<T> factory, int sequence, Map<String, Object> params,
                Map<String, Object> properties, BuildingContext buildingContext, BeanContext<?> parent, String currentPropertyName,
                Consumer<SpecificationBuilder<T>> specifications, String[] combinations) {
        this.sequence = sequence;
        this.params = new LinkedHashMap<>(params);
        this.factory = factory;
        this.factorySet = factorySet;
        this.buildingContext = buildingContext;
        this.parent = parent;
        this.currentPropertyName = currentPropertyName;
        this.specifications = specifications;
        this.combinations = combinations;
        originalProperties = new LinkedHashMap<>(properties);
    }

    void queryOrCreateReferenceBeans() {
        originalProperties.forEach((k, v) -> {
            if (k.contains(".")) {
                PropertyQueryChain propertyQueryChain = PropertyQueryChain.parse(k);
                PropertyWriter<T> propertyWriter = factory.getBeanClass().getPropertyWriter(propertyQueryChain.getBaseName());
                Builder<?> builder = propertyQueryChain.toBuilder(factorySet, propertyWriter.getPropertyType(), v);
                Optional<?> queried = builder.query().stream().findFirst();
                queried.ifPresent(o -> properties.put(propertyWriter.getName(), o));
                if (!queried.isPresent())
                    specificationBuilder.property(propertyWriter.getName()).from(builder);
            } else
                properties.put(k, v);
        });
    }

    void collectAllSpecifications() {
        factory.collectSpecifications(this, combinations);
        collectSpecifications(specifications);
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

    boolean isPropertyNotSpecified(String name) {
        return !properties.containsKey(name);
    }

    void assignDefaultValueToUnSpecifiedProperties(T object) {
        factorySet.getPropertyBuilder().assignDefaultValueToProperties(object, this);
    }

    public FactorySet getFactorySet() {
        return factorySet;
    }

    public SpecificationBuilder<T> getSpecificationBuilder() {
        return specificationBuilder;
    }

    void assignProperties(T instance) {
        properties.forEach((k, v) -> factory.getBeanClass().setPropertyValue(instance, k, v));
    }

    void collectSpecifications(Consumer<SpecificationBuilder<T>> specifications) {
        specifications.accept(specificationBuilder);
    }

    <T> BeanContext<T> createSubContext(Factory<T> factory, int sequence, Map<String, Object> params, Map<String, Object> properties,
                                        String propertyName, Consumer<SpecificationBuilder<T>> specifications, String[] combinations) {
        return new BeanContext<>(factorySet, factory, sequence, params, properties, buildingContext, this, propertyName, specifications, combinations);
    }

    void cacheForSaving(T object) {
        buildingContext.cacheForSaving(object);
    }

    <E> void appendValueSpecification(String property, Supplier<E> supplier) {
        if (isPropertyNotSpecified(property)) {
            PropertyChain propertyChain = new PropertyChain(propertyChain(property));
            buildingContext.appendPropertySpecification(propertyChain, new SupplierSpecification(propertyChain, supplier));
        }
    }

    private List<String> propertyChain(String property) {
        List<String> chain = collectPropertyChain(parent);
        chain.add(property);
        return chain;
    }

    private List<String> collectPropertyChain(BeanContext<?> beanContext) {
        if (beanContext == null)
            return new ArrayList<>();
        List<String> chain = collectPropertyChain(beanContext.parent);
        chain.add(currentPropertyName);
        return chain;
    }

}
