package com.github.leeonky.javabuilder;

import com.github.leeonky.util.BeanClass;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

public abstract class AbstractFactory<T> implements Factory<T> {
    private final BeanClass<T> beanClass;
    private final Map<String, Consumer<SpecificationBuilder<T>>> definedCombinationSpecificationsDefinitions = new HashMap<>();
    private Consumer<SpecificationBuilder<T>> definedSpecificationsDefinition = builder -> {
    };

    AbstractFactory(Class<T> type) {
        beanClass = BeanClass.create(type);
    }

    @Override
    public BeanClass<T> getBeanClass() {
        return beanClass;
    }

    @Override
    public Factory<T> combinable(String name, Consumer<SpecificationBuilder<T>> specificationsDefinition) {
        definedCombinationSpecificationsDefinitions.put(name, specificationsDefinition);
        return this;
    }

    @Override
    public void collectSpecifications(BeanContext<T> beanContext, String... specifiedCombinations) {
        beanContext.collectSpecifications(definedSpecificationsDefinition);
        for (String combination : Objects.requireNonNull(specifiedCombinations)) {
            Consumer<SpecificationBuilder<T>> consumer = definedCombinationSpecificationsDefinitions.get(combination);
            if (consumer == null)
                throw new IllegalArgumentException(String.format("Combination '%s' not exist", combination));
            beanContext.collectSpecifications(consumer);
        }
    }

    @Override
    public void specifications(Consumer<SpecificationBuilder<T>> specificationsDefinition) {
        definedSpecificationsDefinition = specificationsDefinition;
    }
}
