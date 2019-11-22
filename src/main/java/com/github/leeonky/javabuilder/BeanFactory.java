package com.github.leeonky.javabuilder;

import com.github.leeonky.util.BeanClass;

import java.util.function.Consumer;

class BeanFactory<T> implements Factory<T> {
    private final Class<T> type;
    private final Consumer<T> build;

    BeanFactory(Class<T> type, Consumer<T> build) {
        this.type = type;
        this.build = build;
    }

    @Override
    public T newInstance() {
        T instance = BeanClass.newInstance(type);
        build.accept(instance);
        return instance;
    }
}
