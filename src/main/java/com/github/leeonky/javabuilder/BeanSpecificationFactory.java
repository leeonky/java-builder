package com.github.leeonky.javabuilder;

import com.github.leeonky.util.BeanClass;

class BeanSpecificationFactory<T> implements Factory<T> {
    private final BeanSpecification<T> beanSpecification;

    <B extends BeanSpecification<T>> BeanSpecificationFactory(B beanSpecification) {
        this.beanSpecification = beanSpecification;
    }

    @Override
    @SuppressWarnings("unchecked")
    public T newInstance() {
        T instance = BeanClass.newInstance(beanSpecification.getType());
        SpecificationBuilder<T> specificationBuilder = new SpecificationBuilder<>((BeanClass<T>) BeanClass.create(instance.getClass()));
        beanSpecification.specifications(specificationBuilder);
        specificationBuilder.collectSpecifications().forEach(spec -> spec.apply(instance));
        return instance;
    }
}
