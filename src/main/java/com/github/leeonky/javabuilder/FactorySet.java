package com.github.leeonky.javabuilder;

import java.util.HashMap;
import java.util.Map;
import java.util.function.*;

public class FactorySet {
    private Map<Class, Factory> factories = new HashMap<>();
    private Consumer<Converter> converterRegister = c -> {
    };
    private Consumer<PropertyBuilder> propertyRegister = c -> {
    };

    public <T> Builder<T> type(Class<T> type) {
        return new DefaultBuilder<>(factory(type), converterRegister);
    }

    public <T> Factory<T> factory(Class<T> type, String extend) {
        return factory(type).query(extend);
    }

    @SuppressWarnings("unchecked")
    public <T> Factory<T> factory(Class<T> type) {
        return factories.computeIfAbsent(type, k -> new DefaultBeanFactory<>(type, propertyRegister));
    }

    public <T> Builder<T> type(Class<T> type, String extend) {
        return new DefaultBuilder<>(factory(type, extend), converterRegister);
    }

    public <T> Factory<T> onBuild(Class<T> type, Consumer<T> consumer) {
        return onBuild(type, (obj, seq) -> consumer.accept(obj));
    }

    public <T> Factory<T> onBuild(Class<T> type, BiConsumer<T, Integer> consumer) {
        return onBuild(type, (obj, seq, params) -> consumer.accept(obj, seq));
    }

    public <T> Factory<T> onBuild(Class<T> type, TriConsumer<T, Integer, Map<String, ?>> consumer) {
        BeanFactory<T> beanFactory = new BeanFactory<>(type, consumer);
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
        ObjectFactory<T> objectFactory = new ObjectFactory<>(type, supplier);
        factories.put(type, objectFactory);
        return objectFactory;
    }

    public FactorySet registerConverter(Consumer<Converter> register) {
        converterRegister = register;
        return this;
    }

    public FactorySet registerPropertyBuilder(Consumer<PropertyBuilder> register) {
        propertyRegister = register;
        return this;
    }
}
