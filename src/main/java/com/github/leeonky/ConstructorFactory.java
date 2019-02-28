package com.github.leeonky;

import java.util.Map;
import java.util.function.BiFunction;

class ConstructorFactory<T> extends AbstractFactory<T> {
    private BiFunction<Integer, Map<String, Object>, T> supplier;
    private int sequence = 0;

    ConstructorFactory(BiFunction<Integer, Map<String, Object>, T> supplier) {
        this.supplier = supplier;
    }

    @Override
    public T createObject(Map<String, Object> params) {
        return supplier.apply(++sequence, params);
    }
}
