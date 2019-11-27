package com.github.leeonky.javabuilder;

import com.github.leeonky.util.BeanClass;

import java.util.function.Supplier;

class BeanWithNoDefaultConstructorFactory<T> implements Factory<T> {
    private final Class<T> type;
    private final Supplier<T> supplier;

    BeanWithNoDefaultConstructorFactory(Class<T> type, Supplier<T> supplier) {
        this.type = type;
        this.supplier = supplier;
    }

    @Override
    public T newInstance(BuildContext<T> buildContext) {
        return supplier.get();
    }

    @Override
    public BeanClass<T> getBeanClass() {
        return BeanClass.create(type);
    }
}
