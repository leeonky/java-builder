package com.github.leeonky.javabuilder;

import java.util.function.Supplier;

public class SupplierSpecification {
    private final PropertyChain propertyChain;
    private final Supplier<?> supplier;

    public SupplierSpecification(PropertyChain propertyChain, Supplier<?> supplier) {
        this.propertyChain = propertyChain;
        this.supplier = supplier;
    }

    public void apply(Object object) {
        propertyChain.setTo(object, supplier.get());
    }
}
