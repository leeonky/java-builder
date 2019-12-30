package com.github.leeonky.javabuilder;

public interface DependencyProperty<T> {
    String getDependencyName();

    void apply(T instance);
}
