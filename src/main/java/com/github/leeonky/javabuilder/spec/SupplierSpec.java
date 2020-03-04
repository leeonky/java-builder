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

    public static class SupplierOnce<T> implements Supplier<T> {
        private final Supplier<T> supplier;

        private boolean got = false;

        private T object;

        public SupplierOnce(Supplier<T> supplier) {
            this.supplier = supplier;
        }

        @Override
        public T get() {
            if (!got) {
                object = supplier.get();
                got = true;
            }
            return object;
        }
    }
}
