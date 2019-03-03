package com.github.leeonky;

import java.util.Map;
import java.util.function.BiFunction;

class ObjectFactory<T> extends AbstractFactory<T> {
    private BiFunction<Integer, Map<String, Object>, T> supplier;

    ObjectFactory(Class<T> type, BiFunction<Integer, Map<String, Object>, T> supplier) {
        super(type);
        this.supplier = supplier;
    }

    @Override
    public T createObject(int sequence, Map<String, Object> params) {
        return supplier.apply(sequence, params);
    }
}
