package com.github.leeonky;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;

class BeanFactory<T> extends AbstractFactory<T> {
    private final TriConsumer<T, Integer, Map<String, Object>> consumer;
    private final Constructor<T> constructor;

    BeanFactory(Class<T> type, TriConsumer<T, Integer, Map<String, Object>> consumer) {
        super(type);
        try {
            constructor = type.getDeclaredConstructor();
        } catch (NoSuchMethodException e) {
            throw new IllegalStateException("No default constructor of class: " + type.getName(), e);
        }
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
