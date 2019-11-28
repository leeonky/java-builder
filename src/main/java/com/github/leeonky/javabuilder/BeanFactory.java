package com.github.leeonky.javabuilder;

import java.util.function.BiConsumer;

class BeanFactory<T> extends AbstractFactory<T> {
    private final BiConsumer<T, BuildingContext<T>> build;

    BeanFactory(Class<T> type, BiConsumer<T, BuildingContext<T>> build) {
        super(type);
        this.build = build;
    }

    @Override
    public T newInstance(BuildingContext<T> buildingContext) {
        T instance = getBeanClass().newInstance();
        build.accept(instance, buildingContext);
        return instance;
    }
}
