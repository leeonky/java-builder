package com.github.leeonky.javabuilder;

import java.util.List;

public interface DependencyProperty<T> {
    List<String> getDependencyName();

    void apply(T instance);
}
