package com.github.leeonky.javabuilder.spec;

public class TypeHandler<F> {
    private final Class<?> type;

    private final F handler;

    public TypeHandler(Class<?> type, F handler) {
        this.type = type;
        this.handler = handler;
    }

    public F getHandler() {
        return handler;
    }

    public boolean isBaseType(Class<?> type) {
        return this.type.isAssignableFrom(type);
    }

    public boolean isPreciseType(Class<?> type) {
        return this.type.equals(type);
    }
}

