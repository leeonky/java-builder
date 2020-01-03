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
    private final Map<String, Factory<?>> beanSpecificationNameMap = new HashMap<>();
    private final Map<Class<?>, Integer> sequences = new HashMap<>();
    private final DataRepository dataRepository;

    public FactorySet() {
        dataRepository = new HashMapDataRepository();
    }

    public PropertyBuilder getPropertyBuilder() {
        return propertyBuilder;
    }

    public <T> Factory<T> onBuild(Class<T> type, Consumer<T> build) {
        return onBuild(type, (o, context) -> build.accept(o));
    }

    public <T> Factory<T> onBuild(Class<T> type, BiConsumer<T, BeanContext<T>> build) {
        try {
            type.getConstructor();
        } catch (NoSuchMethodException e) {
            throw new IllegalStateException("No default constructor of class: " + type.getName());
        }
        BeanFactory<T> beanFactory = new BeanFactory<>(type, build);
        factories.put(type, beanFactory);
        return beanFactory;
    }

    public <T> Factory<T> register(Class<T> type, Supplier<T> supplier) {
        BeanWithNoDefaultConstructorFactory<T> factory = new BeanWithNoDefaultConstructorFactory<>(type, (buildContext) -> supplier.get());
        factories.put(type, factory);
        return factory;
    }

    public <T> Factory<T> register(Class<T> type, Function<BeanContext<T>, T> supplier) {
        BeanWithNoDefaultConstructorFactory<T> factory = new BeanWithNoDefaultConstructorFactory<>(type, supplier);
        factories.put(type, factory);
        return factory;
    }

    public <B extends BeanSpecification<T>, T> FactorySet define(Class<B> beanSpecificationClass) {
        B beanDefinition = BeanClass.newInstance(beanSpecificationClass);
        if (beanSpecificationNameMap.containsKey(beanDefinition.getName()))
            throw new IllegalArgumentException(String.format("Specification '%s' already exists", beanDefinition.getName()));
        BeanSpecificationFactory<T> beanSpecificationFactory = new BeanSpecificationFactory<>(beanDefinition);
        beanSpecificationMap.put(beanSpecificationClass, beanSpecificationFactory);
        beanSpecificationNameMap.put(beanDefinition.getName(), beanSpecificationFactory);
        return this;
    }

    @SuppressWarnings("unchecked")
    public <T> Factory<T> factory(Class<T> type) {
        return (Factory<T>) factories.computeIfAbsent(type, k -> new DefaultBeanFactory<>(type));
    }

    public <T> Builder<T> type(Class<T> type) {
        return new Builder<>(factory(type), this);
    }

    @SuppressWarnings("unchecked")
    public <B extends BeanSpecification<T>, T> Builder<T> toBuild(Class<B> beanSpecificationClass) {
        Factory<T> factory = (Factory<T>) beanSpecificationMap.get(beanSpecificationClass);
        if (null == factory)
            return define(beanSpecificationClass).toBuild(beanSpecificationClass);
        return new Builder<>(factory, this);
    }

    @SuppressWarnings("unchecked")
    public <T> Builder<T> toBuild(String beanSpecificationName) {
        Factory<T> factory = (Factory<T>) beanSpecificationNameMap.get(beanSpecificationName);
        if (null == factory)
            throw new IllegalArgumentException(String.format("Specification '%s' not exists", beanSpecificationName));
        return new Builder<>(factory, this);
    }

    public int getSequence(Class<?> type) {
        synchronized (FactorySet.class) {
            int sequence = sequences.getOrDefault(type, 0) + 1;
            sequences.put(type, sequence);
            return sequence;
        }
    }

    public DataRepository getDataRepository() {
        return dataRepository;
    }
}
