package com.github.leeonky.javabuilder;

import java.util.function.Supplier;

class BeanWithNoDefaultConstructorFactory<T> implements Factory<T> {
    private final Supplier<T> supplier;

    BeanWithNoDefaultConstructorFactory(Supplier<T> supplier) {
        this.supplier = supplier;
    }

    @Override
    public T newInstance() {
        return supplier.get();
    }
}
