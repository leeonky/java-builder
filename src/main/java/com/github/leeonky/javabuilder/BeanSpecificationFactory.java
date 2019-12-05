package com.github.leeonky.javabuilder;

class BeanSpecificationFactory<T> extends AbstractFactory<T> {

    <B extends BeanSpecification<T>> BeanSpecificationFactory(B beanSpecification) {
        super(beanSpecification.getType());
        specifications(beanSpecification::specifications);
    }

    @Override
    public T newInstance(BuildingContext<T> buildingContext) {
        return buildingContext.getFactorySet().type(getBeanClass().getType()).build();
    }
}
