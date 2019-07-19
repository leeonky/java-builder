package com.github.leeonky.javabuilder;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

public class FactoryDefinition<T> {
    public void onBuild(T object, BuildContext<T> beanBuildContext) {
        beanBuildContext.assignTo(object).setDefault();
    }

    @SuppressWarnings("unchecked")
    public Class<T> getType() {
        if (getClass().getGenericSuperclass() instanceof ParameterizedType) {
            Type type = ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[0];
            if (type instanceof Class)
                return (Class<T>) type;
        }
        throw new IllegalStateException("Invalid FactoryDefinition '" + getClass().getName() +
                "' should specify generic type or override getType() method");
    }

    public String getAlias() {
        return getClass().getSimpleName();
    }
}
