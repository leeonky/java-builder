package com.github.leeonky.javabuilder;

import com.github.leeonky.javabuilder.spec.PropertyQueryChain;
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
                .filter(o -> criteriaMatches(o, beanClass, criteria))
                .collect(Collectors.toList());
    }

    private <T> boolean criteriaMatches(T object, BeanClass<T> beanClass, Map<String, Object> criteria) {
        return criteria.entrySet().stream().allMatch(e -> isPropertyValueMatched(beanClass, object, e.getKey(), e.getValue()));
    }

    @SuppressWarnings("unchecked")
    private boolean isPropertyValueMatched(BeanClass<?> beanClass, Object o, String key, Object target) {
        if (o == null)
            return false;
        if (key.contains(".")) {
            PropertyQueryChain propertyQueryChain = PropertyQueryChain.parse(key);
            PropertyReader propertyReader = beanClass.getPropertyReader(propertyQueryChain.getBaseName());
            return isPropertyValueMatched(propertyReader.getPropertyTypeWrapper(), propertyReader.getValue(o), propertyQueryChain.getCondition(), target);
        }
        PropertyReader propertyReader = beanClass.getPropertyReader(key);
        return Objects.equals(propertyReader.getValue(o), propertyReader.tryConvert(target));
    }
}
