package com.github.leeonky.javabuilder;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static java.util.Collections.singletonList;

public class SpecificationBuilder<T> {
    private final BuildingContext<T> buildingContext;
    private final Map<String, Specification<T>> specificationMap = new LinkedHashMap<>();
    private final Map<String, DependencyProperty<T>> dependencyPropertyMap = new LinkedHashMap<>();

    SpecificationBuilder(BuildingContext<T> buildingContext) {
        this.buildingContext = buildingContext;
    }

    public PropertySpecificationBuilder property(String property) {
        return new PropertySpecificationBuilder(property);
    }

    public void applySpecifications(T instance) {
        specificationMap.entrySet().stream()
                .filter(s -> buildingContext.isNotSpecified(s.getKey()))
                .forEach(s -> s.getValue().apply(instance));
        LinkedHashSet<String> properties = new LinkedHashSet<>(dependencyPropertyMap.keySet())
                .stream().filter(buildingContext::isNotSpecified)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        while (properties.size() > 0)
            assignFromDependency(instance, properties, properties.iterator().next());
    }

    private void assignFromDependency(T instance, LinkedHashSet<String> properties, String property) {
        if (properties.contains(property)) {
            DependencyProperty<T> dependencyProperty = dependencyPropertyMap.get(property);
            dependencyProperty.getDependencyName()
                    .forEach(dependencyDependency -> assignFromDependency(instance, properties, dependencyDependency));
            dependencyProperty.apply(instance);
            properties.remove(property);
        }
    }

    public class PropertySpecificationBuilder {
        private final String property;

        PropertySpecificationBuilder(String property) {
            this.property = property;
        }

        public PropertySpecificationBuilder hasValue(Object value) {
            specificationMap.put(property, instance -> buildingContext.getBeanClass().setPropertyValue(instance, property, value));
            return this;
        }

        public <E> PropertySpecificationBuilder buildFrom(Supplier<E> supplier) {
            specificationMap.put(property, instance -> buildingContext.getBeanClass().setPropertyValue(instance, property, supplier.get()));
            return this;
        }

        public <PT> PropertySpecificationBuilder buildFrom(Class<? extends BeanSpecification<PT>> specification) {
            specificationMap.put(property, instance ->
                    buildingContext.getBeanClass().setPropertyValue(instance, property, buildingContext.getFactorySet().toBuild(specification).build()));
            return this;
        }

        public <PT> PropertySpecificationBuilder buildFrom(Class<? extends BeanSpecification<PT>> specification,
                                                           Function<Builder<PT>, Builder<PT>> builder) {
            specificationMap.put(property, instance ->
                    buildingContext.getBeanClass().setPropertyValue(instance, property, builder.apply(buildingContext.getFactorySet()
                            .toBuild(specification)).build()));
            return this;
        }

        public PropertySpecificationBuilder dependsOn(String dependency, Function<Object, Object> dependencyHandler) {
            return dependsOn(singletonList(dependency), list -> dependencyHandler.apply(list.get(0)));
        }

        public PropertySpecificationBuilder dependsOn(List<String> dependencies, Function<List<Object>, Object> dependencyHandler) {
            dependencyPropertyMap.put(property, new DependencyProperty<T>() {
                @Override
                public List<String> getDependencyName() {
                    return dependencies;
                }

                @Override
                public void apply(T instance) {
                    buildingContext.getBeanClass().setPropertyValue(instance, property,
                            dependencyHandler.apply(dependencies.stream()
                                    .map(dependency -> buildingContext.getBeanClass().getPropertyValue(instance, dependency))
                                    .collect(Collectors.toList())));
                }
            });
            return this;
        }
    }
}
