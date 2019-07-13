package com.github.leeonky.javabuilder;

import java.util.Map;
import java.util.function.BiFunction;

class ObjectFactory<T> extends AbstractFactory<T> {
    private BiFunction<Integer, Map<String, ?>, T> supplier;

    ObjectFactory(Class<T> type, BiFunction<Integer, Map<String, ?>, T> supplier, FactoryConfiguration factoryConfiguration) {
        super(type, factoryConfiguration);
        this.supplier = supplier;
    }

    @Override
    public T createObject(int sequence, Map<String, ?> params) {
        return supplier.apply(sequence, params);
    }
}
