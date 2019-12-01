package com.github.leeonky.javabuilder;

public interface Specification<T> {
    String getProperty();

    void apply(T instance);
}
