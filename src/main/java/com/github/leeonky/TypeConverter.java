package com.github.leeonky;

import java.util.function.Function;

class TypeConverter {
    private final Class<?> source;

    private final Function converter;

    TypeConverter(Class<?> source, Function converter) {
        this.source = source;
        this.converter = converter;
    }

    Function getConverter() {
        return converter;
    }

    boolean isBaseType(Class<?> source) {
        return this.source.isAssignableFrom(source);
    }

    boolean isPreciseType(Class<?> source) {
        return this.source == source;
    }
}
