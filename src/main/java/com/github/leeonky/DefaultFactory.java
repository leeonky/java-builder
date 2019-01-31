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
        T instance;
        try {
            instance = type.getDeclaredConstructor().newInstance();
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            throw new IllegalStateException(e);
        }
        consumer.accept(instance);
        return instance;
    }
}
