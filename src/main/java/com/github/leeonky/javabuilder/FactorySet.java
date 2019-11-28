package com.github.leeonky.javabuilder;

import com.github.leeonky.util.BeanClass;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class FactorySet {
    private final PropertyBuilder propertyBuilder = PropertyBuilder.createDefaultPropertyBuilder();
    private final Map<Class<?>, Factory<?>> factories = new HashMap<>();
    private final Map<Class<?>, Factory<?>> beanSpecificationMap = new HashMap<>();
    private final Map<Class<?>, Integer> sequences = new HashMap<>();

    public PropertyBuilder getPropertyBuilder() {
        return propertyBuilder;
    }

    public <T> FactorySet onBuild(Class<T> type, Consumer<T> build) {
        return onBuild(type, (o, context) -> build.accept(o));
    }

    public <T> FactorySet onBuild(Class<T> type, BiConsumer<T, BuildingContext<T>> build) {
        try {
            type.getConstructor();
        } catch (NoSuchMethodException e) {
            throw new IllegalStateException("No default constructor of class: " + type.getName());
        }
        factories.put(type, new BeanFactory<>(type, build));
        return this;
    }

    @SuppressWarnings("unchecked")
    public <T> Builder<T> type(Class<T> type) {
        Factory<T> factory = (Factory<T>) factories.getOrDefault(type, new DefaultBeanFactory<>(type));
        return new Builder<>(factory, this);
    }

    public <T> FactorySet register(Class<T> type, Supplier<T> supplier) {
        factories.put(type, new BeanWithNoDefaultConstructorFactory<>(type, (buildContext) -> supplier.get()));
        return this;
    }

    public <T> FactorySet register(Class<T> type, Function<BuildingContext<T>, T> supplier) {
        factories.put(type, new BeanWithNoDefaultConstructorFactory<>(type, supplier));
        return this;
    }

    public <B extends BeanSpecification<T>, T> FactorySet onBuild(Class<B> definition) {
        beanSpecificationMap.put(definition, new BeanSpecificationFactory<>(BeanClass.newInstance(definition)));
        return this;
    }

    @SuppressWarnings("unchecked")
    public <B extends BeanSpecification<T>, T> Builder<T> toBuild(Class<B> definition) {
        return new Builder<>((Factory<T>) beanSpecificationMap.get(definition), this);
    }

    public int getSequence(Class<?> type) {
        synchronized (FactorySet.class) {
            int sequence = sequences.getOrDefault(type, 0) + 1;
            sequences.put(type, sequence);
            return sequence;
        }
    }
}
