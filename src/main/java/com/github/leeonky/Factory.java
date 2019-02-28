package com.github.leeonky;

import java.util.Map;

public interface Factory<T> {
    int getSequence();

    T createObject(int sequence, Map<String, Object> params);

    Factory<T> extend(String name, TriConsumer<T, Integer, Map<String, Object>> consumer);

    Factory query(String extend);
}
