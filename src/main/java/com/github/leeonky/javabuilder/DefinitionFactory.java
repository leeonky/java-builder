package com.github.leeonky.javabuilder;

class DefinitionFactory<T> extends BeanFactory<T> {

    private final FactoryDefinition<T> factoryDefinition;

    DefinitionFactory(FactorySet factorySet, FactoryDefinition<T> factoryDefinition) {
        super(factorySet, factoryDefinition.getType(), factoryDefinition::onBuild);
        this.factoryDefinition = factoryDefinition;
    }

    @Override
    public Factory<T> registerAlias() {
        return registerAlias(factoryDefinition.getAlias());
    }
}
