package com.github.leeonky.javabuilder;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

class DefaultBuilder<T> implements Builder<T> {
    private final Factory<T> factory;
    private final Consumer<Converter> register;
    private Map<String, Object> params = new HashMap<>();
    private Map<String, Object> properties = new HashMap<>();
    private BeanUtil beanUtil = new BeanUtil();

    public DefaultBuilder(Factory<T> factory, Consumer<Converter> register) {
        this.factory = Objects.requireNonNull(factory);
        this.register = register;
        register.accept(beanUtil.getConverter());
    }

    private DefaultBuilder<T> copy() {
        DefaultBuilder<T> newBuilder = new DefaultBuilder<>(factory, register);
        newBuilder.params.putAll(params);
        return newBuilder;
    }

    @Override
    public Builder<T> params(Map<String, ?> params) {
        DefaultBuilder<T> builder = copy();
        builder.params.putAll(params);
        return builder;
    }

    @Override
    public Builder<T> properties(Map<String, ?> properties) {
        DefaultBuilder<T> builder = copy();
        builder.properties.putAll(properties);
        return builder;
    }

    @Override
    public Builder<T> property(String name, Object value) {
        Map<String, Object> map = new HashMap<>();
        map.put(name, value);
        return properties(map);
    }

    @Override
    public T build() {
        return beanUtil.assignProperties(factory.createObject(factory.getSequence(), params), properties);
    }
}
