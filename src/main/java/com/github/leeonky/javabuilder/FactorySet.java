package com.github.leeonky.javabuilder;

import com.github.leeonky.util.Converter;

import java.util.HashMap;
import java.util.Map;
import java.util.function.*;

public class FactorySet {
    private final FactoryConfiguration factoryConfiguration;
    private Map<Class, Factory> factories = new HashMap<>();
    private Map<Class, Map<String, Builder>> cacheBuilders = new HashMap<>();

    public FactorySet(FactoryConfiguration factoryConfiguration) {
        this.factoryConfiguration = factoryConfiguration;
    }

    public FactorySet() {
        this(new FactoryConfiguration());
    }

    public Converter getConverter() {
        return factoryConfiguration.getConverter();
    }

    public PropertyBuilder getPropertyBuilder() {
        return factoryConfiguration.getPropertyBuilder();
    }

    public DataRepository getDataRepository() {
        return factoryConfiguration.getDataRepository();
    }

    public <T> Factory<T> onBuild(Class<T> type, Consumer<T> consumer) {
        return onBuild(type, (obj, seq) -> consumer.accept(obj));
    }

    public <T> Factory<T> onBuild(Class<T> type, BiConsumer<T, Integer> consumer) {
        return onBuild(type, (obj, seq, params) -> consumer.accept(obj, seq));
    }

    public <T> Factory<T> onBuild(Class<T> type, TriConsumer<T, Integer, Map<String, ?>> consumer) {
        BeanFactory<T> beanFactory = new BeanFactory<>(type, consumer, factoryConfiguration);
        factories.put(type, beanFactory);
        return beanFactory;
    }

    public <T> Factory<T> register(Class<T> type, Supplier<T> supplier) {
        return register(type, (seq, params) -> supplier.get());
    }

    public <T> Factory<T> register(Class<T> type, Function<Integer, T> supplier) {
        return register(type, (seq, params) -> supplier.apply(seq));
    }

    public <T> Factory<T> register(Class<T> type, BiFunction<Integer, Map<String, ?>, T> supplier) {
        ObjectFactory<T> objectFactory = new ObjectFactory<>(type, supplier, factoryConfiguration);
        factories.put(type, objectFactory);
        return objectFactory;
    }

    public <T> Factory<T> factory(Class<T> type, String extend) {
        Factory<T> factory = factory(type);
        if (extend != null)
            factory = factory.query(extend);
        return factory;
    }

    @SuppressWarnings("unchecked")
    public <T> Factory<T> factory(Class<T> type) {
        return factories.computeIfAbsent(type, k -> new DefaultBeanFactory<>(type, factoryConfiguration));
    }

    @SuppressWarnings("unchecked")
    public <T> Builder<T> type(Class<T> type) {
        return cacheBuilders.computeIfAbsent(type, t -> new HashMap<>())
                .computeIfAbsent(null, s -> new DefaultBuilder<>(this, factory(type)));
    }

    @SuppressWarnings("unchecked")
    public <T> Builder<T> type(Class<T> type, String extend) {
        return cacheBuilders.computeIfAbsent(type, t -> new HashMap<>())
                .computeIfAbsent(extend, s -> new DefaultBuilder<>(this, factory(type, extend)));
    }
}
