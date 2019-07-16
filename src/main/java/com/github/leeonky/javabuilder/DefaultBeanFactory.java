package com.github.leeonky.javabuilder;

class DefaultBeanFactory<T> extends BeanFactory<T> {
    DefaultBeanFactory(Class<T> type, FactorySet factorySet) {
        super(factorySet, type, (o, bc) -> bc.assignTo(o).setDefault());
    }
}
