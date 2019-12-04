package com.github.leeonky.javabuilder;

import java.util.*;

public class HashMapDataRepository extends AbstractDataRepository {
    private static final Set<Object> EMPTY_SET = new HashSet<>();
    private Map<Class<?>, Set<Object>> repo = new HashMap<>();

    @Override
    public <T> T save(T object) {
        if (object != null)
            repo.computeIfAbsent(object.getClass(), c -> new HashSet<>()).add(object);
        return object;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> Collection<T> queryAll(Class<T> type) {
        return (Collection<T>) repo.getOrDefault(type, EMPTY_SET);
    }

    @Override
    public void clear() {
        repo.clear();
    }
}
