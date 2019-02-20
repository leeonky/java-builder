package com.github.leeonky;

import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Map;
import java.util.function.Predicate;

import static java.util.stream.Stream.of;

public class BeanUtil {
    private final Converter converter = new Converter() {{
        addTypeConverter(Object.class, String.class, Object::toString);
        addTypeConverter(String.class, long.class, Long::valueOf);
        addTypeConverter(String.class, int.class, Integer::valueOf);
        addTypeConverter(String.class, short.class, Short::valueOf);
        addTypeConverter(String.class, byte.class, Byte::valueOf);
        addTypeConverter(String.class, double.class, Double::valueOf);
        addTypeConverter(String.class, float.class, Float::valueOf);
        addTypeConverter(String.class, boolean.class, Boolean::valueOf);

        addTypeConverter(String.class, Long.class, Long::valueOf);
        addTypeConverter(String.class, Integer.class, Integer::valueOf);
        addTypeConverter(String.class, Short.class, Short::valueOf);
        addTypeConverter(String.class, Byte.class, Byte::valueOf);
        addTypeConverter(String.class, Double.class, Double::valueOf);
        addTypeConverter(String.class, Float.class, Float::valueOf);
        addTypeConverter(String.class, Boolean.class, Boolean::valueOf);

        addTypeConverter(String.class, BigInteger.class, BigInteger::new);
        addTypeConverter(String.class, BigDecimal.class, BigDecimal::new);
    }};

    public Converter getConverter() {
        return converter;
    }

    public <T> T assignProperties(T object, Map<String, Object> properties) {
        properties.entrySet().stream()
                .filter(e -> e.getValue() != null)
                .forEach(e -> assignProperty(object, e));
        return object;
    }

    private void assignProperty(Object object, Map.Entry<String, Object> e) {
        Method method = getMethod(object, e.getKey());
        Class<?> valueType = method.getParameterTypes()[0];
        Object value = e.getValue();
        try {
            method.invoke(object, converter.tryConvert(valueType, value));
        } catch (Exception ex) {
            throw new IllegalStateException(String.format("Got exception in '%s::%s(%s)', value is %s[%s]",
                    object.getClass().getName(), method.getName(), valueType.getName(), value.getClass().getName(), value), ex);
        }
    }

    private Method getMethod(Object object, String propertyName) {
        return of(object.getClass().getMethods())
                .filter(isSetter(propertyName))
                .findFirst().orElseThrow(() -> new IllegalStateException(String.format("No setter was found in '%s' for property '%s'", object.getClass().getName(), propertyName)));
    }

    private Predicate<Method> isSetter(String propertyName) {
        return m -> m.getName().equals("set" + StringUtils.capitalize(propertyName)) && m.getParameterTypes().length == 1;
    }
}
