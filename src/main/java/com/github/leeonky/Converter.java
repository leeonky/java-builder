package com.github.leeonky;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import static java.util.Optional.empty;
import static java.util.Optional.ofNullable;

public class Converter {
    private Map<Class<?>, Map<Class<?>, TypeConverter>> typeConverters = new HashMap<>();

    public <T, R> Converter addTypeConverter(Class<T> source, Class<R> target, Function<T, R> converter) {
        typeConverters.computeIfAbsent(target, k -> new HashMap<>())
                .put(source, new TypeConverter(source, converter));
        return this;
    }

    private Optional<TypeConverter> findTypeConverter(Class<?> source, Class<?> target) {
        Map<Class<?>, TypeConverter> typeConverters = this.typeConverters.get(target);
        if (typeConverters != null) {
            Optional<TypeConverter> preciseTypeConverter = getPreciseTypeConverter(source, typeConverters);
            if (preciseTypeConverter.isPresent())
                return preciseTypeConverter;
            return getBaseTypeConverter(source, typeConverters);
        }
        return empty();
    }

    private Optional<TypeConverter> getBaseTypeConverter(Class<?> source, Map<Class<?>, TypeConverter> typeConverters) {
        return ofNullable(typeConverters.get(Object.class));
    }

    private Optional<TypeConverter> getPreciseTypeConverter(Class<?> source, Map<Class<?>, TypeConverter> typeConverters) {
        return ofNullable(typeConverters.get(source));
    }

    @SuppressWarnings("unchecked")
    public Object tryConvert(Class<?> type, Object value) {
        Class<?> sourceType = value.getClass();
        if (!type.isAssignableFrom(sourceType))
            return findTypeConverter(sourceType, type)
                    .map(c -> c.getConverter().apply(value))
                    .orElse(value);
        return value;
    }
}
