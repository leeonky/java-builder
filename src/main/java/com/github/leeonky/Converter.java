package com.github.leeonky;

import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static java.util.Collections.emptyList;

public class Converter {
    private Map<Class<?>, List<TypeConverter<Function>>> typeConverters = new HashMap<>();
    private Map<Class<?>, List<TypeConverter<BiFunction>>> enumConverters = new HashMap<>();

    public static Class<?> boxedClass(Class<?> source) {
        if (source == int.class)
            source = Integer.class;
        return source;
    }

    public <T, R> Converter addTypeConverter(Class<T> source, Class<R> target, Function<T, R> converter) {
        typeConverters.computeIfAbsent(target, k -> new ArrayList<>())
                .add(new TypeConverter<>(source, converter));
        return this;
    }

    private Optional<TypeConverter<Function>> findTypeConverter(Class<?> source, Class<?> target) {
        List<TypeConverter<Function>> converters = typeConverters.getOrDefault(target, emptyList());
        return Stream.concat(converters.stream().filter(t -> t.isPreciseType(source)),
                converters.stream().filter(t -> t.isBaseType(source))).findFirst();
    }

    @SuppressWarnings("unchecked")
    public Object tryConvert(Class<?> type, Object value) {
        Class<?> sourceType = value.getClass();
        return type.isAssignableFrom(sourceType) ? value
                : findTypeConverter(sourceType, type)
                .map(c -> c.getConverter().apply(value))
                .orElseGet(noTypeConverter(sourceType, type, value));
    }

    @SuppressWarnings("unchecked")
    private Supplier<Object> noTypeConverter(Class<?> sourceType, Class<?> type, Object value) {
        return () -> {
            if (type.isEnum()) {
                try {
                    return enumConverters.get(type).stream().filter(t -> t.isPreciseType(sourceType)).findFirst().get().getConverter().apply(type, value);
                } catch (Exception e) {
                    return Enum.valueOf((Class) type, value.toString());
                }
            }
            return value;
        };
    }

    @SuppressWarnings("unchecked")
    public <E, V> Converter addEnumConverter(Class<V> source, Class<E> target, BiFunction<Class<E>, V, E> converter) {
        if (source.isPrimitive()) {
            source = (Class<V>) boxedClass(source);
        }
        enumConverters.computeIfAbsent(target, k -> new ArrayList<>())
                .add(new TypeConverter<>(source, converter));
        return this;
    }
}
