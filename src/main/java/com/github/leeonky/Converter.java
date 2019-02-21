package com.github.leeonky;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Stream;

import static java.util.Collections.emptyList;

public class Converter {
    private Map<Class<?>, List<TypeConverter>> typeConverters = new HashMap<>();

    public <T, R> Converter addTypeConverter(Class<T> source, Class<R> target, Function<T, R> converter) {
        typeConverters.computeIfAbsent(target, k -> new ArrayList<>())
                .add(new TypeConverter(source, converter));
        return this;
    }

    private Optional<TypeConverter> findTypeConverter(Class<?> source, Class<?> target) {
        List<TypeConverter> converters = typeConverters.getOrDefault(target, emptyList());
        return Stream.concat(converters.stream().filter(t -> t.isPreciseType(source)),
                converters.stream().filter(t -> t.isBaseType(source))).findFirst();
    }

    @SuppressWarnings("unchecked")
    public Object tryConvert(Class<?> type, Object value) {
        Class<?> sourceType = value.getClass();
        return type.isAssignableFrom(sourceType) ? value
                : findTypeConverter(sourceType, type)
                .map(c -> c.getConverter().apply(value))
                .orElse(value);
    }
}
