package com.github.leeonky.javabuilder;

import com.github.leeonky.util.BeanClass;
import com.github.leeonky.util.PropertyReader;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public abstract class AbstractDataRepository implements DataRepository {

    protected abstract <T> Collection<T> queryAll(Class<T> type);

    @Override
    public <T> Optional<T> query(BeanClass<T> beanClass, Map<String, Object> properties) {
        return queryAll(beanClass.getType())
                .stream()
                .filter(o -> properties.entrySet().stream().noneMatch(e -> {
                    return notEquals(beanClass, o, e.getKey(), e.getValue());
                })).findFirst();
    }

    private boolean notEquals(BeanClass<?> beanClass, Object o, String key, Object target) {
        if (key.contains(".")) {
            String[] propertyList = key.split("\\.", 2);
            String property = propertyList[0];
            String condition = propertyList[1];
            PropertyReader propertyReader = beanClass.getPropertyReader(property);
            return notEquals(propertyReader.getPropertyTypeWrapper(), propertyReader.getValue(o), condition, target);
        }
        PropertyReader propertyReader = beanClass.getPropertyReader(key);
        return !Objects.equals(propertyReader.getValue(o), propertyReader.tryConvert(target));
    }
}
