package com.github.leeonky.javabuilder;

import java.util.function.Supplier;

class SupplierSpec {
    private final PropertyChain property;
    private final Supplier<?> supplier;

    SupplierSpec(PropertyChain property, Supplier<?> supplier) {
        this.property = property;
        this.supplier = supplier;
    }

    void apply(Object object) {
        property.setTo(object, supplier.get());
    }
}
