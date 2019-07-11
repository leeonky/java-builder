package com.github.leeonky.javabuilder;

import com.github.leeonky.util.BeanClass;
import com.github.leeonky.util.PropertyReader;
import com.github.leeonky.util.PropertyWriter;

import java.util.*;
import java.util.function.Consumer;

class DefaultBuilder<T> implements Builder<T> {
    private final Factory<T> factory;
    private final Consumer<Converter> register;
    private final BeanClass<T> beanClass;
    private final Converter converter = Converter.createDefaultConverter();
    private List<T> dataRepo = new ArrayList<>();
    private Map<String, Object> params = new HashMap<>();
    private Map<String, Object> properties = new HashMap<>();

    DefaultBuilder(Factory<T> factory, Consumer<Converter> register) {
        this.factory = Objects.requireNonNull(factory);
        this.register = register;
        beanClass = new BeanClass<>(factory.getType());
        register.accept(converter);
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
        properties.forEach((k, v) -> {
            PropertyWriter<T> propertyWriter = beanClass.getPropertyWriter(k);
            propertyWriter.setValue(object, converter.tryConvert(propertyWriter.getPropertyType(), v));
        });
        return object;
    }

    @Override
    public T query() {
        return dataRepo.stream()
                .filter(this::isCandidate)
                .findFirst().orElse(null);
    }

    private boolean isCandidate(T o) {
        return properties.entrySet().stream().noneMatch(e -> {
            PropertyReader<T> propertyReader = beanClass.getPropertyReader(e.getKey());
            return !Objects.equals(propertyReader.getValue(o),
                    converter.tryConvert(propertyReader.getPropertyType(), e.getValue()));
        });
    }
}
