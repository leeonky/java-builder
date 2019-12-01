package com.github.leeonky.javabuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class SpecificationBuilder<T> {
    private final BuildingContext<T> buildingContext;
    private final List<Specification<T>> specifications = new ArrayList<>();

    SpecificationBuilder(BuildingContext<T> buildingContext) {
        this.buildingContext = buildingContext;
    }

    public SpecificationBuilder<T> propertyValue(String property, Object value) {
        specifications.add(new PropertyValueSpecification(property, value));
        return this;
    }

    public List<Specification<T>> collectSpecifications() {
        return specifications.stream()
                .filter(specification -> buildingContext.notSpecified(specification.getProperty()))
                .collect(Collectors.toList());
    }

    public <PT> SpecificationBuilder<T> propertyFactory(String property, Class<? extends BeanSpecification<PT>> specification) {
        specifications.add(new PropertyFactorySpecification<>(property, specification));
        return this;
    }

    abstract class AbstractSpecification<T> implements Specification<T> {
        private final String property;

        protected AbstractSpecification(String property) {
            this.property = property;
        }

        @Override
        public String getProperty() {
            return property;
        }
    }

    class PropertyValueSpecification extends AbstractSpecification<T> {
        private final Object value;

        PropertyValueSpecification(String property, Object value) {
            super(property);
            this.value = value;
        }

        @Override
        public void apply(T instance) {
            buildingContext.getBeanClass().setPropertyValue(instance, getProperty(), value);
        }
    }

    class PropertyFactorySpecification<PT> extends AbstractSpecification<T> {
        private final Class<? extends BeanSpecification<PT>> specification;

        PropertyFactorySpecification(String property, Class<? extends BeanSpecification<PT>> specification) {
            super(property);
            this.specification = specification;
        }

        @Override
        public void apply(T instance) {
            buildingContext.getBeanClass().setPropertyValue(instance, getProperty(),
                    buildingContext.getFactorySet().toBuild(specification).build());
        }
    }
}
