package com.github.leeonky.javabuilder;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class SpecificationBuilder<T> {
    private final BuildingContext<T> buildingContext;
    private final Map<String, Specification<T>> specificationMap = new LinkedHashMap<>();

    SpecificationBuilder(BuildingContext<T> buildingContext) {
        this.buildingContext = buildingContext;
    }

    public List<Specification<T>> collectSpecifications() {
        return specificationMap.values().stream()
                .filter(specification -> buildingContext.isNotSpecified(specification.getProperty()))
                .collect(Collectors.toList());
    }

    public SpecificationBuilder<T> propertyValue(String property, Object value) {
        specificationMap.put(property, new PropertyValueSpecification(property, value));
        return this;
    }

    public <PT> SpecificationBuilder<T> propertyFactory(String property, Class<? extends BeanSpecification<PT>> specification) {
        specificationMap.put(property, new PropertyFactorySpecification<>(property, specification, null));
        return this;
    }

    public <PT> SpecificationBuilder<T> propertyFactory(String property, Class<? extends BeanSpecification<PT>> specification,
                                                        Function<Builder<PT>, Builder<PT>> builder) {
        specificationMap.put(property, new PropertyFactorySpecification<>(property, specification, builder));
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
        private final Function<Builder<PT>, Builder<PT>> builder;

        PropertyFactorySpecification(String property, Class<? extends BeanSpecification<PT>> specification, Function<Builder<PT>, Builder<PT>> builder) {
            super(property);
            this.specification = specification;
            this.builder = builder;
        }

        @Override
        public void apply(T instance) {
            Builder<PT> builder = buildingContext.getFactorySet().toBuild(specification);
            if (this.builder != null)
                builder = this.builder.apply(builder);
            buildingContext.getBeanClass().setPropertyValue(instance, getProperty(), builder.build());
        }
    }
}
