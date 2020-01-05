package com.github.leeonky.javabuilder;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

class BuildingContext {
    private final FactorySet factorySet;
    private Map<PropertyChain, SupplierSpec> supplierSpecs = new LinkedHashMap<>();
    private Map<PropertyChain, DependencySpec> dependencySpecs = new LinkedHashMap<>();

    BuildingContext(FactorySet factorySet) {
        this.factorySet = factorySet;
    }

    <T> BeanContext<T> createBeanContext(Factory<T> factory, Map<String, Object> params, Map<String, Object> properties,
                                         Consumer<BeanContext<T>> spec, String[] combinations) {
        return new BeanContext<>(factorySet, factory, null, null, factorySet.getSequence(factory.getBeanClass().getType()),
                params, properties, this, spec, combinations);
    }

    void appendSupplierSpec(PropertyChain propertyChain, SupplierSpec spec) {
        supplierSpecs.remove(propertyChain);
        supplierSpecs.put(propertyChain, spec);
    }

    void applyAllSpecs(Object object) {
        supplierSpecs.values().forEach(spec -> spec.apply(object));

        Set<PropertyChain> properties = new LinkedHashSet<>(dependencySpecs.keySet());
        while (properties.size() > 0)
            assignFromDependency(object, properties, properties.iterator().next());
    }

    private void assignFromDependency(Object object, Set<PropertyChain> properties, PropertyChain property) {
        if (properties.contains(property)) {
            DependencySpec dependencySpec = dependencySpecs.get(property);
            dependencySpec.getDependencies()
                    .forEach(dependencyDependency -> assignFromDependency(object, properties, dependencyDependency));
            dependencySpec.apply(object);
            properties.remove(property);
        }
    }

    void appendDependencySpec(PropertyChain propertyChain, DependencySpec spec) {
        dependencySpecs.remove(propertyChain);
        dependencySpecs.put(propertyChain, spec);
    }
}
