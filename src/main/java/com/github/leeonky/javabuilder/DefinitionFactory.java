package com.github.leeonky.javabuilder;

public class DefinitionFactory<T> extends BeanFactory<T> {

    public DefinitionFactory(FactorySet factorySet, FactoryDefinition<T> factoryDefinition) {
        super(factorySet, factoryDefinition.getType(), factoryDefinition::onBuild);
    }
}
