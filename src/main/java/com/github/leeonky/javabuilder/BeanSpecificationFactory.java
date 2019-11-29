package com.github.leeonky.javabuilder;

class BeanSpecificationFactory<T> extends AbstractFactory<T> {
    private final BeanSpecification<T> beanSpecification;

    <B extends BeanSpecification<T>> BeanSpecificationFactory(B beanSpecification) {
        super(beanSpecification.getType());
        this.beanSpecification = beanSpecification;
    }

    @Override
    public T newInstance(BuildingContext<T> buildingContext) {
        Class<T> type = getBeanClass().getType();
        T instance = buildingContext.getFactorySet().type(type).build();
        SpecificationBuilder<T> specificationBuilder = new SpecificationBuilder<>(buildingContext);
        beanSpecification.specifications(specificationBuilder);
        specificationBuilder.collectSpecifications().forEach(spec -> spec.apply(instance));
        return instance;
    }
}
