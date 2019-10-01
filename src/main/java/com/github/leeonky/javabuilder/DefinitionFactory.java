package com.github.leeonky.javabuilder;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.function.BiConsumer;
import java.util.stream.Stream;

class DefinitionFactory<T> extends AbstractFactory<T> {
    private final FactoryDefinition<T> factoryDefinition;
    private final BiConsumer<T, BuildContext<T>> consumer;

    DefinitionFactory(FactorySet factorySet, FactoryDefinition<T> factoryDefinition) {
        super(factorySet, factoryDefinition.getType());
        consumer = factoryDefinition::onBuild;
        this.factoryDefinition = factoryDefinition;

        Stream.of(factoryDefinition.getClass().getMethods())
                .filter(m -> isCombination(factoryDefinition, m))
                .forEach(m -> canCombine(m.getName(), (o, b) -> {
                    try {
                        m.invoke(factoryDefinition, o, b);
                    } catch (IllegalAccessException | InvocationTargetException e) {
                        throw new IllegalStateException(e);
                    }
                }));
    }

    private boolean isCombination(FactoryDefinition<T> factoryDefinition, Method m) {
        return m.getParameters().length == 2
                && m.getParameterTypes()[0].isAssignableFrom(factoryDefinition.getType())
                && m.getParameterTypes()[1] == BuildContext.class
                && !m.getName().equals("onBuild");
    }

    @Override
    public T createObject(BuildContext<T> buildContext) {
        T instance = factorySet.type(factoryDefinition.getType()).buildWithoutSave();
        consumer.accept(instance, buildContext);
        return instance;
    }

    @Override
    public Factory<T> registerAlias() {
        return registerAlias(factoryDefinition.getAlias());
    }
}
