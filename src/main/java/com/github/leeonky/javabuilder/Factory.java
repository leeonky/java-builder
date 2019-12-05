package com.github.leeonky.javabuilder;

import com.github.leeonky.util.BeanClass;

import java.util.function.Consumer;

public interface Factory<T> {
    T newInstance(BuildingContext<T> buildingContext);

    BeanClass<T> getBeanClass();

    Factory<T> combinable(String name, Consumer<SpecificationBuilder<T>> specifications);

    void combine(BuildingContext<T> buildingContext, String... combinations);

    void specifications(Consumer<SpecificationBuilder<T>> specificationBuilder);

    Consumer<SpecificationBuilder<T>> getSpecifications();
}
