package com.github.leeonky.javabuilder;

import java.util.*;

public class DefaultDataRepository implements DataRepository {
    private static final HashSet<Object> EMPTY_SET = new HashSet<>();
    private Map<Class<?>, Set<Object>> repo = new HashMap<>();

    @Override
    public void save(Object object) {
        if (object != null)
            repo.computeIfAbsent(object.getClass(), c -> new HashSet<>()).add(object);
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
