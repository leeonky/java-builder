package com.github.leeonky.javabuilder;

import com.github.leeonky.util.BeanClass;

import java.util.function.Supplier;

public interface BeanContext<T> {
    int getCurrentSequence();

    <P> P param(String name);

    BeanClass<T> getBeanClass();

    boolean isPropertyNotSpecified(String name);

    FactorySet getFactorySet();

    PropertySpecBuilder<T> property(String property);

    BeanContext<T> link(String... properties);

    Supplier<T> getCurrent();
}
