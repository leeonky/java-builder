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
    private final BeanContext<T> beanContext;
    private final Map<String, DependencyProperty<T>> dependencyPropertyMap = new LinkedHashMap<>();

    SpecificationBuilder(BeanContext<T> beanContext) {
        this.beanContext = beanContext;
    }

    public PropertySpecificationBuilder property(String property) {
        return new PropertySpecificationBuilder(property);
    }

    public void applySpecifications(T instance) {
        LinkedHashSet<String> properties = new LinkedHashSet<>(dependencyPropertyMap.keySet())
                .stream().filter(beanContext::isPropertyNotSpecified)
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
            return buildFrom(() -> value);
        }

        public <E> PropertySpecificationBuilder buildFrom(Supplier<E> supplier) {
            if (beanContext.isPropertyNotSpecified(property)) {
                beanContext.appendValueSpecification(property, supplier);
            }
            return this;
        }

        public <PT> PropertySpecificationBuilder buildFrom(Class<? extends BeanSpecification<PT>> specification) {
            return buildFrom(specification, builder -> builder);
        }

        public <PT> PropertySpecificationBuilder buildFrom(Class<? extends BeanSpecification<PT>> specification,
                                                           Function<Builder<PT>, Builder<PT>> customerBuilder) {
            if (beanContext.isPropertyNotSpecified(property)) {
                Builder<PT> builder = customerBuilder.apply(beanContext.getFactorySet().toBuild(specification));
                BeanContext<PT> subBeanContext = builder.createSubBeanContext(beanContext, property);
                buildFrom(() -> builder.subCreate(subBeanContext));
                subBeanContext.collectAllSpecifications();
            }
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
                    beanContext.getBeanClass().setPropertyValue(instance, property,
                            dependencyHandler.apply(dependencies.stream()
                                    .map(dependency -> beanContext.getBeanClass().getPropertyValue(instance, dependency))
                                    .collect(Collectors.toList())));
                }
            });
            return this;
        }
    }
}
