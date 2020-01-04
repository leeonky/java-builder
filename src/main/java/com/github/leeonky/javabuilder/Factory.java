package com.github.leeonky.javabuilder;

import com.github.leeonky.util.BeanClass;

import java.util.function.Consumer;

public interface Factory<T> {
    T newInstance(BeanContext<T> beanContext);

    BeanClass<T> getBeanClass();

    Factory<T> combinable(String name, Consumer<SpecBuilder<T>> specifications);

    void collectSpecs(BeanContext<T> beanContext, String... combinations);

    void spec(Consumer<SpecBuilder<T>> specBuilder);
}
