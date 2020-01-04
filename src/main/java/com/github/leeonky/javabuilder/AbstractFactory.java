package com.github.leeonky.javabuilder;

import com.github.leeonky.util.BeanClass;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

public abstract class AbstractFactory<T> implements Factory<T> {
    private final BeanClass<T> beanClass;
    private final Map<String, Consumer<SpecBuilder<T>>> definedCombinationSpecs = new HashMap<>();
    private Consumer<SpecBuilder<T>> definedSpec = builder -> {
    };

    AbstractFactory(Class<T> type) {
        beanClass = BeanClass.create(type);
    }

    @Override
    public BeanClass<T> getBeanClass() {
        return beanClass;
    }

    @Override
    public Factory<T> combinable(String name, Consumer<SpecBuilder<T>> combinationSpec) {
        definedCombinationSpecs.put(name, combinationSpec);
        return this;
    }

    @Override
    public void collectSpecs(BeanContext<T> beanContext, String... combinations) {
        beanContext.collectSpecs(definedSpec);
        for (String combination : Objects.requireNonNull(combinations)) {
            Consumer<SpecBuilder<T>> consumer = definedCombinationSpecs.get(combination);
            if (consumer == null)
                throw new IllegalArgumentException(String.format("Combination '%s' not exist", combination));
            beanContext.collectSpecs(consumer);
        }
    }

    @Override
    public void spec(Consumer<SpecBuilder<T>> specBuilder) {
        definedSpec = specBuilder;
    }
}
