package com.github.leeonky;

import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.function.Predicate;

import static java.util.stream.Stream.of;

public class BeanUtil {
    private final Converter converter = new Converter() {{
        addTypeConverter(Object.class, String.class, Object::toString);
        addTypeConverter(String.class, long.class, Long::valueOf);
        addTypeConverter(String.class, int.class, Integer::valueOf);
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
        try {
            method.invoke(object, converter.convert(method.getParameterTypes()[0], e.getValue()));
        } catch (Exception ex) {
            throw new IllegalStateException(String.format("Got exception in '%s::%s'", object.getClass().getName(), method.getName()), ex);
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
