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
        ExtendedFactory<T> extendedFactory = new ExtendedFactory<>(this, consumer);
        subFactories.put(name, extendedFactory);
        return this;
    }

    @Override
    public Factory query(String extend) {
        return subFactories.get(extend);
    }
}
