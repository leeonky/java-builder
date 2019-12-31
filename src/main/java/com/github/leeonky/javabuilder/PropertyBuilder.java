package com.github.leeonky.javabuilder;

import com.github.leeonky.util.PropertyWriter;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.*;
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
                .registerThroughType(String.class, (c, p, buildingContext) -> p.getName() + buildingContext.getCurrentSequence())
                .registerThroughType(long.class, (c, p, buildingContext) -> (long) buildingContext.getCurrentSequence())
                .registerThroughType(int.class, (c, p, buildingContext) -> buildingContext.getCurrentSequence())
                .registerThroughType(short.class, (c, p, buildingContext) -> (short) buildingContext.getCurrentSequence())
                .registerThroughType(byte.class, (c, p, buildingContext) -> (byte) buildingContext.getCurrentSequence())
                .registerThroughType(double.class, (c, p, buildingContext) -> (double) buildingContext.getCurrentSequence())
                .registerThroughType(float.class, (c, p, buildingContext) -> (float) buildingContext.getCurrentSequence())
                .registerThroughType(boolean.class, (c, p, buildingContext) -> (buildingContext.getCurrentSequence() % 2) != 0)
                .registerThroughType(Long.class, (c, p, buildingContext) -> (long) buildingContext.getCurrentSequence())
                .registerThroughType(Integer.class, (c, p, buildingContext) -> buildingContext.getCurrentSequence())
                .registerThroughType(Short.class, (c, p, buildingContext) -> (short) buildingContext.getCurrentSequence())
                .registerThroughType(Byte.class, (c, p, buildingContext) -> (byte) buildingContext.getCurrentSequence())
                .registerThroughType(Double.class, (c, p, buildingContext) -> (double) buildingContext.getCurrentSequence())
                .registerThroughType(Float.class, (c, p, buildingContext) -> (float) buildingContext.getCurrentSequence())
                .registerThroughType(Boolean.class, (c, p, buildingContext) -> (buildingContext.getCurrentSequence() % 2) != 0)
                .registerThroughType(BigInteger.class, (c, p, buildingContext) -> BigInteger.valueOf(buildingContext.getCurrentSequence()))
                .registerThroughType(BigDecimal.class, (c, p, buildingContext) -> BigDecimal.valueOf(buildingContext.getCurrentSequence()))
                .registerThroughType(UUID.class, (c, p, buildingContext) -> UUID.fromString(String.format("00000000-0000-0000-0000-%012d", buildingContext.getCurrentSequence())))
                .registerThroughType(Instant.class, (c, p, buildingContext) -> INSTANT_START.plusSeconds(buildingContext.getCurrentSequence()))
                .registerThroughType(Date.class, (c, p, buildingContext) -> Date.from(INSTANT_START.plus(buildingContext.getCurrentSequence(), ChronoUnit.DAYS)))
                .registerThroughType(LocalTime.class, (c, p, buildingContext) -> LOCAL_TIME_START.plusSeconds(buildingContext.getCurrentSequence()))
                .registerThroughType(LocalDate.class, (c, p, buildingContext) -> LOCAL_DATE_START.plusDays(buildingContext.getCurrentSequence()))
                .registerThroughType(LocalDateTime.class, (c, p, buildingContext) -> LOCAL_DATE_TIME_START.plusSeconds(buildingContext.getCurrentSequence()))
                .registerThroughType(OffsetDateTime.class, (c, p, buildingContext) -> INSTANT_START.plusSeconds(buildingContext.getCurrentSequence()).atZone(ZoneId.systemDefault()).toOffsetDateTime())
                .registerThroughType(ZonedDateTime.class, (c, p, buildingContext) -> INSTANT_START.plusSeconds(buildingContext.getCurrentSequence()).atZone(ZoneId.systemDefault()))
                .registerThroughType(Enum.class, (c, p, buildingContext) -> {
                    Enum[] enums = c.getEnumConstants();
                    return enums[(buildingContext.getCurrentSequence() - 1) % enums.length];
                });
    }

    public <T, B> PropertyBuilder registerThroughType(Class<T> propertyType,
                                                      TriFunction<Class<T>, PropertyWriter<B>, BeanContext<B>, T> builder) {
        setters.add(new TypeHandler<>(propertyType, builder));
        return this;
    }

    public <B> PropertyBuilder registerThroughProperty(Predicate<PropertyWriter<B>> predicate,
                                                       TriFunction<PropertyWriter<B>, Object, BeanContext<B>, Object> builder) {
        propertyBuilders.put(predicate, builder);
        return this;
    }

    public PropertyBuilder skipProperty(Predicate<PropertyWriter<?>> predicate) {
        skipper.add(predicate);
        return this;
    }

    @SuppressWarnings("unchecked")
    public <T> void assignPropertiesAsDefaultValues(T object, BeanContext<T> beanContext) {
        beanContext.getBeanClass().getPropertyWriters()
                .values().stream()
                .filter(propertyWriter -> skipper.stream().noneMatch(p -> p.test(propertyWriter)))
                .filter(propertyWriter -> beanContext.isNotSpecified(propertyWriter.getName()))
                .forEach(propertyWriter -> assignPropertyAsDefaultValue(object, propertyWriter, beanContext));
    }

    @SuppressWarnings("unchecked")
    private void assignPropertyAsDefaultValue(Object object, PropertyWriter propertyWriter, BeanContext<?> beanContext) {
        Stream.concat(buildValueFromMethodBuilder(propertyWriter, object, beanContext),
                buildValueFromPropertyBuilder(propertyWriter, beanContext)).findFirst()
                .ifPresent(value -> propertyWriter.setValue(object, value));
    }

    @SuppressWarnings("unchecked")
    private Stream<Object> buildValueFromMethodBuilder(PropertyWriter propertyWriter, Object object, BeanContext<?> beanContext) {
        return propertyBuilders.entrySet().stream()
                .filter(e -> e.getKey().test(propertyWriter))
                .map(e -> e.getValue().apply(propertyWriter, object, beanContext));
    }

    @SuppressWarnings("unchecked")
    private Stream<Object> buildValueFromPropertyBuilder(PropertyWriter propertyWriter, BeanContext<?> beanContext) {
        return Stream.concat(setters.stream().filter(s -> s.isPreciseType(propertyWriter.getPropertyType())),
                setters.stream().filter(s -> s.isBaseType(propertyWriter.getPropertyType())))
                .map(t -> t.getHandler().apply(propertyWriter.getPropertyType(), propertyWriter, beanContext));
    }

}

