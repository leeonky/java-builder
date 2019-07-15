package com.github.leeonky.javabuilder;

import java.util.function.Function;

class ObjectFactory<T> extends AbstractFactory<T> {
    private Function<BuildContext<T>, T> supplier;

    ObjectFactory(Class<T> type, Function<BuildContext<T>, T> supplier, FactoryConfiguration factoryConfiguration) {
        super(type, factoryConfiguration);
        this.supplier = supplier;
    }

    @Override
    public T createObject(BuildContext<T> buildContext) {
        return supplier.apply(buildContext);
    }
}
