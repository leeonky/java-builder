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
        return queryAll(beanClass.getType()).stream()
                .filter(o -> properties.entrySet().stream().noneMatch(e ->
                        notEquals(beanClass, o, e.getKey(), e.getValue()))).findFirst();
    }

    @SuppressWarnings("unchecked")
    private boolean notEquals(BeanClass<?> beanClass, Object o, String key, Object target) {
        if (o == null)
            return true;
        if (key.contains(".")) {
            String[] propertyList = key.split("\\.", 2);
            String propertyName = propertyList[0];
            if (propertyName.contains("("))
                propertyName = propertyName.split("\\(", 2)[0];
            PropertyReader propertyReader = beanClass.getPropertyReader(propertyName);
            return notEquals(propertyReader.getPropertyTypeWrapper(), propertyReader.getValue(o), propertyList[1], target);
        }
        PropertyReader propertyReader = beanClass.getPropertyReader(key);
        return !Objects.equals(propertyReader.getValue(o), propertyReader.tryConvert(target));
    }
}
