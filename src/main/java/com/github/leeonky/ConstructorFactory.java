package com.github.leeonky;

import java.util.Map;
import java.util.function.BiFunction;

class ConstructorFactory<T> extends AbstractFactory<T> {
    private BiFunction<Integer, Map<String, Object>, T> supplier;

    ConstructorFactory(BiFunction<Integer, Map<String, Object>, T> supplier) {
        this.supplier = supplier;
    }

    @Override
    public T createObject(int sequence, Map<String, Object> params) {
        return supplier.apply(sequence, params);
    }
}
