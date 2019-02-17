package com.github.leeonky;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import static java.util.Optional.ofNullable;

public class Converter {
    private Map<Class<?>, Map<Class<?>, TypeConverter>> typeConverters = new HashMap<>();

    public <T, R> Converter addTypeConverter(Class<T> source, Class<R> target, Function<T, R> converter) {
        typeConverters.computeIfAbsent(target, k -> new HashMap<>())
                .put(source, new TypeConverter(source, converter));
        return this;
    }

    private Optional<Function> findTypeConverter(Class<?> source, Class<?> target) {
        TypeConverter typeConverter = getPreciseTypeConverter(source, target);
        if (typeConverter == null)
            typeConverter = getBaseTypeConverter(source, target);
        return ofNullable(typeConverter.getConverter());
    }

    private TypeConverter getBaseTypeConverter(Class<?> source, Class<?> target) {
        return typeConverters.get(target).get(Object.class);
    }

    private TypeConverter getPreciseTypeConverter(Class<?> source, Class<?> target) {
        return typeConverters.get(target).get(source);
    }

    @SuppressWarnings("unchecked")
    public Object convert(Class<?> type, Object value) {
        Class<?> sourceType = value.getClass();
        if (sourceType != type) {
            return findTypeConverter(sourceType, type).get().apply(value);
        }
        return value;
    }
}
