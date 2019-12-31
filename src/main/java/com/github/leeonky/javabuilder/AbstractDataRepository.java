package com.github.leeonky.javabuilder;

import com.github.leeonky.util.BeanClass;
import com.github.leeonky.util.PropertyReader;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public abstract class AbstractDataRepository implements DataRepository {

    protected abstract <T> Collection<T> queryAll(Class<T> type);

    @Override
    public <T> List<T> query(BeanClass<T> beanClass, Map<String, Object> criteria) {
        return queryAll(beanClass.getType()).stream()
                .filter(o -> criteria.entrySet().stream()
                        .allMatch(e -> propertyMatched(beanClass, o, e.getKey(), e.getValue())))
                .collect(Collectors.toList());
    }

    @SuppressWarnings("unchecked")
    private boolean propertyMatched(BeanClass<?> beanClass, Object o, String key, Object target) {
        if (o == null)
            return false;
        if (key.contains(".")) {
            PropertyQueryChain propertyQueryChain = PropertyQueryChain.parse(key);
            PropertyReader propertyReader = beanClass.getPropertyReader(propertyQueryChain.getBaseName());
            return propertyMatched(propertyReader.getPropertyTypeWrapper(), propertyReader.getValue(o), propertyQueryChain.getCondition(), target);
        }
        PropertyReader propertyReader = beanClass.getPropertyReader(key);
        return Objects.equals(propertyReader.getValue(o), propertyReader.tryConvert(target));
    }
}
