package com.github.leeonky.javabuilder;

import com.github.leeonky.util.BeanClass;

import java.util.function.Consumer;

public interface Factory<T> {
    T newInstance(BuildingContext<T> buildingContext);

    BeanClass<T> getBeanClass();

    T postProcess(BuildingContext<T> buildingContext, T object);

    Factory<T> combinable(String name, Consumer<SpecificationBuilder<T>> specifications);

    void combine(BuildingContext<T> buildingContext, String... combinations);
}
