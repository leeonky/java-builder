package com.github.leeonky;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;

class DefaultFactory<T> implements Factory<T> {
    private final TriConsumer<T, Integer, Map<String, Object>> consumer;
    private final Constructor<T> constructor;
    private int sequence = 0;

    public DefaultFactory(TriConsumer<T, Integer, Map<String, Object>> consumer, Constructor<T> constructor) {
        this.constructor = constructor;
        this.consumer = consumer;
    }

    @Override
    public T createObject(Map<String, Object> params) {
        T instance;
        try {
            instance = constructor.newInstance();
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw new IllegalStateException(e);
        }
        consumer.accept(instance, ++sequence, params);
        return instance;
    }
}
