package com.github.leeonky;

public class DefaultBuilder<T> implements Builder<T> {
    private final Factory<T> factory;

    public DefaultBuilder(Factory<T> factory) {
        this.factory = factory;
    }

    @Override
    public T build() {
        return factory.createObject();
    }
}
