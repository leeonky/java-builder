package com.github.leeonky;

class TypeConverter<F> {
    private final Class<?> source;

    private final F converter;

    TypeConverter(Class<?> source, F converter) {
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
