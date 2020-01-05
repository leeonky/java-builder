package com.github.leeonky.javabuilder;

import com.github.leeonky.util.GenericType;

public abstract class BeanSpecs<T> {
    public void specs(SpecBuilder<T> specBuilder) {
    }

    @SuppressWarnings("unchecked")
    public Class<T> getType() {
        return (Class<T>) GenericType.createGenericType(getClass().getGenericSuperclass()).getGenericTypeParameter(0)
                .orElseThrow(() -> new IllegalStateException(String.format("Invalid FactoryDefinition '%s' should specify generic type or override getType() method", getClass().getName())))
                .getRawType();
    }

    public String getName() {
        return getClass().getSimpleName();
    }
}
