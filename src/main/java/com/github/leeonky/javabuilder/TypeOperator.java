package com.github.leeonky.javabuilder;

class TypeOperator<F> {
    private final Class<?> source;

    private final F converter;

    TypeOperator(Class<?> source, F converter) {
        this.source = source;
        this.converter = converter;
    }

    F getConverter() {
        return converter;
    }

    boolean isBaseType(Class<?> source) {
        return this.source.isAssignableFrom(source);
    }

    boolean isPreciseType(Class<?> source) {
        return this.source == source;
    }
}
