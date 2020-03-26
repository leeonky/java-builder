package com.github.leeonky.javabuilder.spec;

import com.github.leeonky.javabuilder.BeanContextImpl;
import com.github.leeonky.javabuilder.Builder;
import com.github.leeonky.javabuilder.BuildingContext;
import com.github.leeonky.javabuilder.FactorySet;
import com.github.leeonky.util.BeanClass;
import com.github.leeonky.util.PropertyReader;

import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

    private Class<?> getWritePropertyType() {
        return beanClass.getPropertyWriter(baseName).getPropertyType();
    }

    private Builder<?> toBuilder(FactorySet factorySet, Class<?> type) {
        return (specName != null ? factorySet.toBuild(specName) : factorySet.type(type))
                .combine(combinations)
                .property(condition, value);
    }

    public boolean sameWith(QueryExpression another) {
        return beanClass.getType().equals(another.beanClass.getType())
                && getWritePropertyType().equals(another.getWritePropertyType())
                && (isDefaultBuild(another) || (Arrays.equals(combinations, another.combinations) && Objects.equals(specName, another.specName)))
                && Objects.equals(condition, another.condition)
                && Objects.equals(value, another.value);
    }

    private boolean isDefaultBuild(QueryExpression another) {
        return another.specName == null && another.combinations.length == 0;
    }

    @SuppressWarnings("unchecked")
    public boolean matches(Object object) {
        if (object == null)
            return false;
        PropertyReader propertyReader = beanClass.getPropertyReader(baseName);
        Object propertyValue = propertyReader.getValue(object);
        if (condition == null)
            return Objects.equals(propertyValue, propertyReader.tryConvert(value));
        return new QueryExpression(propertyReader.getPropertyTypeWrapper(), condition, value).matches(propertyValue);
    }

    private boolean queryTo(FactorySet factorySet, Map<String, Object> queried) {
        if (condition == null) {
            queried.put(baseName, value);
            return true;
        }
        return toBuilder(factorySet, beanClass.getPropertyReader(baseName).getPropertyType()).query().stream()
                .peek(o -> queried.put(baseName, o))
                .findFirst()
                .isPresent();
    }

    public void queryOrCreateTo(FactorySet factorySet, BuildingContext buildingContext, BeanContextImpl<T> beanContext, Map<String, Object> queried, Set<String> created) {
        if (!queryTo(factorySet, queried)) {
            beanContext.processSubCreate(baseName, toBuilder(factorySet, getWritePropertyType()),
                    creator -> buildingContext.appendPropertiesSpec(new PropertySpec(beanContext.propertyChain(baseName), creator, this)));
            created.add(baseName);
        }
    }
}
