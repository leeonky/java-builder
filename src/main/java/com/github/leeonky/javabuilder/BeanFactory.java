package com.github.leeonky.javabuilder;

import java.util.function.BiConsumer;

class BeanFactory<T> extends AbstractFactory<T> {
    private final BiConsumer<T, BeanContext<T>> builder;

    BeanFactory(Class<T> type, BiConsumer<T, BeanContext<T>> builder) {
        super(type);
        this.builder = builder;
    }

    @Override
    public T newInstance(BeanContext<T> beanContext) {
        T instance = getBeanClass().newInstance();
        builder.accept(instance, beanContext);
        return instance;
    }
}
