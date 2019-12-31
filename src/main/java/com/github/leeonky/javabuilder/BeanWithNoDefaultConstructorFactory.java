package com.github.leeonky.javabuilder;

import java.util.function.Function;

class BeanWithNoDefaultConstructorFactory<T> extends AbstractFactory<T> {
    private final Function<BeanContext<T>, T> supplier;

    BeanWithNoDefaultConstructorFactory(Class<T> type, Function<BeanContext<T>, T> supplier) {
        super(type);
        this.supplier = supplier;
    }

    @Override
    public T newInstance(BeanContext<T> beanContext) {
        return supplier.apply(beanContext);
    }
}
