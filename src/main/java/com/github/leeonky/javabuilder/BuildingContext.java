package com.github.leeonky.javabuilder;

import com.github.leeonky.javabuilder.spec.*;

import java.util.*;
import java.util.function.Consumer;

public class BuildingContext {
    private final FactorySet factorySet;
    private final ObjectTree objectTree = new ObjectTree();
    private final List<LinkSpec> linkSpecs = new ArrayList<>();
    private final Map<PropertyChain, PropertySpec> propertiesSpecs = new LinkedHashMap<>();
    private final Map<PropertyChain, SupplierSpec> supplierSpecs = new LinkedHashMap<>();
    private final Map<PropertyChain, DependencySpec> dependencySpecs = new LinkedHashMap<>();

    public BuildingContext(FactorySet factorySet) {
        this.factorySet = factorySet;
    }

    public <T> BeanContext<T> createBeanContext(Factory<T> factory, Map<String, Object> params, Map<String, Object> properties,
                                                Consumer<BeanContext<T>> spec, String[] combinations) {
        return new BeanContext<>(factorySet, factory, null, null, factorySet.getSequence(factory.getBeanClass().getType()),
                params, properties, this, spec, combinations);
    }

    public void appendSupplierSpec(PropertyChain propertyChain, SupplierSpec spec) {
        if (dependencySpecs.containsKey(propertyChain))
            return;
        supplierSpecs.remove(propertyChain);
        supplierSpecs.put(propertyChain, spec);
    }

    public void appendPropertiesSpec(PropertyChain propertyChain, PropertySpec spec) {
        propertiesSpecs.remove(propertyChain);
        propertiesSpecs.put(propertyChain, spec);
    }

    public void applyAllSpecsAndSaveCached(Object object, BeanContext<?> beanContext) {
        linkSpecs.forEach(linkSpec -> linkSpec.preApply(beanContext));

        mergePropertySpecs(object);

        supplierSpecs.values().forEach(spec -> spec.apply(object));

        Set<PropertyChain> properties = new LinkedHashSet<>(dependencySpecs.keySet());
        while (properties.size() > 0)
            assignFromDependency(object, properties, properties.iterator().next());

        linkSpecs.forEach(linkSpec -> linkSpec.apply(object, beanContext));

        objectTree.foreach(object, o -> factorySet.getDataRepository().save(o));
    }

    private void mergePropertySpecs(Object object) {
        ArrayList<PropertySpec> propertiesSpecList = new ArrayList<>(propertiesSpecs.values());
        for (int i = 0; i < propertiesSpecList.size(); ++i)
            for (int j = i + 1; j < propertiesSpecList.size(); ++j)
                propertiesSpecList.get(j).tryMerge(propertiesSpecList.get(i));
        propertiesSpecList.forEach(spec -> spec.apply(object));
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

    public void appendDependencySpec(PropertyChain propertyChain, DependencySpec spec) {
        supplierSpecs.remove(propertyChain);
        dependencySpecs.remove(propertyChain);
        dependencySpecs.put(propertyChain, spec);
    }

    public void cacheSave(Object parent, Object node) {
        objectTree.addNode(parent, node);
    }

    public void appendLinkSpec(LinkSpec linkSpec) {
        linkSpecs.add(linkSpec);
    }

    public boolean isSupplierSpec(PropertyChain propertyChain) {
        return supplierSpecs.containsKey(propertyChain);
    }

    public void removeSupplierSpec(PropertyChain propertyChain) {
        supplierSpecs.remove(propertyChain);
    }
}
