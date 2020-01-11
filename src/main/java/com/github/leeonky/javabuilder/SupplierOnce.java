package com.github.leeonky.javabuilder;

import java.util.function.Supplier;

public class SupplierOnce<T> implements Supplier<T> {
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
