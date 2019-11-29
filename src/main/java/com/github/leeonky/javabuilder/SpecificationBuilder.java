package com.github.leeonky.javabuilder;

import java.util.ArrayList;
import java.util.List;

public class SpecificationBuilder<T> {
    private final BuildingContext<T> buildingContext;
    private final List<Specification<T>> specifications = new ArrayList<>();

    SpecificationBuilder(BuildingContext<T> buildingContext) {
        this.buildingContext = buildingContext;
    }

    public void propertyValue(String property, Object value) {
        specifications.add(new PropertyValueSpecification(property, value));
    }

    public List<Specification<T>> collectSpecifications() {
        return specifications;
    }

    public <PT> void propertyFactory(String property, Class<? extends BeanSpecification<PT>> specification) {
        specifications.add(new PropertyFactorySpecification<>(property, specification));
    }

    class PropertyValueSpecification implements Specification<T> {
        private final String property;
        private final Object value;

        PropertyValueSpecification(String property, Object value) {
            this.property = property;
            this.value = value;
        }

        @Override
        public void apply(T instance) {
            buildingContext.getBeanClass().setPropertyValue(instance, property, value);
        }
    }

    class PropertyFactorySpecification<PT> implements Specification<T> {
        private final String property;
        private final Class<? extends BeanSpecification<PT>> specification;

        PropertyFactorySpecification(String property, Class<? extends BeanSpecification<PT>> specification) {
            this.property = property;
            this.specification = specification;
        }

        @Override
        public void apply(T instance) {
            buildingContext.getBeanClass().setPropertyValue(instance, property,
                    buildingContext.getFactorySet().toBuild(specification).build());
        }
    }
}
