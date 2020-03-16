package com.github.leeonky.javabuilder;

import com.github.leeonky.javabuilder.spec.QueryExpression;
import com.github.leeonky.util.BeanClass;

import java.util.Collection;
import java.util.List;
import java.util.Map;
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
        return criteria.entrySet().stream()
                .map(e -> new QueryExpression<>(beanClass, e.getKey(), e.getValue()))
                .allMatch(expression -> expression.matches(object));
    }
}
