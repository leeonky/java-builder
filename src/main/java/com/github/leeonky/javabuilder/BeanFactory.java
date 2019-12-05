package com.github.leeonky.javabuilder;

import java.util.function.BiConsumer;

class BeanFactory<T> extends AbstractFactory<T> {
    private final BiConsumer<T, BuildingContext<T>> builder;

    BeanFactory(Class<T> type, BiConsumer<T, BuildingContext<T>> builder) {
        super(type);
        this.builder = builder;
    }

    @Override
    public T newInstance(BuildingContext<T> buildingContext) {
        T instance = getBeanClass().newInstance();
        builder.accept(instance, buildingContext);
        return instance;
    }
}
