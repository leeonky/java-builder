package com.github.leeonky.javabuilder.spec;

import java.util.function.Supplier;

public class SupplierSpec {
    private final PropertyChain property;
    private final Supplier<?> supplier;

    public SupplierSpec(PropertyChain property, Supplier<?> supplier) {
        this.property = property;
        this.supplier = supplier;
    }

    public void apply(Object object) {
        property.setTo(object, supplier.get());
    }
}
