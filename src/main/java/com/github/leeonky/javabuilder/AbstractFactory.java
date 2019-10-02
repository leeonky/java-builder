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
    private Map<String, BiConsumer<T, BuildContext<T>>> combinations = new HashMap<>();

    public AbstractFactory(FactorySet factorySet, Class<T> type) {
        this.factorySet = factorySet;
        beanClass = BeanClass.create(type);
    }

    @Override
    public int getSequence() {
        return factorySet.getTypeSequence(beanClass.getType());
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
    public Factory<T> registerAlias() {
        return registerAlias(getBeanClass().getSimpleName());
    }

    @Override
    public Factory<T> registerAlias(String alias) {
        factorySet.aliasFactory(alias, this);
        return this;
    }

    @Override
    public Factory<T> canCombine(String name, BiConsumer<T, BuildContext<T>> combination) {
        if (combinations.containsKey(name))
            throw new IllegalArgumentException(String.format("Combination [%s] exists", name));
        combinations.put(name, combination);
        return this;
    }

    @Override
    public void combineBuild(T object, String name, BuildContext<T> buildContext) {
        combinations.getOrDefault(name, (o, b) -> {
            throw new IllegalArgumentException(String.format("Combination [%s] does not exist", name));
        }).accept(object, buildContext);
    }
}
