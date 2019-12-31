package com.github.leeonky.javabuilder;

import com.github.leeonky.util.BeanClass;
import com.github.leeonky.util.PropertyWriter;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Consumer;

public class BeanContext<T> {
    private final Factory<T> factory;
    private final FactorySet factorySet;
    private final int sequence;
    private final Map<String, Object> params;
    private final Map<String, Object> properties;
    private final BuildingContext buildingContext;
    private final SpecificationBuilder<T> specificationBuilder = new SpecificationBuilder<>(this);

    BeanContext(FactorySet factorySet, Factory<T> factory, int sequence, Map<String, Object> params, Map<String, Object> properties, BuildingContext buildingContext) {
        this.sequence = sequence;
        this.params = new LinkedHashMap<>(params);
        this.factory = factory;
        this.factorySet = factorySet;
        this.buildingContext = buildingContext;
        this.properties = processReferenceProperties(properties);
    }


    @SuppressWarnings("unchecked")
    private Map<String, Object> processReferenceProperties(Map<String, Object> properties) {
        Map<String, Object> processedProperties = new LinkedHashMap<>();
        properties.forEach((k, v) -> {
            if (k.contains(".")) {
                PropertyQueryChain propertyQueryChain = PropertyQueryChain.parse(k);
                PropertyWriter<T> propertyWriter = factory.getBeanClass().getPropertyWriter(propertyQueryChain.getBaseName());
                Builder builder = propertyQueryChain.toBuilder(factorySet, propertyWriter.getPropertyType(), v);
                processedProperties.put(propertyWriter.getName(), builder.query().stream().findFirst().orElseGet(() -> builder.create(this)));
            } else
                processedProperties.put(k, v);
        });
        return processedProperties;
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

    public boolean isNotSpecified(String name) {
        return !properties.containsKey(name);
    }

    public void assignPropertiesAsDefaultValues(T object) {
        factorySet.getPropertyBuilder().assignPropertiesAsDefaultValues(object, this);
    }

    public FactorySet getFactorySet() {
        return factorySet;
    }

    public SpecificationBuilder<T> getSpecificationBuilder() {
        return specificationBuilder;
    }

    public void assignProperties(T instance) {
        properties.forEach((k, v) -> factory.getBeanClass().setPropertyValue(instance, k, v));
    }

    public void collectSpecifications(Consumer<SpecificationBuilder<T>> specifications) {
        specifications.accept(specificationBuilder);
    }

    public <T> BeanContext<T> createSubContext(Factory<T> factory, int sequence, Map<String, Object> params, Map<String, Object> properties) {
        return new BeanContext<>(factorySet, factory, sequence, params, properties, buildingContext);
    }

    public void collectForLastSave(T object) {
        buildingContext.getUnSavedObjects().add(object);
    }
}
