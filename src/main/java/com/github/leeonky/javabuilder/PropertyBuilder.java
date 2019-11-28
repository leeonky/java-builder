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
                .registerFromType(String.class, (c, p, buildingContext) -> p.getName() + buildingContext.getSequence())
                .registerFromType(long.class, (c, p, buildingContext) -> (long) buildingContext.getSequence())
                .registerFromType(int.class, (c, p, buildingContext) -> buildingContext.getSequence())
                .registerFromType(short.class, (c, p, buildingContext) -> (short) buildingContext.getSequence())
                .registerFromType(byte.class, (c, p, buildingContext) -> (byte) buildingContext.getSequence())
                .registerFromType(double.class, (c, p, buildingContext) -> (double) buildingContext.getSequence())
                .registerFromType(float.class, (c, p, buildingContext) -> (float) buildingContext.getSequence())
                .registerFromType(boolean.class, (c, p, buildingContext) -> (buildingContext.getSequence() % 2) != 0)
                .registerFromType(Long.class, (c, p, buildingContext) -> (long) buildingContext.getSequence())
                .registerFromType(Integer.class, (c, p, buildingContext) -> buildingContext.getSequence())
                .registerFromType(Short.class, (c, p, buildingContext) -> (short) buildingContext.getSequence())
                .registerFromType(Byte.class, (c, p, buildingContext) -> (byte) buildingContext.getSequence())
                .registerFromType(Double.class, (c, p, buildingContext) -> (double) buildingContext.getSequence())
                .registerFromType(Float.class, (c, p, buildingContext) -> (float) buildingContext.getSequence())
                .registerFromType(Boolean.class, (c, p, buildingContext) -> (buildingContext.getSequence() % 2) != 0)
                .registerFromType(BigInteger.class, (c, p, buildingContext) -> BigInteger.valueOf(buildingContext.getSequence()))
                .registerFromType(BigDecimal.class, (c, p, buildingContext) -> BigDecimal.valueOf(buildingContext.getSequence()))
                .registerFromType(UUID.class, (c, p, buildingContext) -> UUID.fromString(String.format("00000000-0000-0000-0000-%012d", buildingContext.getSequence())))
                .registerFromType(Instant.class, (c, p, buildingContext) -> INSTANT_START.plusSeconds(buildingContext.getSequence()))
                .registerFromType(Date.class, (c, p, buildingContext) -> Date.from(INSTANT_START.plus(buildingContext.getSequence(), ChronoUnit.DAYS)))
                .registerFromType(LocalTime.class, (c, p, buildingContext) -> LOCAL_TIME_START.plusSeconds(buildingContext.getSequence()))
                .registerFromType(LocalDate.class, (c, p, buildingContext) -> LOCAL_DATE_START.plusDays(buildingContext.getSequence()))
                .registerFromType(LocalDateTime.class, (c, p, buildingContext) -> LOCAL_DATE_TIME_START.plusSeconds(buildingContext.getSequence()))
                .registerFromType(OffsetDateTime.class, (c, p, buildingContext) -> INSTANT_START.plusSeconds(buildingContext.getSequence()).atZone(ZoneId.systemDefault()).toOffsetDateTime())
                .registerFromType(ZonedDateTime.class, (c, p, buildingContext) -> INSTANT_START.plusSeconds(buildingContext.getSequence()).atZone(ZoneId.systemDefault()))
                .registerFromType(Enum.class, (c, p, buildingContext) -> {
                    Enum[] enums = c.getEnumConstants();
                    return enums[(buildingContext.getSequence() - 1) % enums.length];
                });
    }

    public <T, B> PropertyBuilder registerFromType(Class<T> propertyType,
                                                   TriFunction<Class<T>, PropertyWriter<B>, BuildingContext<B>, T> builder) {
        setters.add(new TypeHandler<>(propertyType, builder));
        return this;
    }

    public <B> PropertyBuilder registerFromProperty(Predicate<PropertyWriter<B>> predicate,
                                                    TriFunction<PropertyWriter<B>, Object, BuildingContext<B>, Object> builder) {
        propertyBuilders.put(predicate, builder);
        return this;
    }

    public PropertyBuilder skipProperty(Predicate<PropertyWriter<?>> predicate) {
        skipper.add(predicate);
        return this;
    }

    @SuppressWarnings("unchecked")
    public <T> T buildDefaultProperty(T object, BuildingContext<T> buildingContext) {
        buildingContext.getBeanClass().getPropertyWriters()
                .values().stream()
                .filter(propertyWriter -> skipper.stream().noneMatch(p -> p.test(propertyWriter)))
                .filter(propertyWriter -> buildingContext.notSpecified(propertyWriter.getName()))
                .forEach(propertyWriter -> buildDefaultProperty(object, propertyWriter, buildingContext));
        return object;
    }

    @SuppressWarnings("unchecked")
    private void buildDefaultProperty(Object object, PropertyWriter propertyWriter, BuildingContext<?> buildingContext) {
        Stream.concat(buildValueFromMethodBuilder(propertyWriter, object, buildingContext),
                buildValueFromPropertyBuilder(propertyWriter, buildingContext)).findFirst()
                .ifPresent(value -> propertyWriter.setValue(object, value));
    }

    @SuppressWarnings("unchecked")
    private Stream<Object> buildValueFromMethodBuilder(PropertyWriter propertyWriter, Object object, BuildingContext<?> buildingContext) {
        return propertyBuilders.entrySet().stream()
                .filter(e -> e.getKey().test(propertyWriter))
                .map(e -> e.getValue().apply(propertyWriter, object, buildingContext));
    }

    @SuppressWarnings("unchecked")
    private Stream<Object> buildValueFromPropertyBuilder(PropertyWriter propertyWriter, BuildingContext<?> buildingContext) {
        return Stream.concat(setters.stream().filter(s -> s.isPreciseType(propertyWriter.getPropertyType())),
                setters.stream().filter(s -> s.isBaseType(propertyWriter.getPropertyType())))
                .map(t -> t.getHandler().apply(propertyWriter.getPropertyType(), propertyWriter, buildingContext));
    }

}

