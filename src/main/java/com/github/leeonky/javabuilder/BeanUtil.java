package com.github.leeonky.javabuilder;

import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.function.Predicate;

import static java.util.stream.Stream.of;

public class BeanUtil {
    private final Converter converter = Converter.createDefaultConverter();

    public static Object getPropertyValue(Object instance, String name) throws Exception {
        try {
            return getGetter(instance.getClass(), name).invoke(instance);
        } catch (Exception e) {
            return instance.getClass().getField(name).get(instance);
        }
    }

    private static String capitalize(String str) {
        return str.isEmpty() ? str : str.toUpperCase().substring(0, 1) + str.substring(1);
    }

    public static Method getGetter(Class<?> type, String name) throws Exception {
        try {
            Method method = type.getMethod("get" + capitalize(name));
            if (isGetter(method))
                return method;
            return getBooleanGetter(type, name);
        } catch (Exception ex) {
            return getBooleanGetter(type, name);
        }
    }

    private static Method getBooleanGetter(Class<?> type, String name) throws NoSuchMethodException {
        Method method;
        method = type.getMethod("is" + capitalize(name));
        if (isGetter(method))
            return method;
        throw new IllegalStateException("No getter for " + type.getName() + "." + name);
    }

    private static boolean isGetter(Method m) {
        if (m.getParameters().length == 0) {
            if (m.getName().startsWith("get") && !m.getReturnType().equals(void.class) && !m.getName().equals("getClass"))
                return true;
            return m.getName().startsWith("is") && m.getReturnType().equals(boolean.class);
        }
        return false;
    }

    public Converter getConverter() {
        return converter;
    }

    public <T> T assignProperties(T object, Map<String, ?> properties) {
        properties.entrySet().stream()
                .filter(e -> e.getValue() != null)
                .forEach(e -> assignProperty(object, e));
        return object;
    }

    private void assignProperty(Object object, Map.Entry<String, ?> e) {
        Method method = getSetter(object, e.getKey());
        Class<?> valueType = method.getParameterTypes()[0];
        Object value = e.getValue();
        try {
            method.invoke(object, converter.tryConvert(valueType, value));
        } catch (Exception ex) {
            throw new IllegalStateException(String.format("Got exception in '%s::%s(%s)', value is %s[%s]",
                    object.getClass().getName(), method.getName(), valueType.getName(), value.getClass().getName(), value), ex);
        }
    }

    private Method getSetter(Object object, String propertyName) {
        return of(object.getClass().getMethods())
                .filter(isSetter(propertyName))
                .findFirst().orElseThrow(() -> new IllegalStateException(String.format("No setter was found in '%s' for property '%s'", object.getClass().getName(), propertyName)));
    }

    private Predicate<Method> isSetter(String propertyName) {
        return m -> m.getName().equals("set" + StringUtils.capitalize(propertyName)) && m.getParameterTypes().length == 1;
    }
}
