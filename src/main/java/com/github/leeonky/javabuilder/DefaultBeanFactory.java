package com.github.leeonky.javabuilder;

class DefaultBeanFactory<T> extends BeanFactory<T> {
    DefaultBeanFactory(Class<T> type, FactoryConfiguration factoryConfiguration) {
        super(type, (o, bc) -> factoryConfiguration.getPropertyBuilder().apply(o, bc), factoryConfiguration);
    }
}
