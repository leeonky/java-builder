package com.github.leeonky.javabuilder;

import java.util.function.Supplier;

class BeanWithNoDefaultConstructorFactory<T> extends AbstractFactory<T> {
    private final Supplier<T> supplier;

    BeanWithNoDefaultConstructorFactory(Class<T> type, Supplier<T> supplier) {
        super(type);
        this.supplier = supplier;
    }

    @Override
    public T newInstance(BuildContext<T> buildContext) {
        return supplier.get();
    }
}
