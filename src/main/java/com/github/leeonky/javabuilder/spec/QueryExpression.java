package com.github.leeonky.javabuilder.spec;

import com.github.leeonky.javabuilder.Builder;
import com.github.leeonky.javabuilder.FactorySet;
import com.github.leeonky.util.BeanClass;

import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.util.Collections.singletonList;

public class QueryExpression<T> {
    private final BeanClass<T> beanClass;
    private final Object value;
    private final String baseName;
    private final String[] combinations;
    private final String specName, condition;

    public QueryExpression(BeanClass<T> beanClass, String chain, Object value) {
        this.beanClass = beanClass;
        this.value = value;
        Matcher matcher;
        if ((matcher = Pattern.compile("([^.]+)\\((.+)[, |,| ](.+)\\)\\.(.+)").matcher(chain)).matches()) {
            baseName = matcher.group(1);
            combinations = matcher.group(2).split(", |,| ");
            specName = matcher.group(3);
            condition = matcher.group(4);
        } else if ((matcher = Pattern.compile("([^.]+)\\((.+)\\)\\.(.+)").matcher(chain)).matches()) {
            baseName = matcher.group(1);
            combinations = new String[0];
            specName = matcher.group(2);
            condition = matcher.group(3);
        } else if ((matcher = Pattern.compile("([^.]+)\\.(.+)").matcher(chain)).matches()) {
            baseName = matcher.group(1);
            combinations = new String[0];
            specName = null;
            condition = matcher.group(2);
        } else if ((matcher = Pattern.compile("([^.]+)").matcher(chain)).matches()) {
            baseName = matcher.group(1);
            combinations = new String[0];
            specName = null;
            condition = null;
        } else
            throw new IllegalStateException("Invalid query expression `" + chain + "`");
    }

    public String getBaseName() {
        return baseName;
    }

    public String getCondition() {
        return condition;
    }

    public Builder<?> forCreating(FactorySet factorySet) {
        return toBuilder(factorySet, getWritePropertyType());
    }

    private Class<?> getWritePropertyType() {
        return beanClass.getPropertyWriter(getBaseName()).getPropertyType();
    }

    public List<?> query(FactorySet factorySet) {
        if (condition == null)
            return singletonList(value);
        return toBuilder(factorySet, beanClass.getPropertyReader(getBaseName()).getPropertyType()).query();
    }

    private Builder<?> toBuilder(FactorySet factorySet, Class<?> type) {
        return (specName != null ? factorySet.toBuild(specName) : factorySet.type(type))
                .combine(combinations)
                .property(condition, value);
    }

    public boolean isSameQuery(QueryExpression another) {
        return getWritePropertyType().equals(another.getWritePropertyType())
                && condition.equals(another.condition)
                && Objects.equals(value, another.value);
    }
}
