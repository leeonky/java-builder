package com.github.leeonky.javabuilder;

import com.github.leeonky.util.BeanClass;

import java.util.function.Consumer;

public interface Factory<T> {
    T newInstance(BeanContext<T> beanContext);

    BeanClass<T> getBeanClass();

    Factory<T> combinable(String name, Consumer<BeanContext<T>> spec);

    void collectSpecs(BeanContext<T> beanContext, String... combinations);

    void spec(Consumer<BeanContext<T>> spec);
}
