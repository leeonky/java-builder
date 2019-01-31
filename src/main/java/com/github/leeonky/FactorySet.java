package com.github.leeonky;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class FactorySet {
    private Map<Class, Factory> factories = new HashMap<>();

    public <T> Builder<T> type(Class<T> type) {
        return new DefaultBuilder<T>(factories.get(type));
    }

    public <T> void register(Class<T> type, Consumer<T> consumer) {
        DefaultFactory<T> defaultFactory = new DefaultFactory<>(type, consumer);
        factories.put(type, defaultFactory);
    }
}
