package com.github.leeonky.javabuilder;

import com.github.leeonky.util.BeanClass;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

public abstract class AbstractFactory<T> implements Factory<T> {
    protected final FactorySet factorySet;
    private final BeanClass<T> beanClass;
    private Map<String, Factory> subFactories = new HashMap<>();

    private int sequence = 0;

    public AbstractFactory(FactorySet factorySet, Class<T> type) {
        this.factorySet = factorySet;
        beanClass = new BeanClass<>(type, factorySet.getConverter());
    }

    @Override
    public int getSequence() {
        return ++sequence;
    }

    @Override
    public Factory<T> extend(String name, BiConsumer<T, BuildContext<T>> consumer) {
        try {
            getRoot().query(name);
            throw new IllegalArgumentException("Duplicated factory name[" + name + "] for " + beanClass.getName());
        } catch (NoFactoryException ignore) {
            ExtendedFactory<T> extendedFactory = new ExtendedFactory<>(factorySet, this, name, consumer);
            subFactories.put(name, extendedFactory);
            return extendedFactory;
        }
    }

    @Override
    public Factory<T> query(String extend) {
        List<Factory<T>> result = new ArrayList<>();
        queryAll(extend, result);
        if (result.size() == 0)
            throw new NoFactoryException(extend, beanClass.getType());
        return result.get(0);
    }

    @SuppressWarnings("unchecked")
    private void queryAll(String extend, List<Factory<T>> result) {
        Factory factory = subFactories.get(extend);
        if (factory != null)
            result.add(factory);
        subFactories.values().forEach(f -> ((AbstractFactory) f).queryAll(extend, result));
    }

    @Override
    public BeanClass<T> getBeanClass() {
        return beanClass;
    }

    @Override
    public Factory<T> useAlias() {
        return useAlias(getBeanClass().getSimpleName());
    }

    @Override
    public Factory<T> useAlias(String alias) {
        factorySet.aliasFactory(alias, this);
        return this;
    }

    static class NoFactoryException extends RuntimeException {
        NoFactoryException(String extend, Class<?> type) {
            super("Factory[" + extend + "] for " + type.getName() + " dose not exist");
        }
    }
}
