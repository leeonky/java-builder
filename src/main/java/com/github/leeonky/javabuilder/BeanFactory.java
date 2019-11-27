package com.github.leeonky.javabuilder;

import com.github.leeonky.util.BeanClass;

import java.util.function.BiConsumer;

class BeanFactory<T> implements Factory<T> {
    private final Class<T> type;
    private final BiConsumer<T, BuildContext<T>> build;

    BeanFactory(Class<T> type, BiConsumer<T, BuildContext<T>> build) {
        this.type = type;
        this.build = build;
    }

    @Override
    public T newInstance(BuildContext<T> buildContext) {
        T instance = BeanClass.newInstance(type);
        build.accept(instance, buildContext);
        return instance;
    }

    @Override
    public BeanClass<T> getBeanClass() {
        return BeanClass.create(type);
    }
}
