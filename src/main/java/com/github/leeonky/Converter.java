package com.github.leeonky;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
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

    public static Converter newDefault() {
        return new Converter()
                .addTypeConverter(Object.class, String.class, Object::toString)
                .addTypeConverter(String.class, long.class, Long::valueOf)
                .addTypeConverter(String.class, int.class, Integer::valueOf)
                .addTypeConverter(String.class, short.class, Short::valueOf)
                .addTypeConverter(String.class, byte.class, Byte::valueOf)
                .addTypeConverter(String.class, double.class, Double::valueOf)
                .addTypeConverter(String.class, float.class, Float::valueOf)
                .addTypeConverter(String.class, boolean.class, Boolean::valueOf)

                .addTypeConverter(String.class, Long.class, Long::valueOf)
                .addTypeConverter(String.class, Integer.class, Integer::valueOf)
                .addTypeConverter(String.class, Short.class, Short::valueOf)
                .addTypeConverter(String.class, Byte.class, Byte::valueOf)
                .addTypeConverter(String.class, Double.class, Double::valueOf)
                .addTypeConverter(String.class, Float.class, Float::valueOf)
                .addTypeConverter(String.class, Boolean.class, Boolean::valueOf)

                .addTypeConverter(String.class, BigInteger.class, BigInteger::new)
                .addTypeConverter(String.class, BigDecimal.class, BigDecimal::new)

                .addTypeConverter(String.class, UUID.class, UUID::fromString)

                .addTypeConverter(String.class, Instant.class, Instant::parse)
                .addTypeConverter(String.class, Date.class, source -> {
                    try {
                        return new SimpleDateFormat("yyyy-MM-dd").parse(source);
                    } catch (ParseException e) {
                        throw new IllegalArgumentException("Cannot convert '" + source + "' to " + Date.class.getName(), e);
                    }
                })
                .addTypeConverter(String.class, LocalTime.class, LocalTime::parse)
                .addTypeConverter(String.class, LocalDate.class, LocalDate::parse)
                .addTypeConverter(String.class, LocalDateTime.class, LocalDateTime::parse);
    }

    @SuppressWarnings("unchecked")
    public <T, R> Converter addTypeConverter(Class<T> source, Class<R> target, Function<T, R> converter) {
        source = (Class<T>) boxedClass(source);
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
