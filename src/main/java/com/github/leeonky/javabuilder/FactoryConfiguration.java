package com.github.leeonky.javabuilder;

import com.github.leeonky.util.Converter;

class FactoryConfiguration {
    private final Converter converter = Converter.createDefaultConverter();
    private final PropertyBuilder propertyBuilder = PropertyBuilder.createDefaultPropertyBuilder();
    private final DataRepository dataRepository = new DefaultDataRepository();

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
