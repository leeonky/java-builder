package com.github.leeonky.javabuilder;

import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

public interface PropertySpecBuilder<T> {
    BeanContext<T> value(Object value);

    BeanContext<T> from(Supplier<?> supplier);

    <PT> BeanContext<T> from(Class<? extends BeanSpecs<PT>> beanSpecsClass);

    <PT> BeanContext<T> from(Class<? extends BeanSpecs<PT>> beanSpecsClass,
                             Function<Builder<PT>, Builder<PT>> customerBuilder);

    BeanContext<T> dependsOn(String dependency, Function<Object, Object> function);

    BeanContext<T> dependsOn(List<String> dependencies, Function<List<Object>, Object> function);

    BeanContext<T> type(Class<?> type);

    <PT> BeanContext<T> type(Class<PT> type, Function<Builder<PT>, Builder<PT>> customerBuilder);
}
