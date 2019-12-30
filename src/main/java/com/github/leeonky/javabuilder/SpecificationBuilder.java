package com.github.leeonky.javabuilder;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class SpecificationBuilder<T> {
    private final BuildingContext<T> buildingContext;
    private final Map<String, Specification<T>> specificationMap = new LinkedHashMap<>();
    private final Map<String, DependencyProperty<T>> dependencyPropertyMap = new LinkedHashMap<>();

    SpecificationBuilder(BuildingContext<T> buildingContext) {
        this.buildingContext = buildingContext;
    }

    public SpecificationBuilder<T> propertyValue(String property, Object value) {
        specificationMap.put(property, new PropertyValueSpecification(property, value));
        return this;
    }

    public <PT> SpecificationBuilder<T> propertyFactory(String property, Class<? extends BeanSpecification<PT>> specification) {
        specificationMap.put(property, new PropertyFactorySpecification<>(property, specification, b -> b));
        return this;
    }

    public <PT> SpecificationBuilder<T> propertyFactory(String property, Class<? extends BeanSpecification<PT>> specification,
                                                        Function<Builder<PT>, Builder<PT>> builder) {
        specificationMap.put(property, new PropertyFactorySpecification<>(property, specification, builder));
        return this;
    }

    public <E> SpecificationBuilder<T> propertySupplier(String property, Supplier<E> supplier) {
        specificationMap.put(property, new PropertySupplierSpecification<>(property, supplier));
        return this;
    }

    public SpecificationBuilder<T> propertyDependsOn(String property, String dependency, Function<Object, Object> dependencyHandler) {
        dependencyPropertyMap.put(property, new DependencyProperty<T>() {
            @Override
            public String getDependencyName() {
                return dependency;
            }

            @Override
            public void apply(T instance) {
                buildingContext.getBeanClass().setPropertyValue(instance, property,
                        dependencyHandler.apply(buildingContext.getBeanClass().getPropertyValue(instance, dependency)));
            }
        });
        return this;
    }

    public void applySpecifications(T instance) {
        specificationMap.values().stream()
                .filter(specification -> buildingContext.isNotSpecified(specification.getProperty()))
                .forEach(spec -> spec.apply(instance));
        LinkedHashSet<String> properties = new LinkedHashSet<>(dependencyPropertyMap.keySet())
                .stream().filter(buildingContext::isNotSpecified)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        while (properties.size() > 0) {
            processDependency(instance, properties, properties.iterator().next());
        }
    }

    private void processDependency(T instance, LinkedHashSet<String> properties, String property) {
        if (properties.contains(property)) {
            DependencyProperty<T> dependencyProperty = dependencyPropertyMap.get(property);
            String dependencyDependency = dependencyProperty.getDependencyName();
            processDependency(instance, properties, dependencyDependency);
            dependencyProperty.apply(instance);
            properties.remove(property);
        }
    }

    abstract static class AbstractSpecification<T> implements Specification<T> {
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
        private final Function<Builder<PT>, Builder<PT>> additionalBuilderConfig;

        PropertyFactorySpecification(String property, Class<? extends BeanSpecification<PT>> specification, Function<Builder<PT>, Builder<PT>> additionalBuilderConfig) {
            super(property);
            this.specification = specification;
            this.additionalBuilderConfig = Objects.requireNonNull(additionalBuilderConfig);
        }

        @Override
        public void apply(T instance) {
            Builder<PT> builder = buildingContext.getFactorySet().toBuild(specification);
            buildingContext.getBeanClass().setPropertyValue(instance, getProperty(), additionalBuilderConfig.apply(builder).build());
        }
    }

    class PropertySupplierSpecification<E> extends AbstractSpecification<T> {
        private final Supplier<E> supplier;

        PropertySupplierSpecification(String property, Supplier<E> supplier) {
            super(property);
            this.supplier = supplier;
        }

        @Override
        public void apply(T instance) {
            buildingContext.getBeanClass().setPropertyValue(instance, getProperty(), supplier.get());
        }
    }
}
