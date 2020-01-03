package com.github.leeonky.javabuilder;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

class BuildingContext {
    private final FactorySet factorySet;
    private List<Object> unSavedObjects = new ArrayList<>();
    private Map<PropertyChain, SupplierSpecification> propertySpecifications = new LinkedHashMap<>();

    BuildingContext(FactorySet factorySet) {
        this.factorySet = factorySet;
    }

    void cacheForSaving(Object object) {
        unSavedObjects.add(object);
    }

    void saveCachedObjects() {
        unSavedObjects.forEach(o -> factorySet.getDataRepository().save(o));
    }

    <T> BeanContext<T> createBeanContext(Factory<T> factory, Map<String, Object> params, Map<String, Object> properties,
                                         Consumer<SpecificationBuilder<T>> specifications, String[] combinations) {
        return new BeanContext<>(factorySet, factory, factorySet.getSequence(factory.getBeanClass().getType()),
                params, properties, this, null, null, specifications, combinations);
    }

    void appendPropertySpecification(PropertyChain propertyChain, SupplierSpecification propertySpecification) {
        propertySpecifications.remove(propertyChain);
        propertySpecifications.put(propertyChain, propertySpecification);
    }

    void applyAllSpecifications(Object object) {
        propertySpecifications.values().forEach(propertySpecification -> propertySpecification.apply(object));
    }
}
