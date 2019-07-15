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
                .registerFromType(String.class, (c, p, buildContext) -> p.getName() + buildContext.getSequence())
                .registerFromType(long.class, (c, p, buildContext) -> (long) buildContext.getSequence())
                .registerFromType(int.class, (c, p, buildContext) -> buildContext.getSequence())
                .registerFromType(short.class, (c, p, buildContext) -> (short) buildContext.getSequence())
                .registerFromType(byte.class, (c, p, buildContext) -> (byte) buildContext.getSequence())
                .registerFromType(double.class, (c, p, buildContext) -> (double) buildContext.getSequence())
                .registerFromType(float.class, (c, p, buildContext) -> (float) buildContext.getSequence())
                .registerFromType(boolean.class, (c, p, buildContext) -> (buildContext.getSequence() % 2) != 0)
                .registerFromType(Long.class, (c, p, buildContext) -> (long) buildContext.getSequence())
                .registerFromType(Integer.class, (c, p, buildContext) -> buildContext.getSequence())
                .registerFromType(Short.class, (c, p, buildContext) -> (short) buildContext.getSequence())
                .registerFromType(Byte.class, (c, p, buildContext) -> (byte) buildContext.getSequence())
                .registerFromType(Double.class, (c, p, buildContext) -> (double) buildContext.getSequence())
                .registerFromType(Float.class, (c, p, buildContext) -> (float) buildContext.getSequence())
                .registerFromType(Boolean.class, (c, p, buildContext) -> (buildContext.getSequence() % 2) != 0)
                .registerFromType(BigInteger.class, (c, p, buildContext) -> BigInteger.valueOf(buildContext.getSequence()))
                .registerFromType(BigDecimal.class, (c, p, buildContext) -> BigDecimal.valueOf(buildContext.getSequence()))
                .registerFromType(UUID.class, (c, p, buildContext) -> UUID.fromString(String.format("00000000-0000-0000-0000-%012d", buildContext.getSequence())))
                .registerFromType(Instant.class, (c, p, buildContext) -> INSTANT_START.plusSeconds(buildContext.getSequence()))
                .registerFromType(Date.class, (c, p, buildContext) -> Date.from(INSTANT_START.plus(buildContext.getSequence() - 1, ChronoUnit.DAYS)))
                .registerFromType(LocalTime.class, (c, p, buildContext) -> LOCAL_TIME_START.plusSeconds(buildContext.getSequence()))
                .registerFromType(LocalDate.class, (c, p, buildContext) -> LOCAL_DATE_START.plusDays(buildContext.getSequence() - 1))
                .registerFromType(LocalDateTime.class, (c, p, buildContext) -> LOCAL_DATE_TIME_START.plusSeconds(buildContext.getSequence()))
                .registerFromType(Enum.class, (c, p, buildContext) -> {
                    Enum[] enums = c.getEnumConstants();
                    return enums[(buildContext.getSequence() - 1) % enums.length];
                });
    }

    public <T, B> PropertyBuilder registerFromType(Class<T> propertyType,
                                                   TriFunction<Class<T>, PropertyWriter<B>, BuildContext, T> builder) {
        setters.add(new TypeHandler<>(propertyType, builder));
        return this;
    }

    public <B> PropertyBuilder registerFromProperty(Predicate<PropertyWriter<B>> predicate,
                                                    TriFunction<PropertyWriter<B>, Object, BuildContext, Object> builder) {
        propertyBuilders.put(predicate, builder);
        return this;
    }

    public <B> PropertyBuilder skipProperty(Predicate<PropertyWriter<B>> predicate) {
        skipper.add(predicate);
        return this;
    }

    @SuppressWarnings("unchecked")
    public <T> T apply(T object, BuildContext buildContext) {
        new BeanClass<>(object.getClass()).getPropertyWriters()
                .values().stream()
                .filter(propertyWriter -> skipper.stream().noneMatch(p -> p.test(propertyWriter)))
                .forEach(propertyWriter -> buildAndAssign(propertyWriter, object, buildContext));
        return object;
    }

    @SuppressWarnings("unchecked")
    private void buildAndAssign(PropertyWriter method, Object object, BuildContext buildContext) {
        Stream.concat(buildValueFromMethodBuilder(method, object, buildContext),
                buildValueFromPropertyBuilder(method, buildContext)).findFirst()
                .ifPresent(value -> method.setValue(object, value));
    }

    @SuppressWarnings("unchecked")
    private Stream<Object> buildValueFromMethodBuilder(PropertyWriter propertyWriter, Object object, BuildContext buildContext) {
        return propertyBuilders.entrySet().stream()
                .filter(e -> e.getKey().test(propertyWriter))
                .map(e -> e.getValue().apply(propertyWriter, object, buildContext));
    }

    @SuppressWarnings("unchecked")
    private Stream<Object> buildValueFromPropertyBuilder(PropertyWriter propertyWriter, BuildContext buildContext) {
        return Stream.concat(setters.stream().filter(s -> s.isPreciseType(propertyWriter.getPropertyType())),
                setters.stream().filter(s -> s.isBaseType(propertyWriter.getPropertyType())))
                .map(t -> t.getHandler().apply(propertyWriter.getPropertyType(), propertyWriter, buildContext));
    }

}
