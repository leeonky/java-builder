package com.github.leeonky.javabuilder;

class BeanSpecificationFactory<T> extends AbstractFactory<T> {
    private final BeanSpecification<T> beanSpecification;

    <B extends BeanSpecification<T>> BeanSpecificationFactory(B beanSpecification) {
        super(beanSpecification.getType());
        this.beanSpecification = beanSpecification;
    }

    @Override
    public T newInstance(BuildingContext<T> buildingContext) {
        return buildingContext.getFactorySet().type(getBeanClass().getType()).build();
    }

    @Override
    public T postProcess(BuildingContext<T> buildingContext, T object) {
        SpecificationBuilder<T> specificationBuilder = new SpecificationBuilder<>(buildingContext);
        beanSpecification.specifications(specificationBuilder);
        specificationBuilder.collectSpecifications().forEach(spec -> spec.apply(object));
        return object;
    }
}
