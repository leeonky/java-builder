package com.github.leeonky.javabuilder;

import com.github.leeonky.util.BeanClass;

public abstract class AbstractFactory<T> implements Factory<T> {
    private final BeanClass<T> type;

    protected AbstractFactory(Class<T> type) {
        this.type = BeanClass.create(type);
    }

    @Override
    public BeanClass<T> getBeanClass() {
        return type;
    }
}
