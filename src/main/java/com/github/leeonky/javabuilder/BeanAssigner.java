package com.github.leeonky.javabuilder;

import java.util.function.Supplier;

public interface BeanAssigner<T> {
    BeanAssigner<T> setDefault();

    BeanAssigner<T> setPropertyDefaultInFactory(String property, String factory);

    BeanAssigner<T> setPropertyDefaultInDefaultFactory(String property);

    BeanAssigner<T> setPropertyDefaultInSupplier(String property, Supplier<?> supplier);
}
