package com.github.leeonky.javabuilder.spec;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

public class DependencySpec {
    private final PropertyChain property;
    private final List<PropertyChain> dependencies;
    private final Function<List<Object>, ?> supplier;

    public DependencySpec(PropertyChain property, List<PropertyChain> dependencies, Function<List<Object>, ?> supplier) {
        this.property = property;
        this.dependencies = dependencies;
        this.supplier = supplier;
    }

    public void apply(Object object) {
        property.setTo(object, supplier.apply(dependencies.stream().map(d -> d.getFrom(object)).collect(Collectors.toList())));
    }

    public List<PropertyChain> getDependencies() {
        return dependencies;
    }
}
