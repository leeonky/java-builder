package com.github.leeonky;

import java.util.Map;

public interface Builder<T> {
    Builder<T> params(Map<String, Object> params);

    T build();
}
