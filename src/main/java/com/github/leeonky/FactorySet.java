package com.github.leeonky;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class FactorySet {
    private Map<Class, Factory> factories = new HashMap<>();

    public <T> Builder<T> type(Class<T> type) {
        return new DefaultBuilder<T>(factories.get(type));
    }

    public <T> void register(Class<T> type, Consumer<T> consumer) {
        register(type, (obj, seq) -> consumer.accept(obj));
    }

    public <T> void register(Class<T> type, BiConsumer<T, Integer> consumer) {
        register(type, (obj, seq, params) -> consumer.accept(obj, seq));
    }

    public <T> void register(Class<T> type, TriConsumer<T, Integer, Map<String, Object>> consumer) {
        DefaultFactory<T> defaultFactory;
        try {
            defaultFactory = new DefaultFactory<>(consumer, type.getDeclaredConstructor());
        } catch (NoSuchMethodException e) {
            throw new IllegalStateException("No default constructor of class: " + type.getName(), e);
        }
        factories.put(type, defaultFactory);
    }
}
