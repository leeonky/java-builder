package com.github.leeonky.javabuilder;

import java.util.*;
import java.util.function.Consumer;

class DefaultBuilder<T> implements Builder<T> {
    private final Factory<T> factory;
    private final Consumer<Converter> register;
    private List<T> dataRepo = new ArrayList<>();
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
        newBuilder.dataRepo = dataRepo;
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
    public void clearRepository() {
        dataRepo.clear();
    }

    @Override
    public T build() {
        T object = factory.createObject(factory.getSequence(), params);
        dataRepo.add(object);
        return beanUtil.assignProperties(object, properties);
    }

    @Override
    public T query() {
        return dataRepo.stream()
                .filter(this::isCandidate)
                .findFirst().orElse(null);
    }

    private boolean isCandidate(Object o) {
        Class<?> type = o.getClass();
        return !properties.entrySet().stream().anyMatch(e -> {
            try {
                return !Objects.equals(BeanUtil.getPropertyValue(o, e.getKey()),
                        beanUtil.getConverter().tryConvert(
                                BeanUtil.getGetter(type, e.getKey()).getReturnType(), e.getValue()));
            } catch (Exception ex) {
                throw new IllegalStateException(ex);
            }
        });
    }
}
