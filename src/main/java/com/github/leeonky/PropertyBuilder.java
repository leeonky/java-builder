package com.github.leeonky;

import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import static java.util.stream.Stream.of;

public class PropertyBuilder {
    private List<TypeOperator<TriFunction>> setters = new ArrayList<>();

    public static PropertyBuilder createDefaultPropertyBuilder() {
        return new PropertyBuilder()
                .addPropertyBuilder(String.class, (c, p, i) -> p + i);
    }

    public <T> PropertyBuilder addPropertyBuilder(Class<T> type, TriFunction<Class<T>, String, Integer, T> builder) {
        setters.add(new TypeOperator<>(type, builder));
        return this;
    }

    public <T> T apply(int sequence, T object) {
        of(object.getClass().getMethods())
                .filter(this::isSetter)
                .forEach(m -> buildAndAssign(m, sequence, object));
        return object;
    }

    private boolean isSetter(Method m) {
        return m.getName().startsWith("set") && m.getParameterTypes().length == 1;
    }

    private <T> void buildAndAssign(Method method, int sequence, T object) {
        setters.stream()
                .filter(s -> s.isPreciseType(method.getParameterTypes()[0]))
                .findFirst()
                .ifPresent(t -> {
                    Object value = t.getConverter().apply(method.getParameterTypes()[0], toPrepertyName(method), sequence);
                    try {
                        method.invoke(object, value);
                    } catch (IllegalAccessException | InvocationTargetException e) {
                        e.printStackTrace();
                    }
                });
    }

    private String toPrepertyName(Method method) {
        return StringUtils.uncapitalize(method.getName().replaceFirst("^set", ""));
    }
}
