package com.github.leeonky.javabuilder;

import com.github.leeonky.util.BeanClass;
import com.github.leeonky.util.PropertyWriter;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class PropertyBuilder {
    public static final LocalDate LOCAL_DATE_START = LocalDate.parse("1996-01-23");
    public static final LocalDateTime LOCAL_DATE_TIME_START = LocalDateTime.parse("1996-01-23T00:00:00");
    private static final LocalTime LOCAL_TIME_START = LocalTime.parse("00:00:00");
    private static final Instant INSTANT_START = Instant.parse("1996-01-23T00:00:00Z");
    private List<TypeHandler<TriFunction>> setters = new ArrayList<>();
    private List<Predicate> skipper = new ArrayList<>();
    private Map<Predicate, TriFunction> propertyBuilders = new LinkedHashMap<>();

    public static PropertyBuilder createDefaultPropertyBuilder() {
        return new PropertyBuilder()
                .registerFromType(String.class, (c, p, i) -> p.getName() + i)
                .registerFromType(long.class, (c, p, i) -> (long) i)
                .registerFromType(int.class, (c, p, i) -> i)
                .registerFromType(short.class, (c, p, i) -> (short) i.intValue())
                .registerFromType(byte.class, (c, p, i) -> (byte) i.intValue())
                .registerFromType(double.class, (c, p, i) -> (double) i)
                .registerFromType(float.class, (c, p, i) -> (float) i)
                .registerFromType(boolean.class, (c, p, i) -> (i % 2) != 0)
                .registerFromType(Long.class, (c, p, i) -> (long) i)
                .registerFromType(Integer.class, (c, p, i) -> i)
                .registerFromType(Short.class, (c, p, i) -> (short) i.intValue())
                .registerFromType(Byte.class, (c, p, i) -> (byte) i.intValue())
                .registerFromType(Double.class, (c, p, i) -> (double) i)
                .registerFromType(Float.class, (c, p, i) -> (float) i)
                .registerFromType(Boolean.class, (c, p, i) -> (i % 2) != 0)
                .registerFromType(BigInteger.class, (c, p, i) -> BigInteger.valueOf(i))
                .registerFromType(BigDecimal.class, (c, p, i) -> BigDecimal.valueOf(i))
                .registerFromType(UUID.class, (c, p, i) -> UUID.fromString(String.format("00000000-0000-0000-0000-%012d", i)))
                .registerFromType(Instant.class, (c, p, i) -> INSTANT_START.plusSeconds(i))
                .registerFromType(Date.class, (c, p, i) -> Date.from(INSTANT_START.plus(i - 1, ChronoUnit.DAYS)))
                .registerFromType(LocalTime.class, (c, p, i) -> LOCAL_TIME_START.plusSeconds(i))
                .registerFromType(LocalDate.class, (c, p, i) -> LOCAL_DATE_START.plusDays(i - 1))
                .registerFromType(LocalDateTime.class, (c, p, i) -> LOCAL_DATE_TIME_START.plusSeconds(i))
                .registerFromType(Enum.class, (c, p, i) -> {
                    Enum[] enums = c.getEnumConstants();
                    return enums[(i - 1) % enums.length];
                });
    }

    public <T, B> PropertyBuilder registerFromType(Class<T> propertyType, TriFunction<Class<T>, PropertyWriter<B>, Integer, T> builder) {
        setters.add(new TypeHandler<>(propertyType, builder));
        return this;
    }

    public <B> PropertyBuilder registerFromProperty(Predicate<PropertyWriter<B>> predicate, TriFunction<PropertyWriter<B>, Object, Integer, Object> builder) {
        propertyBuilders.put(predicate, builder);
        return this;
    }

    public <B> PropertyBuilder skipProperty(Predicate<PropertyWriter<B>> predicate) {
        skipper.add(predicate);
        return this;
    }

    @SuppressWarnings("unchecked")
    public <T> T apply(int sequence, T object) {
        new BeanClass<>(object.getClass()).getPropertyWriters()
                .values().stream()
                .filter(propertyWriter -> skipper.stream().noneMatch(p -> p.test(propertyWriter)))
                .forEach(propertyWriter -> buildAndAssign(propertyWriter, sequence, object));
        return object;
    }

    @SuppressWarnings("unchecked")
    private void buildAndAssign(PropertyWriter method, int sequence, Object object) {
        Stream.concat(buildValueFromMethodBuilder(method, sequence, object),
                buildValueFromPropertyBuilder(method, sequence)).findFirst()
                .ifPresent(value -> method.setValue(object, value));
    }

    @SuppressWarnings("unchecked")
    private Stream<Object> buildValueFromMethodBuilder(PropertyWriter propertyWriter, int sequence, Object object) {
        return propertyBuilders.entrySet().stream()
                .filter(e -> e.getKey().test(propertyWriter))
                .map(e -> e.getValue().apply(propertyWriter, object, sequence));
    }

    @SuppressWarnings("unchecked")
    private Stream<Object> buildValueFromPropertyBuilder(PropertyWriter propertyWriter, int sequence) {
        return Stream.concat(setters.stream().filter(s -> s.isPreciseType(propertyWriter.getPropertyType())),
                setters.stream().filter(s -> s.isBaseType(propertyWriter.getPropertyType())))
                .map(t -> t.getHandler().apply(propertyWriter.getPropertyType(), propertyWriter, sequence));
    }

}
