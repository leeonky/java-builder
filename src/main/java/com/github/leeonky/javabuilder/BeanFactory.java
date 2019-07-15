package com.github.leeonky.javabuilder;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.function.BiConsumer;

class BeanFactory<T> extends AbstractFactory<T> {
    private final BiConsumer<T, BuildContext> consumer;
    private final Constructor<T> constructor;

    BeanFactory(Class<T> type, BiConsumer<T, BuildContext> consumer, FactoryConfiguration factoryConfiguration) {
        super(type, factoryConfiguration);
        try {
            constructor = type.getDeclaredConstructor();
        } catch (NoSuchMethodException e) {
            throw new IllegalStateException("No default constructor of class: " + type.getName(), e);
        }
        this.consumer = consumer;
    }

    @Override
    public T createObject(BuildContext buildContext) {
        T instance;
        try {
            instance = constructor.newInstance();
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw new IllegalStateException(e);
        }
        consumer.accept(instance, buildContext);
        return instance;
    }
}
