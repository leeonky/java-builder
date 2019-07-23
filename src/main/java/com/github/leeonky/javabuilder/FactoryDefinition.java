package com.github.leeonky.javabuilder;

import com.github.leeonky.util.GenericType;

public class FactoryDefinition<T> {
    public void onBuild(T object, BuildContext<T> beanBuildContext) {
        beanBuildContext.assignTo(object).setDefault();
    }

    @SuppressWarnings("unchecked")
    public Class<T> getType() {
        return (Class<T>) GenericType.createGenericType(getClass().getGenericSuperclass()).getGenericTypeParameter(0)
                .orElseThrow(() -> new IllegalStateException("Invalid FactoryDefinition '" + getClass().getName() +
                        "' should specify generic type or override getType() method"))
                .getRawType();
    }

    public String getAlias() {
        return getClass().getSimpleName();
    }
}
