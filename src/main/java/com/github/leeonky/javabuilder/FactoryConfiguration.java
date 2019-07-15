package com.github.leeonky.javabuilder;

import com.github.leeonky.util.Converter;

public class FactoryConfiguration {
    private final Converter converter;
    private final PropertyBuilder propertyBuilder;
    private final DataRepository dataRepository;

    public FactoryConfiguration(Converter converter, PropertyBuilder propertyBuilder, DataRepository dataRepository) {
        this.converter = converter;
        this.propertyBuilder = propertyBuilder;
        this.dataRepository = dataRepository;
    }

    public FactoryConfiguration() {
        this(Converter.createDefaultConverter(), PropertyBuilder.createDefaultPropertyBuilder(), new DefaultDataRepository());
    }

    public FactoryConfiguration(DataRepository dataRepository) {
        this(Converter.createDefaultConverter(), PropertyBuilder.createDefaultPropertyBuilder(), dataRepository);
    }

    public Converter getConverter() {
        return converter;
    }

    public PropertyBuilder getPropertyBuilder() {
        return propertyBuilder;
    }

    public DataRepository getDataRepository() {
        return dataRepository;
    }
}
