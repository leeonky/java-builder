package com.github.leeonky.javabuilder;

import com.github.leeonky.util.BeanClass;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

public abstract class AbstractFactory<T> implements Factory<T> {
    private final BeanClass<T> beanClass;
    private final FactoryConfiguration factoryConfiguration;
    private Map<String, Factory> subFactories = new HashMap<>();

    private int sequence = 0;

    public AbstractFactory(Class<T> type, FactoryConfiguration factoryConfiguration) {
        this.factoryConfiguration = factoryConfiguration;
        beanClass = new BeanClass<>(type, factoryConfiguration.getConverter());
    }

    @Override
    public int getSequence() {
        return ++sequence;
    }

    @Override
    public Factory<T> extend(String name, BiConsumer<T, BuildContext> consumer) {
        try {
            getRoot().query(name);
            throw new IllegalArgumentException("Duplicated factory name[" + name + "] for " + beanClass.getName());
        } catch (NoFactoryException ignore) {
            ExtendedFactory<T> extendedFactory = new ExtendedFactory<>(this, consumer, factoryConfiguration);
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

    static class NoFactoryException extends RuntimeException {
        NoFactoryException(String extend, Class<?> type) {
            super("Factory[" + extend + "] for " + type.getName() + " dose not exist");
        }
    }
}
