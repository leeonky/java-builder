package com.github.leeonky.javabuilder;

import com.github.leeonky.util.BeanClass;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

public abstract class AbstractFactory<T> implements Factory<T> {
    private final BeanClass<T> beanClass;
    private final Map<String, Consumer<SpecificationBuilder<T>>> combinations = new HashMap<>();
    private Consumer<SpecificationBuilder<T>> specifications = specificationBuilder -> {
    };

    AbstractFactory(Class<T> type) {
        beanClass = BeanClass.create(type);
    }

    @Override
    public BeanClass<T> getBeanClass() {
        return beanClass;
    }

    @Override
    public Factory<T> combinable(String name, Consumer<SpecificationBuilder<T>> specifications) {
        combinations.put(name, specifications);
        return this;
    }

    @Override
    public void combine(BuildingContext<T> buildingContext, String... combinations) {
        for (String combination : Objects.requireNonNull(combinations)) {
            Consumer<SpecificationBuilder<T>> consumer = this.combinations.get(combination);
            if (consumer == null)
                throw new IllegalArgumentException(String.format("Combination '%s' not exist", combination));
            buildingContext.collectSpecifications(consumer);
        }
    }

    @Override
    public Consumer<SpecificationBuilder<T>> getSpecifications() {
        return specifications;
    }

    @Override
    public void specifications(Consumer<SpecificationBuilder<T>> specifications) {
        this.specifications = specifications;
    }
}
