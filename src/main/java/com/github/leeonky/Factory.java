package com.github.leeonky;

import java.util.Map;

public interface Factory<T> {
    T createObject(Map<String, Object> params);
}
