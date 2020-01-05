package com.github.leeonky.javabuilder;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

class DependencySpec {
    private final PropertyChain property;
    private final List<PropertyChain> dependencies;
    private final Function<List<Object>, ?> supplier;

    DependencySpec(PropertyChain property, List<PropertyChain> dependencies, Function<List<Object>, ?> supplier) {
        this.property = property;
        this.dependencies = dependencies;
        this.supplier = supplier;
    }

    void apply(Object object) {
        property.setTo(object, supplier.apply(dependencies.stream().map(d -> d.getFrom(object)).collect(Collectors.toList())));
    }

    List<PropertyChain> getDependencies() {
        return dependencies;
    }
}
