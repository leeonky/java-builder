package com.github.leeonky.javabuilder;

import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

import static java.util.Collections.singletonList;

public class SpecBuilder<T> {
    private final BeanContext<T> beanContext;

    SpecBuilder(BeanContext<T> beanContext) {
        this.beanContext = beanContext;
    }

    public PropertySpecBuilder property(String property) {
        return new PropertySpecBuilder(property);
    }

    public class PropertySpecBuilder {
        private final String property;

        PropertySpecBuilder(String property) {
            this.property = property;
        }

        public SpecBuilder<T> eq(Object value) {
            return from(() -> value);
        }

        public <E> SpecBuilder<T> from(Supplier<E> supplier) {
            beanContext.appendValueSpec(property, supplier);
            return SpecBuilder.this;
        }

        public <PT> SpecBuilder<T> from(Class<? extends BeanSpecs<PT>> specification) {
            return from(specification, builder -> builder);
        }

        public <PT> SpecBuilder<T> from(Class<? extends BeanSpecs<PT>> specification,
                                        Function<Builder<PT>, Builder<PT>> customerBuilder) {
            return from(customerBuilder.apply(beanContext.getFactorySet().toBuild(specification)));
        }

        <PT> SpecBuilder<T> from(Builder<PT> builder) {
            if (beanContext.isPropertyNotSpecified(property)) {
                BeanContext<PT> subBeanContext = builder.createSubBeanContext(beanContext, property);
                from(() -> builder.subCreate(subBeanContext));
                subBeanContext.queryOrCreateReferenceBeansAndCollectAllSpecs();
            }
            return SpecBuilder.this;
        }

        public SpecBuilder<T> dependsOn(String dependency, Function<Object, Object> dependencyHandler) {
            return dependsOn(singletonList(dependency), list -> dependencyHandler.apply(list.get(0)));
        }

        public SpecBuilder<T> dependsOn(List<String> dependencies, Function<List<Object>, Object> dependencyHandler) {
            beanContext.appendDependencySpec(property, dependencies, dependencyHandler);
            return SpecBuilder.this;
        }

        public SpecBuilder<T> type(Class<?> type) {
            return type(type, builder -> builder);
        }

        public <PT> SpecBuilder<T> type(Class<PT> type, Function<Builder<PT>, Builder<PT>> customerBuilder) {
            return from(customerBuilder.apply(customerBuilder.apply(beanContext.getFactorySet().type(type))));
        }
    }
}
