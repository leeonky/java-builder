package com.github.leeonky.javabuilder;

import com.github.leeonky.util.BeanClass;

class BeanSpecificationFactory<T> extends AbstractFactory<T> {
    private final BeanSpecification<T> beanSpecification;

    <B extends BeanSpecification<T>> BeanSpecificationFactory(B beanSpecification) {
        super(beanSpecification.getType());
        this.beanSpecification = beanSpecification;
    }

    @Override
    @SuppressWarnings("unchecked")
    public T newInstance(BuildingContext<T> buildingContext) {
        T instance = getBeanClass().newInstance();
        SpecificationBuilder<T> specificationBuilder = new SpecificationBuilder<>((BeanClass<T>) BeanClass.create(instance.getClass()));
        beanSpecification.specifications(specificationBuilder);
        specificationBuilder.collectSpecifications().forEach(spec -> spec.apply(instance));
        return instance;
    }
}
