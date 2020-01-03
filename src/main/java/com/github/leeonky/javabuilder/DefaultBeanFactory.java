package com.github.leeonky.javabuilder;

class DefaultBeanFactory<T> extends BeanFactory<T> {
    DefaultBeanFactory(Class<T> type) {
        super(type, (o, buildingContext) -> buildingContext.assignDefaultValueToUnSpecifiedProperties(o));
    }
}
