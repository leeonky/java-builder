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
    private final Map<Class<?>, Factory<?>> beanSpecsMap = new HashMap<>();
    private final Map<String, Factory<?>> beanSpecsNameMap = new HashMap<>();
    private final Map<Class<?>, Integer> sequences = new HashMap<>();
    private final DataRepository dataRepository;

    public FactorySet() {
        dataRepository = new HashMapDataRepository();
    }

    public FactorySet(DataRepository dataRepository) {
        this.dataRepository = dataRepository;
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

    public FactorySet define(Class<? extends BeanSpecs<?>> beanSpecsClass) {
        BeanSpecs<?> beanDefinition = BeanClass.newInstance(beanSpecsClass);
        if (beanSpecsNameMap.containsKey(beanDefinition.getName()))
            throw new IllegalArgumentException(String.format("Specification '%s' already exists", beanDefinition.getName()));
        BeanSpecsFactory<?> beanSpecsFactory = new BeanSpecsFactory<>(beanDefinition);
        beanSpecsMap.put(beanSpecsClass, beanSpecsFactory);
        beanSpecsNameMap.put(beanDefinition.getName(), beanSpecsFactory);
        return this;
    }

    @SuppressWarnings("unchecked")
    public <T> Factory<T> factory(Class<T> type) {
        return (Factory<T>) factories.computeIfAbsent(type, k -> new DefaultBeanFactory<>(type));
    }

    public <T> Builder<T> type(Class<T> type) {
        return new Builder<>(factory(type), this);
    }

    public <T> Builder<T> toBuild(Class<? extends BeanSpecs<T>> beanSpecsClass) {
        Factory<T> factory = specs(beanSpecsClass);
        if (null == factory)
            return define(beanSpecsClass).toBuild(beanSpecsClass);
        return new Builder<>(factory, this);
    }

    @SuppressWarnings("unchecked")
    public <T> Factory<T> specs(Class<? extends BeanSpecs<T>> beanSpecsClass) {
        return (Factory<T>) beanSpecsMap.get(beanSpecsClass);
    }

    public <T> Builder<T> toBuild(String beanSpecsName) {
        return new Builder<>(specs(beanSpecsName), this);
    }

    @SuppressWarnings("unchecked")
    public <T> Factory<T> specs(String beanSpecsName) {
        Factory<T> factory = (Factory<T>) beanSpecsNameMap.get(beanSpecsName);
        if (null == factory)
            throw new IllegalArgumentException(String.format("Specification '%s' not exists", beanSpecsName));
        return factory;
    }

    int getSequence(Class<?> type) {
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
