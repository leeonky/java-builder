package com.github.leeonky.javabuilder;

import java.util.HashMap;
import java.util.Map;

public abstract class AbstractFactory<T> implements Factory<T> {
    private final Class<T> type;
    private Map<String, Factory> subFactories = new HashMap<>();

    private int sequence = 0;

    public AbstractFactory(Class<T> type) {
        this.type = type;
    }

    @Override
    public int getSequence() {
        return ++sequence;
    }

    @Override
    public Factory<T> extend(String name, TriConsumer<T, Integer, Map<String, ?>> consumer) {
        extendInner(name, consumer);
        return this;
    }

    private void extendInner(String name, TriConsumer<T, Integer, Map<String, ?>> consumer) {
        String[] names = name.split("\\.", 2);
        if (names.length == 1) {
            ExtendedFactory<T> extendedFactory = new ExtendedFactory<>(this, consumer);
            subFactories.put(name, extendedFactory);
        } else
            query(names[0]).extend(names[1], consumer);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Factory<T> query(String extend) {
        String[] names = extend.split("\\.", 2);
        Factory factory = subFactories.get(names[0]);
        if (factory == null)
            throw new NoFactoryException(extend, getType());
        if (names.length == 1)
            return factory;
        else
            try {
                return factory.query(names[1]);
            } catch (NoFactoryException e) {
                throw new NoFactoryException(extend, getType());
            }
    }

    @Override
    public Class<T> getType() {
        return type;
    }

    static class NoFactoryException extends RuntimeException {
        NoFactoryException(String extend, Class<?> type) {
            super("Factory[" + extend + "] for " + type.getName() + " dose not exist");
        }
    }
}
