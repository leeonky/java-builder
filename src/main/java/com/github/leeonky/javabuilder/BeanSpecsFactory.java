package com.github.leeonky.javabuilder;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.stream.Stream;

class BeanSpecsFactory<T> extends AbstractFactory<T> {

    private final BeanSpecs<T> beanSpecs;

    <B extends BeanSpecs<T>> BeanSpecsFactory(B beanSpecs) {
        super(beanSpecs.getType());
        this.beanSpecs = beanSpecs;
        Stream.of(beanSpecs.getClass().getMethods())
                .filter(method -> method.getAnnotation(Combination.class) != null)
                .forEach(method -> combinable(getCombinationName(method), beanContext -> {
                    try {
                        method.invoke(beanSpecs, beanContext);
                    } catch (IllegalAccessException | InvocationTargetException e) {
                        throw new IllegalStateException(e);
                    }
                }));
    }

    private String getCombinationName(Method method) {
        Combination annotation = method.getAnnotation(Combination.class);
        return annotation.value().isEmpty() ? method.getName() : annotation.value();
    }

    @Override
    public T newInstance(BeanContext<T> beanContext) {
        return beanContext.getFactorySet().type(getBeanClass().getType()).build(beanContext.getBuildingContext());
    }

    @Override
    public void collectSpecs(BeanContext<T> beanContext, String... combinations) {
        beanSpecs.specs(beanContext);
        super.collectSpecs(beanContext, combinations);
    }
}
