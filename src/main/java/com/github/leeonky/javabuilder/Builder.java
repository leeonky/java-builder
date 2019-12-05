package com.github.leeonky.javabuilder;

import com.github.leeonky.util.BeanClass;
import com.github.leeonky.util.PropertyWriter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

public class Builder<T> {
    private final Factory<T> factory;
    private final FactorySet factorySet;
    private final Map<String, Object> properties = new HashMap<>();
    private final Map<String, Object> params = new HashMap<>();
    private String[] combinations = new String[]{};
    private Consumer<SpecificationBuilder<T>> specifications = null;

    Builder(Factory<T> factory, FactorySet factorySet) {
        this.factory = factory;
        this.factorySet = factorySet;
    }

    private Builder<T> copy() {
        Builder<T> newBuilder = new Builder<>(factory, factorySet);
        newBuilder.properties.putAll(properties);
        return newBuilder;
    }


    public Builder<T> property(String property, Object value) {
        Builder<T> builder = copy();
        builder.properties.put(property, value);
        return builder;
    }

    public T build() {
        Map<String, Object> processedProperties = new HashMap<>();
        BuildingContext<T> buildingContext = new BuildingContext<>(factorySet.getSequence(factory.getBeanClass().getType()),
                params, processedProperties, factory, factorySet);
        properties.forEach((k, v) -> processProperties(factory.getBeanClass(), processedProperties, k, v));
        T object = factory.newInstance(buildingContext);
        processedProperties.forEach((k, v) -> factory.getBeanClass().setPropertyValue(object, k, v));
        factory.postProcess(buildingContext, object);
        factory.combine(buildingContext, combinations);
        if (specifications != null)
            specifications.accept(buildingContext.getSpecificationBuilder());
        buildingContext.getSpecificationBuilder().collectSpecifications().forEach(spec -> spec.apply(object));
        return factorySet.getDataRepository().save(object);
    }

    @SuppressWarnings("unchecked")
    private void processProperties(BeanClass<T> beanClass, Map<String, Object> processed, String name, Object value) {
        if (name.contains(".")) {
            String[] propertyList = name.split("\\.", 2);
            String propertyName = propertyList[0];
            String condition = propertyList[1];
            String factoryName = null;
            if (propertyName.contains("(")) {
                String[] propertyFactory = propertyName.split("\\(");
                propertyName = propertyFactory[0];
                factoryName = propertyFactory[1].split("\\)")[0];
            }
            PropertyWriter<T> propertyWriter = beanClass.getPropertyWriter(propertyName);
            Builder builder = ((factoryName != null ? factorySet.toBuild(factoryName) : factorySet.type(propertyWriter.getPropertyType())))
                    .property(condition, value);
            processed.put(propertyWriter.getName(), builder.query().stream().findFirst().orElseGet(builder::build));
        } else
            processed.put(name, value);
    }

    public Builder<T> properties(Map<String, Object> properties) {
        Builder<T> builder = copy();
        builder.properties.putAll(properties);
        return builder;
    }

    public Builder<T> param(String paramName, Object value) {
        Builder<T> builder = copy();
        builder.params.put(paramName, value);
        return builder;
    }

    public Builder<T> specifications(Consumer<SpecificationBuilder<T>> specifications) {
        Builder<T> builder = copy();
        builder.specifications = specifications;
        return builder;
    }

    public List<T> query() {
        return factorySet.getDataRepository().query(factory.getBeanClass(), properties);
    }

    public Builder<T> combine(String... combinations) {
        Builder<T> builder = copy();
        builder.combinations = Objects.requireNonNull(combinations);
        return builder;
    }
}
