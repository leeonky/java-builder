package com.github.leeonky;

import java.util.HashMap;
import java.util.Map;

class DefaultBuilder<T> implements Builder<T> {
    private final Factory<T> factory;
    private Map<String, Object> params = new HashMap<>();
    private Map<String, Object> properties = new HashMap<>();

    public DefaultBuilder(Factory<T> factory) {
        this.factory = factory;
    }

    private DefaultBuilder<T> copy() {
        DefaultBuilder<T> newBuilder = new DefaultBuilder<>(factory);
        newBuilder.params.putAll(params);
        return newBuilder;
    }

    @Override
    public Builder<T> params(Map<String, Object> params) {
        DefaultBuilder<T> builder = copy();
        builder.params.putAll(params);
        return builder;
    }

    @Override
    public Builder<T> properties(Map<String, Object> properties) {
        DefaultBuilder<T> builder = copy();
        builder.properties.putAll(properties);
        return builder;
    }

    @Override
    public T build() {
        T object = factory.createObject(params);
        return new BeanUtil().assignProperties(object, properties);
    }
}
