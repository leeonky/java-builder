package com.github.leeonky.javabuilder;

import com.github.leeonky.util.BeanClass;

import java.util.Map;
import java.util.Optional;

public interface DataRepository {
    <T> T save(T object);

    <T> Optional<T> query(BeanClass<T> beanClass, Map<String, Object> properties);

    void clear();
}
