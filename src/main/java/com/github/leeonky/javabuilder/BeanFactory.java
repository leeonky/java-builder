package com.github.leeonky.javabuilder;

import java.util.function.BiConsumer;

class BeanFactory<T> extends AbstractFactory<T> {
    private final BiConsumer<T, BuildContext<T>> build;

    BeanFactory(Class<T> type, BiConsumer<T, BuildContext<T>> build) {
        super(type);
        this.build = build;
    }

    @Override
    public T newInstance(BuildContext<T> buildContext) {
        T instance = getBeanClass().newInstance();
        build.accept(instance, buildContext);
        return instance;
    }
}
