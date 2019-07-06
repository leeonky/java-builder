package com.github.leeonky.javabuilder;

import java.util.Map;

class ExtendedFactory<T> extends AbstractFactory<T> {
    private final Factory<T> parent;
    private final TriConsumer<T, Integer, Map<String, ?>> consumer;

    ExtendedFactory(Factory<T> parent, TriConsumer<T, Integer, Map<String, ?>> consumer) {
        super(parent.getType());
        this.parent = parent;
        this.consumer = consumer;
    }

    @Override
    public int getSequence() {
        return parent.getSequence();
    }

    @Override
    public Class<T> getType() {
        return parent.getType();
    }

    @Override
    public T createObject(int sequence, Map<String, ?> params) {
        T object = parent.createObject(sequence, params);
        consumer.accept(object, sequence, params);
        return object;
    }

    @Override
    public Factory<T> getRoot() {
        return parent.getRoot();
    }
}
