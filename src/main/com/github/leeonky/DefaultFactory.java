package com.github.leeonky;

import java.lang.reflect.InvocationTargetException;
import java.util.function.Consumer;

public class DefaultFactory<T> implements Factory<T> {
    private final Class<T> type;
    private final Consumer<T> consumer;

    public DefaultFactory(Class<T> type, Consumer<T> consumer) {
        this.type = type;
        this.consumer = consumer;
    }

    @Override
    public T createObject() {
        try {
            T instance = type.getDeclaredConstructor().newInstance();
            consumer.accept(instance);
            return instance;
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            throw new IllegalStateException(e);
        }
    }
}
