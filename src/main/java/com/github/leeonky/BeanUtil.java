package com.github.leeonky;

import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.Method;
import java.util.Map;

public class BeanUtil<T> {
    private final T object;

    public BeanUtil(T object) {
        this.object = object;
    }

    public T assignProperties(Map<String, Object> properties) {
        properties.entrySet().forEach(e -> {
            Method[] methods = object.getClass().getMethods();
            Method method = null;
            for (Method m : methods) {
                if (m.getName().equals("set" + StringUtils.capitalize(e.getKey())) && m.getParameterTypes().length == 1)
                    method = m;
            }
            try {
                method.invoke(object, e.getValue());
            } catch (Exception ex) {
                throw new IllegalStateException(ex);
            }
        });
        return object;
    }
}
