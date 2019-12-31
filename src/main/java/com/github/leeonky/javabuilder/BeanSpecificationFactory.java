package com.github.leeonky.javabuilder;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.stream.Stream;

class BeanSpecificationFactory<T> extends AbstractFactory<T> {

    <B extends BeanSpecification<T>> BeanSpecificationFactory(B beanSpecification) {
        super(beanSpecification.getType());
        specifications(beanSpecification::specifications);
        Stream.of(beanSpecification.getClass().getMethods())
                .filter(method -> method.getAnnotation(Combination.class) != null)
                .forEach(method -> combinable(getCombinationName(method), specificationBuilder -> {
                    try {
                        method.invoke(beanSpecification, specificationBuilder);
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
        return beanContext.getFactorySet().type(getBeanClass().getType()).build(beanContext);
    }
}
