package com.github.leeonky;

import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.function.Predicate;

import static java.util.stream.Stream.of;

public class BeanUtil<T> {
    private final T object;

    public BeanUtil(T object) {
        this.object = object;
    }

    public T assignProperties(Map<String, Object> properties) {
        properties.entrySet().forEach(this::assignProperty);
        return object;
    }

    private void assignProperty(Map.Entry<String, Object> e) {
        Method method = getMethod(e.getKey());
        try {
            method.invoke(object, e.getValue());
        } catch (Exception ex) {
            throw new IllegalStateException(String.format("Got exception in '%s::%s'", object.getClass().getName(), method.getName()), ex);
        }
    }

    private Method getMethod(String propertyName) {
        return of(object.getClass().getMethods())
                .filter(isSetter(propertyName))
                .findFirst().orElseThrow(() -> new IllegalStateException(String.format("No setter was found in '%s' for property '%s'", object.getClass().getName(), propertyName)));
    }

    private Predicate<Method> isSetter(String propertyName) {
        return m -> m.getName().equals("set" + StringUtils.capitalize(propertyName)) && m.getParameterTypes().length == 1;
    }
}
