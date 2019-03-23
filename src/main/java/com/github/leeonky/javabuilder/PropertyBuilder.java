package com.github.leeonky.javabuilder;

import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
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

import static java.util.stream.Stream.of;

public class PropertyBuilder {
    public static final LocalDate LOCAL_DATE_START = LocalDate.parse("1996-01-23");
    public static final LocalDateTime LOCAL_DATE_TIME_START = LocalDateTime.parse("1996-01-23T00:00:00");
    private static final LocalTime LOCAL_TIME_START = LocalTime.parse("00:00:00");
    private static final Instant INSTANT_START = Instant.parse("1996-01-23T00:00:00Z");
    private List<TypeOperator<TriFunction>> setters = new ArrayList<>();
    private Map<Predicate<Method>, TriFunction<Method, Object, Integer, Object>> methodBuilders = new LinkedHashMap<>();
    private List<Predicate<Method>> skipper = new ArrayList<>();

    public static PropertyBuilder createDefaultPropertyBuilder() {
        return new PropertyBuilder()
                .addPropertyBuilder(String.class, (c, p, i) -> p + i)
                .addPropertyBuilder(long.class, (c, p, i) -> (long) i)
                .addPropertyBuilder(int.class, (c, p, i) -> i)
                .addPropertyBuilder(short.class, (c, p, i) -> (short) i.intValue())
                .addPropertyBuilder(byte.class, (c, p, i) -> (byte) i.intValue())
                .addPropertyBuilder(double.class, (c, p, i) -> (double) i)
                .addPropertyBuilder(float.class, (c, p, i) -> (float) i)
                .addPropertyBuilder(boolean.class, (c, p, i) -> (i % 2) != 0)
                .addPropertyBuilder(Long.class, (c, p, i) -> (long) i)
                .addPropertyBuilder(Integer.class, (c, p, i) -> i)
                .addPropertyBuilder(Short.class, (c, p, i) -> (short) i.intValue())
                .addPropertyBuilder(Byte.class, (c, p, i) -> (byte) i.intValue())
                .addPropertyBuilder(Double.class, (c, p, i) -> (double) i)
                .addPropertyBuilder(Float.class, (c, p, i) -> (float) i)
                .addPropertyBuilder(Boolean.class, (c, p, i) -> (i % 2) != 0)
                .addPropertyBuilder(BigInteger.class, (c, p, i) -> BigInteger.valueOf(i))
                .addPropertyBuilder(BigDecimal.class, (c, p, i) -> BigDecimal.valueOf(i))
                .addPropertyBuilder(UUID.class, (c, p, i) -> UUID.fromString(String.format("00000000-0000-0000-0000-%012d", i)))
                .addPropertyBuilder(Instant.class, (c, p, i) -> INSTANT_START.plusSeconds(i))
                .addPropertyBuilder(Date.class, (c, p, i) -> Date.from(INSTANT_START.plus(i - 1, ChronoUnit.DAYS)))
                .addPropertyBuilder(LocalTime.class, (c, p, i) -> LOCAL_TIME_START.plusSeconds(i))
                .addPropertyBuilder(LocalDate.class, (c, p, i) -> LOCAL_DATE_START.plusDays(i - 1))
                .addPropertyBuilder(LocalDateTime.class, (c, p, i) -> LOCAL_DATE_TIME_START.plusSeconds(i))
                .addPropertyBuilder(Enum.class, (c, p, i) -> {
                    Enum[] enums = c.getEnumConstants();
                    return enums[(i - 1) % enums.length];
                });
    }

    public <T> PropertyBuilder addPropertyBuilder(Class<T> type, TriFunction<Class<T>, String, Integer, T> builder) {
        setters.add(new TypeOperator<>(type, builder));
        return this;
    }

    public PropertyBuilder addMethodBuilder(Predicate<Method> predicate, TriFunction<Method, Object, Integer, Object> builder) {
        methodBuilders.put(predicate, builder);
        return this;
    }

    public PropertyBuilder addSkipMethod(Predicate<Method> predicate) {
        skipper.add(predicate);
        return this;
    }

    public <T> T apply(int sequence, T object) {
        of(object.getClass().getMethods())
                .filter(this::isSetter)
                .filter(method -> !skipper.stream().anyMatch(p -> p.test(method)))
                .forEach(m -> buildAndAssign(m, sequence, object));
        return object;
    }

    private boolean isSetter(Method m) {
        return m.getName().startsWith("set") && m.getParameterTypes().length == 1;
    }

    @SuppressWarnings("unchecked")
    private <T> void buildAndAssign(Method method, int sequence, T object) {
        Stream.concat(buildValueFromMethodBuilder(method, sequence, object),
                buildValueFromPropertyBuilder(method, sequence)).findFirst()
                .ifPresent(value -> {
                    try {
                        method.invoke(object, value);
                    } catch (IllegalAccessException | InvocationTargetException e) {
                        throw new IllegalStateException("Got exception in " + method.getDeclaringClass().getName() + "." + method.getName(), e);
                    }
                });
    }

    private <T> Stream<Object> buildValueFromMethodBuilder(Method method, int sequence, T object) {
        return methodBuilders.entrySet().stream()
                .filter(e -> e.getKey().test(method))
                .map(e -> e.getValue().apply(method, object, sequence));
    }

    private Stream<Object> buildValueFromPropertyBuilder(Method method, int sequence) {
        return Stream.concat(setters.stream().filter(s -> s.isPreciseType(method.getParameterTypes()[0])),
                setters.stream().filter(s -> s.isBaseType(method.getParameterTypes()[0])))
                .map(t -> t.getConverter().apply(method.getParameterTypes()[0], toPropertyName(method), sequence));
    }

    private String toPropertyName(Method method) {
        return StringUtils.uncapitalize(method.getName().replaceFirst("^set", ""));
    }
}
