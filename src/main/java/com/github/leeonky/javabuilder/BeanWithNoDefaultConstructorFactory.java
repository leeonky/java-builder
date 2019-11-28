package com.github.leeonky.javabuilder;

import java.util.function.Function;

class BeanWithNoDefaultConstructorFactory<T> extends AbstractFactory<T> {
    private final Function<BuildingContext<T>, T> supplier;

    BeanWithNoDefaultConstructorFactory(Class<T> type, Function<BuildingContext<T>, T> supplier) {
        super(type);
        this.supplier = supplier;
    }

    @Override
    public T newInstance(BuildingContext<T> buildingContext) {
        return supplier.apply(buildingContext);
    }
}
