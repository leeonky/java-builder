package com.github.leeonky;

import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Stream;

import static java.util.Collections.emptyList;

public class Converter {
    private Map<Class<?>, List<TypeConverter<Function>>> typeConverters = new HashMap<>();
    private Map<Class<?>, List<TypeConverter<BiFunction>>> enumConverters = new HashMap<>();

    public static Class<?> boxedClass(Class<?> source) {
        if (source.isPrimitive())
            if (source == int.class)
                source = Integer.class;
            else if (source == short.class)
                source = Short.class;
            else if (source == long.class)
                source = Long.class;
            else if (source == float.class)
                source = Float.class;
            else if (source == double.class)
                source = Double.class;
            else if (source == boolean.class)
                source = Boolean.class;
        return source;
    }

    public <T, R> Converter addTypeConverter(Class<T> source, Class<R> target, Function<T, R> converter) {
        typeConverters.computeIfAbsent(target, k -> new ArrayList<>())
                .add(new TypeConverter<>(source, converter));
        return this;
    }

    private <T> Optional<TypeConverter<T>> findTypeConverter(Class<?> source, Class<?> target,
                                                             Map<Class<?>, List<TypeConverter<T>>> typeConverters, List<TypeConverter<T>> defaultValue) {
        List<TypeConverter<T>> converters = typeConverters.getOrDefault(target, defaultValue);
        return Stream.concat(converters.stream().filter(t -> t.isPreciseType(source)),
                converters.stream().filter(t -> t.isBaseType(source))).findFirst();
    }

    @SuppressWarnings("unchecked")
    public Object tryConvert(Class<?> type, Object value) {
        Class<?> sourceType = value.getClass();
        return type.isAssignableFrom(sourceType) ? value
                : findTypeConverter(sourceType, type, typeConverters, emptyList())
                .map(c -> c.getConverter().apply(value))
                .orElseGet(() -> type.isEnum() ?
                        findTypeConverter(sourceType, type, enumConverters, getBaseEnumTypeConverts(type, enumConverters))
                                .map(c -> c.getConverter().apply(type, value))
                                .orElseGet(() -> Enum.valueOf((Class) type, value.toString())) :
                        value);
    }

    private List<TypeConverter<BiFunction>> getBaseEnumTypeConverts(Class<?> type,
                                                                    Map<Class<?>, List<TypeConverter<BiFunction>>> enumConverters) {
        return enumConverters.entrySet().stream()
                .filter(e -> e.getKey().isAssignableFrom(type))
                .map(Map.Entry::getValue)
                .findFirst().orElse(emptyList());
    }

    @SuppressWarnings("unchecked")
    public <E, V> Converter addEnumConverter(Class<V> source, Class<E> target, BiFunction<Class<E>, V, E> converter) {
        source = (Class<V>) boxedClass(source);
        enumConverters.computeIfAbsent(target, k -> new ArrayList<>())
                .add(new TypeConverter<>(source, converter));
        return this;
    }
}
