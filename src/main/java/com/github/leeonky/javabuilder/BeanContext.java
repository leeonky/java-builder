package com.github.leeonky.javabuilder;

import com.github.leeonky.javabuilder.spec.*;
import com.github.leeonky.util.BeanClass;
import com.github.leeonky.util.PropertyWriter;

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
    private final Set<String> specProperties = new HashSet<>();
    private final Map<String, Object> specifiedProperties = new LinkedHashMap<>();
    private T built;

    BeanContext(BuildingContext buildingContext, BeanContext<?> parent, String propertyNameInParent, Builder<T> builder) {
        this.builder = builder.copy();
        sequence = builder.factorySet.getSequence(builder.factory.getBeanClass().getType());
        this.buildingContext = buildingContext;
        this.parent = parent;
        currentPropertyName = propertyNameInParent;
    }

    private void queryOrCreateReferenceBeansAndCollectAllSpecs() {
        builder.properties.forEach((k, v) -> {
            if (k.contains(".")) {
                PropertyQueryChain propertyQueryChain = PropertyQueryChain.parse(k);
                PropertyWriter<T> propertyWriter = getBeanClass().getPropertyWriter(propertyQueryChain.getBaseName());
                Builder<?> builder = propertyQueryChain.toBuilder(this.builder.factorySet, propertyWriter.getPropertyType(), v);
                List<?> query = builder.query();
                if (query.isEmpty()) {
                    createSub(propertyWriter.getName(), builder, creator -> {
                        PropertyChain propertyChain = new PropertyChain(absolutePropertyChain(propertyWriter.getName()));
                        buildingContext.appendPropertiesSpec(propertyChain, new PropertySpec(propertyWriter.getPropertyType(), propertyChain,
                                creator, k, v));
                    });
                    specProperties.add(propertyWriter.getName());
                } else
                    specifiedProperties.put(propertyWriter.getName(), query.get(0));
            } else
                specifiedProperties.put(k, v);
        });
        builder.factory.collectSpecs(this, builder.combinations);
        builder.spec.accept(this);
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
        return !specifiedProperties.containsKey(name) && !specProperties.contains(name);
    }

    public FactorySet getFactorySet() {
        return builder.factorySet;
    }

    private T newWithProperties() {
        built = builder.factory.newInstance(this);
        specifiedProperties.forEach((k, v) -> builder.factory.getBeanClass().setPropertyValue(built, k, v));
        return built;
    }

    private List<String> absolutePropertyChain(String property) {
        return absolutePropertyChain(parent, property);
    }

    private List<String> absolutePropertyChain(BeanContext<?> parent, String property) {
        List<String> chain = parent == null ? new ArrayList<>() : parent.absolutePropertyChain(parent.parent, currentPropertyName);
        chain.addAll(asList(property.split("\\.")));
        return chain;
    }

    public BuildingContext getBuildingContext() {
        return buildingContext;
    }

    public PropertySpecBuilder property(String property) {
        return new PropertySpecBuilder(property);
    }

    public BeanContext<T> link(String... properties) {
        buildingContext.appendLinkSpec(new LinkSpec(Stream.of(properties)
                .map(p -> new PropertyChain(absolutePropertyChain(p))).collect(Collectors.toList())));
        return this;
    }

    T build() {
        queryOrCreateReferenceBeansAndCollectAllSpecs();
        T object = newWithProperties();
        buildingContext.applySpecs(object, this);
        return object;
    }

    private T subCreate() {
        T object = newWithProperties();
        buildingContext.cacheSave(parent.built, object);
        return object;
    }

    private <T> void createSub(String property, Builder<T> builder, Consumer<Supplier<T>> consumer) {
        BeanContext<T> subBeanContext = new BeanContext<>(buildingContext, this, property, builder);
        consumer.accept(subBeanContext::subCreate);
        subBeanContext.queryOrCreateReferenceBeansAndCollectAllSpecs();
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
            if (isPropertyNotSpecified(property))
                createSub(property, builder, this::from);
            return BeanContext.this;
        }

        public BeanContext<T> dependsOn(String dependency, Function<Object, Object> function) {
            return dependsOn(singletonList(dependency), list -> function.apply(list.get(0)));
        }

        public BeanContext<T> dependsOn(List<String> dependencies, Function<List<Object>, Object> function) {
            if (isPropertyNotSpecified(property.split("\\.")[0])) {
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
