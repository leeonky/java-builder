package com.github.leeonky.javabuilder;

public interface Specification<T> {
    void apply(T instance);
}
