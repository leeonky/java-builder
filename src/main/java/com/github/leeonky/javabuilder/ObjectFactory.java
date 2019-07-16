package com.github.leeonky.javabuilder;

import java.util.function.Function;

class ObjectFactory<T> extends AbstractFactory<T> {
    private Function<BuildContext<T>, T> supplier;

    ObjectFactory(FactorySet factorySet, Class<T> type, Function<BuildContext<T>, T> supplier) {
        super(factorySet, type);
        this.supplier = supplier;
    }

    @Override
    public T createObject(BuildContext<T> buildContext) {
        return supplier.apply(buildContext);
    }
}
