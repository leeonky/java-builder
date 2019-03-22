package com.github.leeonky.javabuilder;

import java.util.function.Consumer;

class DefaultBeanFactory<T> extends BeanFactory<T> {
    DefaultBeanFactory(Class<T> type, Consumer<PropertyBuilder> propertyRegister) {
        super(type, (o, i, p) -> {
            PropertyBuilder defaultPropertyBuilder = PropertyBuilder.createDefaultPropertyBuilder();
            propertyRegister.accept(defaultPropertyBuilder);
            defaultPropertyBuilder.apply(i, o);
        });
    }
}
