package com.github.leeonky;

public class Converter {
    public Object convert(Class<?> type, Object value) {
        if (value != null)
            if (value.getClass() != type)
                return value.toString();
        return value;
    }
}
