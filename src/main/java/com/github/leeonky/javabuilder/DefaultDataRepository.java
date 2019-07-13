package com.github.leeonky.javabuilder;

import com.github.leeonky.util.BeanClass;
import com.github.leeonky.util.PropertyReader;

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
    public <T> Optional<T> query(BeanClass<T> beanClass, Map<String, Object> properties) {
        return repo.getOrDefault(beanClass.getType(), EMPTY_SET)
                .stream()
                .map(o -> (T) o)
                .filter(o -> isCandidate(beanClass, o, properties))
                .findFirst();
    }

    private <T> boolean isCandidate(BeanClass<T> beanClass, T o, Map<String, Object> properties) {
        return properties.entrySet().stream().noneMatch(e -> {
            PropertyReader<T> propertyReader = beanClass.getPropertyReader(e.getKey());
            return !Objects.equals(propertyReader.getValue(o), propertyReader.tryConvert(e.getValue()));
        });
    }

    @Override
    public void clear() {
        repo.clear();
    }
}
