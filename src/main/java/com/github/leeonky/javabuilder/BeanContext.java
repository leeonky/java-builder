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

public class BeanContext<T> {
    private final BuildingContext buildingContext;
    private final BeanContext<?> parent;
    private final String currentPropertyName;
    private final int sequence;
    private final Builder<T> builder;
    private final Set<String> propertySpecProperties = new HashSet<>();
    private final Map<String, Object> specifiedProperties = new LinkedHashMap<>();
    private T built;

    BeanContext(BuildingContext buildingContext, BeanContext<?> parent, String propertyNameInParent, Builder<T> builder) {
        this.builder = builder.copy();
        sequence = builder.factorySet.getSequence(builder.factory.getBeanClass().getType());
        this.buildingContext = buildingContext;
        this.parent = parent;
        currentPropertyName = propertyNameInParent;
    }

    private void queryOrCreateReferencesAndCollectSpecs() {
        builder.properties.forEach((k, v) -> {
            QueryExpression<T> queryExpression = new QueryExpression<>(getBeanClass(), k, v);
            List<?> query = queryExpression.query(builder.factorySet);
            if (query.isEmpty()) {
                createSub(queryExpression.getBaseName(), queryExpression.forCreating(builder.factorySet), creator ->
                        buildingContext.appendPropertiesSpec(new PropertySpec(propertyChain(queryExpression.getBaseName()), creator, queryExpression)));
                propertySpecProperties.add(queryExpression.getBaseName());
            } else
                specifiedProperties.put(queryExpression.getBaseName(), query.get(0));
        });
        builder.factory.collectSpecs(this, builder.combinations);
        builder.spec.accept(this);
    }

    private PropertyChain propertyChain(String name) {
        return new PropertyChain(absolutePropertyChain(parent, name));
    }

    public int getCurrentSequence() {
        return sequence;
    }

    @SuppressWarnings("unchecked")
    public <P> P param(String name) {
        return (P) builder.params.get(name);
    }

    public BeanClass<T> getBeanClass() {
        return builder.factory.getBeanClass();
    }

    public boolean isPropertyNotSpecified(String name) {
        return !specifiedProperties.containsKey(name) && !propertySpecProperties.contains(name);
    }

    public FactorySet getFactorySet() {
        return builder.factorySet;
    }

    private T newWithProperties() {
        built = builder.factory.newInstance(this);
        specifiedProperties.forEach((k, v) -> builder.factory.getBeanClass().setPropertyValue(built, k, v));
        return built;
    }

    private List<String> absolutePropertyChain(BeanContext<?> parent, String property) {
        List<String> chain = parent == null ? new ArrayList<>() : parent.absolutePropertyChain(parent.parent, currentPropertyName);
        chain.addAll(asList(property.split("\\.")));
        return chain;
    }

    public PropertySpecBuilder property(String property) {
        return new PropertySpecBuilder(property);
    }

    public BeanContext<T> link(String... properties) {
        buildingContext.appendLinkSpec(new LinkSpec(buildingContext, Stream.of(properties)
                .map(this::propertyChain).collect(Collectors.toList())));
        return this;
    }

    T build() {
        queryOrCreateReferencesAndCollectSpecs();
        T object = newWithProperties();
        buildingContext.applySpecs(object, this);
        return object;
    }

    private T subCreate() {
        return buildingContext.cacheSave(parent.built, newWithProperties());
    }

    private <T> void createSub(String property, Builder<T> builder, Consumer<Supplier<T>> consumer) {
        BeanContext<T> subBeanContext = new BeanContext<>(buildingContext, this, property, builder);
        consumer.accept(subBeanContext::subCreate);
        subBeanContext.queryOrCreateReferencesAndCollectSpecs();
    }

    public class PropertySpecBuilder {
        private final String property;

        PropertySpecBuilder(String property) {
            this.property = property;
        }

        public BeanContext<T> value(Object value) {
            return from(() -> value);
        }

        public BeanContext<T> from(Supplier<?> supplier) {
            if (isPropertyNotSpecified(property)) {
                PropertyChain propertyChain = propertyChain(property);
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
            if (isPropertyNotSpecified(property))
                createSub(property, builder, this::from);
            return BeanContext.this;
        }

        public BeanContext<T> dependsOn(String dependency, Function<Object, Object> function) {
            return dependsOn(singletonList(dependency), list -> function.apply(list.get(0)));
        }

        public BeanContext<T> dependsOn(List<String> dependencies, Function<List<Object>, Object> function) {
            if (isPropertyNotSpecified(property.split("\\.")[0])) {
                PropertyChain propertyChain = propertyChain(property);
                buildingContext.appendDependencySpec(propertyChain, new DependencySpec(propertyChain,
                        dependencies.stream().map(BeanContext.this::propertyChain).collect(Collectors.toList()), function));
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
