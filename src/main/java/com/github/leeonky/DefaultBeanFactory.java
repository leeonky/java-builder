package com.github.leeonky;

class DefaultBeanFactory<T> extends BeanFactory<T> {
    DefaultBeanFactory(Class<T> type) {
        super(type, (o, i, p) -> PropertyBuilder.createDefaultPropertyBuilder().apply(i, o));
    }

}
