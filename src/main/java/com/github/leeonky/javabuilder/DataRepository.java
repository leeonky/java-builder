package com.github.leeonky.javabuilder;

import com.github.leeonky.util.BeanClass;
import com.github.leeonky.util.PropertyReader;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public interface DataRepository {

    void save(Object object);

    <T> Collection<T> queryAll(Class<T> type);

    void clear();

    default <T> Optional<T> query(BeanClass<T> beanClass, Map<String, Object> properties) {
        return queryAll(beanClass.getType())
                .stream()
                .filter(o -> properties.entrySet().stream().noneMatch(e -> {
                    PropertyReader<T> propertyReader = beanClass.getPropertyReader(e.getKey());
                    return !Objects.equals(propertyReader.getValue(o), propertyReader.tryConvert(e.getValue()));
                })).findFirst();
    }
}
