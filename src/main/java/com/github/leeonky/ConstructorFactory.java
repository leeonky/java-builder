package com.github.leeonky;

import java.util.Map;
import java.util.function.BiFunction;

public class ConstructorFactory<T> implements Factory {
    private BiFunction<Integer, Map<String, Object>, T> supplier;
    private int sequence = 0;

    public ConstructorFactory(BiFunction<Integer, Map<String, Object>, T> supplier) {
        this.supplier = supplier;
    }

    @Override
    public Object createObject(Map params) {
        return supplier.apply(++sequence, params);
    }
}
