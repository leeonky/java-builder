package com.github.leeonky.javabuilder;

import java.util.function.BiConsumer;

class ExtendedFactory<T> extends AbstractFactory<T> {
    private final Factory<T> parent;
    private final BiConsumer<T, BuildContext<T>> consumer;
    private final String name;

    ExtendedFactory(FactorySet factorySet, Factory<T> parent, String name, BiConsumer<T, BuildContext<T>> consumer) {
        super(factorySet, parent.getBeanClass().getType());
        this.parent = parent;
        this.consumer = consumer;
        this.name = name;
    }

    @Override
    public int getSequence() {
        return parent.getSequence();
    }

    @Override
    public T createObject(BuildContext<T> buildContext) {
        T object = parent.createObject(buildContext);
        consumer.accept(object, buildContext);
        return object;
    }

    @Override
    public Factory<T> getRoot() {
        return parent.getRoot();
    }

    @Override
    public Factory<T> useAlias() {
        return useAlias(name);
    }
}
