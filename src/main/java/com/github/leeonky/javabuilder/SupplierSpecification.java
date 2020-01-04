package com.github.leeonky.javabuilder;

import java.util.function.Supplier;

class SupplierSpecification {
    private final PropertyChain propertyChain;
    private final Supplier<?> supplier;

    SupplierSpecification(PropertyChain propertyChain, Supplier<?> supplier) {
        this.propertyChain = propertyChain;
        this.supplier = supplier;
    }

    void apply(Object object) {
        propertyChain.setTo(object, supplier.get());
    }
}
