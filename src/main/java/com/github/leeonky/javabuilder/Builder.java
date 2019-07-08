package com.github.leeonky.javabuilder;

import java.util.Map;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public interface Builder<T> {
    Builder<T> params(Map<String, ?> params);

    Builder<T> properties(Map<String, ?> properties);

    T build();

    default Stream<T> build(int count) {
        return IntStream.range(0, count).mapToObj(i -> build());
    }

    T query();

    Builder<T> property(String name, Object value);

    void clearRepository();
}
