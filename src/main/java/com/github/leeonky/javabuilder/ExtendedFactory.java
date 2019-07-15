package com.github.leeonky.javabuilder;

import java.util.function.BiConsumer;

class ExtendedFactory<T> extends AbstractFactory<T> {
    private final Factory<T> parent;
    private final BiConsumer<T, BuildContext> consumer;

    ExtendedFactory(Factory<T> parent, BiConsumer<T, BuildContext> consumer, FactoryConfiguration factoryConfiguration) {
        super(parent.getBeanClass().getType(), factoryConfiguration);
        this.parent = parent;
        this.consumer = consumer;
    }

    @Override
    public int getSequence() {
        return parent.getSequence();
    }

    @Override
    public T createObject(BuildContext buildContext) {
        T object = parent.createObject(buildContext);
        consumer.accept(object, buildContext);
        return object;
    }

    @Override
    public Factory<T> getRoot() {
        return parent.getRoot();
    }
}
