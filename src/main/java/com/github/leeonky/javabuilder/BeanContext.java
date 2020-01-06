package com.github.leeonky.javabuilder;

import com.github.leeonky.util.BeanClass;
import com.github.leeonky.util.PropertyWriter;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static java.util.Collections.singletonList;

public class BeanContext<T> {
    private final Factory<T> factory;
    private final FactorySet factorySet;
    private final int sequence;
    private final Map<String, Object> params;
    private final Map<String, Object> properties = new LinkedHashMap<>(), originalProperties;
    private final Consumer<BeanContext<T>> spec;
    private final String[] combinations;
    private final BuildingContext buildingContext;
    private final BeanContext<?> parent;
    private final String currentPropertyName;

    BeanContext(FactorySet factorySet, Factory<T> factory, BeanContext<?> parent, String propertyNameInParent,
                int sequence, Map<String, Object> params, Map<String, Object> properties, BuildingContext buildingContext,
                Consumer<BeanContext<T>> spec, String[] combinations) {
        this.sequence = sequence;
        this.params = new LinkedHashMap<>(params);
        this.factory = factory;
        this.factorySet = factorySet;
        this.buildingContext = buildingContext;
        this.parent = parent;
        currentPropertyName = propertyNameInParent;
        this.spec = spec;
        this.combinations = combinations;
        originalProperties = new LinkedHashMap<>(properties);
    }

    void queryOrCreateReferenceBeansAndCollectAllSpecs() {
        originalProperties.forEach((k, v) -> {
            if (k.contains(".")) {
                PropertyQueryChain propertyQueryChain = PropertyQueryChain.parse(k);
                PropertyWriter<T> propertyWriter = factory.getBeanClass().getPropertyWriter(propertyQueryChain.getBaseName());
                Builder<?> builder = propertyQueryChain.toBuilder(factorySet, propertyWriter.getPropertyType(), v);
                Optional<?> queried = builder.query().stream().findFirst();
                queried.ifPresent(o -> properties.put(propertyWriter.getName(), o));
                if (!queried.isPresent())
                    property(propertyWriter.getName()).from(builder);
            } else
                properties.put(k, v);
        });
        factory.collectSpecs(this, combinations);
        collectSpecs(spec);
    }

    public int getCurrentSequence() {
        return sequence;
    }

    @SuppressWarnings("unchecked")
    public <P> P param(String name) {
        return (P) params.get(name);
    }

    public BeanClass<T> getBeanClass() {
        return factory.getBeanClass();
    }

    public boolean isPropertyNotSpecified(String name) {
        return !properties.containsKey(name);
    }

    void assignDefaultValueToUnSpecifiedProperties(T object) {
        factorySet.getPropertyBuilder().assignDefaultValueToProperties(object, this);
    }

    public FactorySet getFactorySet() {
        return factorySet;
    }

    T assignProperties(T instance) {
        properties.forEach((k, v) -> factory.getBeanClass().setPropertyValue(instance, k, v));
        return instance;
    }

    void collectSpecs(Consumer<BeanContext<T>> spec) {
        spec.accept(this);
    }

    <T> BeanContext<T> createSubContext(Factory<T> factory, String propertyName, int sequence, Map<String, Object> params,
                                        Map<String, Object> properties, Consumer<BeanContext<T>> spec, String[] combinations) {
        return new BeanContext<>(factorySet, factory, this, propertyName, sequence, params, properties, buildingContext, spec, combinations);
    }

    private List<String> absolutePropertyChain(String property) {
        return absolutePropertyChain(parent, property);
    }

    private List<String> absolutePropertyChain(BeanContext<?> parent, String property) {
        List<String> chain = parent == null ? new ArrayList<>() : absolutePropertyChain(parent.parent, currentPropertyName);
        chain.add(property);
        return chain;
    }

    BuildingContext getBuildingContext() {
        return buildingContext;
    }

    public PropertySpecBuilder property(String property) {
        return new PropertySpecBuilder(property);
    }

    public class PropertySpecBuilder {
        private final String property;

        PropertySpecBuilder(String property) {
            this.property = property;
        }

        public BeanContext<T> eq(Object value) {
            return from(() -> value);
        }

        public BeanContext<T> from(Supplier<?> supplier) {
            if (isPropertyNotSpecified(property)) {
                PropertyChain propertyChain = new PropertyChain(absolutePropertyChain(property));
                buildingContext.appendSupplierSpec(propertyChain, new SupplierSpec(propertyChain, supplier));
            }
            return BeanContext.this;
        }

        public <PT> BeanContext<T> from(Class<? extends BeanSpecs<PT>> beanSpecsClass) {
            return from(beanSpecsClass, builder -> builder);
        }

        public <PT> BeanContext<T> from(Class<? extends BeanSpecs<PT>> beanSpecsClass,
                                        Function<Builder<PT>, Builder<PT>> customerBuilder) {
            return from(customerBuilder.apply(getFactorySet().toBuild(beanSpecsClass)));
        }

        <PT> BeanContext<T> from(Builder<PT> builder) {
            if (isPropertyNotSpecified(property)) {
                BeanContext<PT> subBeanContext = builder.createSubBeanContext(BeanContext.this, property);
                from(() -> builder.subCreate(subBeanContext));
                subBeanContext.queryOrCreateReferenceBeansAndCollectAllSpecs();
            }
            return BeanContext.this;
        }

        public BeanContext<T> dependsOn(String dependency, Function<Object, Object> function) {
            return dependsOn(singletonList(dependency), list -> function.apply(list.get(0)));
        }

        public BeanContext<T> dependsOn(List<String> dependencies, Function<List<Object>, Object> function) {
            if (isPropertyNotSpecified(property)) {
                PropertyChain propertyChain = new PropertyChain(absolutePropertyChain(property));
                buildingContext.appendDependencySpec(propertyChain, new DependencySpec(propertyChain,
                        dependencies.stream().map(d -> new PropertyChain(absolutePropertyChain(d))).collect(Collectors.toList()), function));
            }
            return BeanContext.this;
        }

        public BeanContext<T> type(Class<?> type) {
            return type(type, builder -> builder);
        }

        public <PT> BeanContext<T> type(Class<PT> type, Function<Builder<PT>, Builder<PT>> customerBuilder) {
            return from(customerBuilder.apply(customerBuilder.apply(getFactorySet().type(type))));
        }
    }
}