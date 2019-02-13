package com.github.leeonky;

import java.util.Map;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public interface Builder<T> {
    Builder<T> params(Map<String, Object> params);

    Builder<T> properties(Map<String, Object> properties);

    T build();

    default Stream<T> build(int count) {
        return IntStream.range(0, count).mapToObj(i -> build());
    }
}
