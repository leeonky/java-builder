package com.github.leeonky;

import java.util.HashMap;
import java.util.Map;
import java.util.function.*;

public class FactorySet {
    private Map<Class, Factory> factories = new HashMap<>();
    private Consumer<Converter> register = c -> {
    };

    public <T> Builder<T> type(Class<T> type) {
        return new DefaultBuilder<T>(factories.get(type), register);
    }

    public <T> void onBuild(Class<T> type, Consumer<T> consumer) {
        onBuild(type, (obj, seq) -> consumer.accept(obj));
    }

    public <T> void onBuild(Class<T> type, BiConsumer<T, Integer> consumer) {
        onBuild(type, (obj, seq, params) -> consumer.accept(obj, seq));
    }

    public <T> void onBuild(Class<T> type, TriConsumer<T, Integer, Map<String, Object>> consumer) {
        DefaultFactory<T> defaultFactory;
        try {
            defaultFactory = new DefaultFactory<>(consumer, type.getDeclaredConstructor());
        } catch (NoSuchMethodException e) {
            throw new IllegalStateException("No default constructor of class: " + type.getName(), e);
        }
        factories.put(type, defaultFactory);
    }

    public <T> void register(Class<T> type, Supplier<T> supplier) {
        register(type, (seq, params) -> supplier.get());
    }

    public <T> void register(Class<T> type, Function<Integer, T> supplier) {
        register(type, (seq, params) -> supplier.apply(seq));
    }

    public <T> void register(Class<T> type, BiFunction<Integer, Map<String, Object>, T> supplier) {
        factories.put(type, new ConstructorFactory<>(supplier));
    }

    public FactorySet registerConverter(Consumer<Converter> register) {
        this.register = register;
        return this;
    }
}
