package com.github.leeonky.javabuilder;

import com.github.leeonky.javabuilder.spec.*;
import com.github.leeonky.util.BeanClass;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;

public class BeanContextImpl<T> implements BeanContext<T> {
    private final BuildingContext buildingContext;
    private final BeanContextImpl<?> parent;
    private final String currentPropertyName;
    private final int sequence;
    private final Builder<T> builder;
    private final Set<String> propertySpecProperties = new HashSet<>();
    private final Map<String, Object> specifiedProperties = new LinkedHashMap<>();
    private T built;

    BeanContextImpl(BuildingContext buildingContext, BeanContextImpl<?> parent, String propertyNameInParent, Builder<T> builder) {
        this.builder = builder.copy();
        sequence = builder.factorySet.getSequence(builder.factory.getBeanClass().getType());
        this.buildingContext = buildingContext;
        this.parent = parent;
        currentPropertyName = propertyNameInParent;
    }

    private void queryOrCreateReferencesAndCollectSpecs() {
        builder.properties.forEach((k, v) -> new QueryExpression<>(getBeanClass(), k, v)
                .queryOrCreateTo(builder.factorySet, buildingContext, this, specifiedProperties, propertySpecProperties));
        builder.factory.collectSpecs(this, builder.combinations);
        builder.spec.accept(this);
    }

    public PropertyChain propertyChain(String name) {
        return new PropertyChain(absolutePropertyChain(parent, name));
    }

    @Override
    public int getCurrentSequence() {
        return sequence;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <P> P param(String name) {
        return (P) builder.params.get(name);
    }

    @Override
    public BeanClass<T> getBeanClass() {
        return builder.factory.getBeanClass();
    }

    @Override
    public boolean isPropertyNotSpecified(String name) {
        return !specifiedProperties.containsKey(name) && !propertySpecProperties.contains(name);
    }

    @Override
    public FactorySet getFactorySet() {
        return builder.factorySet;
    }

    private T newWithProperties() {
        built = builder.factory.newInstance(this);
        specifiedProperties.forEach((k, v) -> builder.factory.getBeanClass().setPropertyValue(built, k, v));
        return built;
    }

    private List<String> absolutePropertyChain(BeanContextImpl<?> parent, String property) {
        List<String> chain = parent == null ? new ArrayList<>() : parent.absolutePropertyChain(parent.parent, currentPropertyName);
        chain.addAll(asList(property.split("\\.")));
        return chain;
    }

    @Override
    public PropertySpecBuilder<T> property(String property) {
        return new PropertySpecBuilderImpl(property);
    }

    @Override
    public BeanContext<T> link(String... properties) {
        buildingContext.appendLinkSpec(new LinkSpec(buildingContext, Stream.of(properties)
                .map(this::propertyChain).collect(Collectors.toList())));
        return this;
    }

    public T build() {
        queryOrCreateReferencesAndCollectSpecs();
        T object = newWithProperties();
        buildingContext.applySpecs(object, this);
        return object;
    }

    public <T> void processSubCreate(String property, Builder<T> builder, Consumer<Supplier<T>> consumer) {
        BeanContextImpl<T> subBeanContext = new BeanContextImpl<>(buildingContext, this, property, builder);
        consumer.accept(() -> subBeanContext.buildingContext.cacheSave(subBeanContext.parent.built, subBeanContext.newWithProperties()));
        subBeanContext.queryOrCreateReferencesAndCollectSpecs();
    }

    public class PropertySpecBuilderImpl implements PropertySpecBuilder<T> {
        private final String property;

        PropertySpecBuilderImpl(String property) {
            this.property = property;
        }

        @Override
        public BeanContext<T> value(Object value) {
            return from(() -> value);
        }

        @Override
        public BeanContext<T> from(Supplier<?> supplier) {
            if (isPropertyNotSpecified(property)) {
                PropertyChain propertyChain = propertyChain(property);
                buildingContext.appendSupplierSpec(propertyChain, new SupplierSpec(propertyChain, supplier));
            }
            return BeanContextImpl.this;
        }

        @Override
        public <PT> BeanContext<T> from(Class<? extends BeanSpecs<PT>> beanSpecsClass) {
            return from(beanSpecsClass, builder -> builder);
        }

        @Override
        public <PT> BeanContext<T> from(Class<? extends BeanSpecs<PT>> beanSpecsClass,
                                        Function<Builder<PT>, Builder<PT>> customerBuilder) {
            return from(customerBuilder.apply(getFactorySet().toBuild(beanSpecsClass)));
        }

        <PT> BeanContext<T> from(Builder<PT> builder) {
            if (isPropertyNotSpecified(property))
                processSubCreate(property, builder, this::from);
            return BeanContextImpl.this;
        }

        @Override
        public BeanContext<T> dependsOn(String dependency, Function<Object, Object> function) {
            return dependsOn(singletonList(dependency), list -> function.apply(list.get(0)));
        }

        @Override
        public BeanContext<T> dependsOn(List<String> dependencies, Function<List<Object>, Object> function) {
            if (isPropertyNotSpecified(property.split("\\.")[0])) {
                PropertyChain propertyChain = propertyChain(property);
                buildingContext.appendDependencySpec(propertyChain, new DependencySpec(propertyChain,
                        dependencies.stream().map(BeanContextImpl.this::propertyChain).collect(Collectors.toList()), function));
            }
            return BeanContextImpl.this;
        }

        @Override
        public BeanContext<T> type(Class<?> type) {
            return type(type, builder -> builder);
        }

        @Override
        public <PT> BeanContext<T> type(Class<PT> type, Function<Builder<PT>, Builder<PT>> customerBuilder) {
            return from(customerBuilder.apply(customerBuilder.apply(getFactorySet().type(type))));
        }
    }
}
