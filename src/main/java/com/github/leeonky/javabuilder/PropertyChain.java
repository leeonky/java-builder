package com.github.leeonky.javabuilder;

import java.util.stream.Stream;

class PropertyChain {
    private final String name, condition, specificationName;
    private final String[] combinations;

    private PropertyChain(String chain) {
        String[] propertyList = chain.split("\\.", 2);
        condition = propertyList[1];
        String propertyName = propertyList[0];
        if (propertyName.contains("(")) {
            String[] propertyFactory = propertyName.split("\\(");
            propertyName = propertyFactory[0];
            String[] combinedSpecificationName = propertyFactory[1].split("\\)")[0].split(",");
            combinations = Stream.of(combinedSpecificationName)
                    .limit(combinedSpecificationName.length - 1)
                    .map(String::trim)
                    .toArray(String[]::new);
            specificationName = combinedSpecificationName[combinedSpecificationName.length - 1].trim();
        } else {
            specificationName = null;
            combinations = null;
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
        return (specificationName != null ? factorySet.toBuild(specificationName).combine(combinations)
                : factorySet.type(type))
                .property(getCondition(), param);
    }
}
