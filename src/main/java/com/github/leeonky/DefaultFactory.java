package com.github.leeonky;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;

class DefaultFactory<T> extends AbstractFactory<T> {
    private final TriConsumer<T, Integer, Map<String, Object>> consumer;
    private final Constructor<T> constructor;

    DefaultFactory(TriConsumer<T, Integer, Map<String, Object>> consumer, Constructor<T> constructor, Class<T> type) {
        super(type);
        this.constructor = constructor;
        this.consumer = consumer;
    }

    @Override
    public T createObject(int sequence, Map<String, Object> params) {
        T instance;
        try {
            instance = constructor.newInstance();
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw new IllegalStateException(e);
        }
        consumer.accept(instance, sequence, params);
        return instance;
    }
}
