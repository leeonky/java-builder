package com.github.leeonky.javabuilder.spec;

import com.github.leeonky.javabuilder.Builder;
import com.github.leeonky.javabuilder.FactorySet;

import java.util.stream.Stream;

public class PropertyQueryChain {
    private final String baseName, condition, specificationName;
    private final String[] combinations;

    private PropertyQueryChain(String chain) {
        String[] propertyList = chain.split("\\.", 2);
        condition = propertyList[1];
        String propertyName = propertyList[0];
        if (propertyName.contains("(")) {
            String[] propertyFactory = propertyName.split("\\(");
            propertyName = propertyFactory[0];
            String[] combinedSpecificationName = propertyFactory[1].split("\\)")[0].split("[,\\ ]");
            combinations = Stream.of(combinedSpecificationName)
                    .limit(combinedSpecificationName.length - 1)
                    .map(String::trim)
                    .toArray(String[]::new);
            specificationName = combinedSpecificationName[combinedSpecificationName.length - 1].trim();
        } else {
            specificationName = null;
            combinations = new String[0];
        }
        baseName = propertyName;
    }

    public static PropertyQueryChain parse(String chain) {
        return new PropertyQueryChain(chain);
    }

    public String getBaseName() {
        return baseName;
    }

    public String getCondition() {
        return condition;
    }

    public Builder<?> toBuilder(FactorySet factorySet, Class<?> type, Object param) {
        return (specificationName != null ? factorySet.toBuild(specificationName) : factorySet.type(type))
                .combine(combinations)
                .property(getCondition(), param);
    }
}
