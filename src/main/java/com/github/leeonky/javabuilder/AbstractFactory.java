package com.github.leeonky.javabuilder;

import com.github.leeonky.util.BeanClass;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public abstract class AbstractFactory<T> implements Factory<T> {
    private final BeanClass<T> type;
    private final Map<String, Consumer<SpecificationBuilder<T>>> combinations = new HashMap<>();

    AbstractFactory(Class<T> type) {
        this.type = BeanClass.create(type);
    }

    @Override
    public BeanClass<T> getBeanClass() {
        return type;
    }

    @Override
    public T postProcess(BuildingContext<T> buildingContext, T object) {
        return object;
    }

    @Override
    public Factory<T> combinable(String name, Consumer<SpecificationBuilder<T>> specifications) {
        combinations.put(name, specifications);
        return this;
    }

    @Override
    public void combine(BuildingContext<T> buildingContext, String... combinations) {
        for (String combination : combinations) {
            Consumer<SpecificationBuilder<T>> consumer = this.combinations.get(combination);
            if (consumer == null)
                throw new IllegalArgumentException(String.format("Combination '%s' not exist", combination));
            consumer.accept(buildingContext.getSpecificationBuilder());
        }
    }
}
