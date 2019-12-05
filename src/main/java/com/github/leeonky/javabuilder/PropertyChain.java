package com.github.leeonky.javabuilder;

class PropertyChain {
    private final String name, condition, specificationName;

    private PropertyChain(String chain) {
        String[] propertyList = chain.split("\\.", 2);
        condition = propertyList[1];
        String propertyName = propertyList[0];
        if (propertyName.contains("(")) {
            String[] propertyFactory = propertyName.split("\\(");
            propertyName = propertyFactory[0];
            specificationName = propertyFactory[1].split("\\)")[0];
        } else {
            specificationName = null;
        }
        name = propertyName;
    }

    static PropertyChain parse(String chain) {
        return new PropertyChain(chain);
    }

    public String getName() {
        return name;
    }

    public String getCondition() {
        return condition;
    }

    Builder<?> toBuilder(FactorySet factorySet, Class<?> type, Object param) {
        return (specificationName != null ? factorySet.toBuild(specificationName) : factorySet.type(type))
                .property(getCondition(), param);
    }
}
