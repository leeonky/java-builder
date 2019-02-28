package com.github.leeonky;

import java.util.HashMap;
import java.util.Map;

public abstract class AbstractFactory<T> implements Factory<T> {
    private Map<String, Factory> subFactories = new HashMap<>();

    private int sequence = 0;

    @Override
    public int getSequence() {
        return ++sequence;
    }

    @Override
    public Factory<T> extend(String name, TriConsumer<T, Integer, Map<String, Object>> consumer) {
        extendInner(name, consumer);
        return this;
    }

    private void extendInner(String name, TriConsumer<T, Integer, Map<String, Object>> consumer) {
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
        if (names.length == 1)
            return subFactories.get(names[0]);
        else
            return subFactories.get(names[0]).query(names[1]);
    }
}
