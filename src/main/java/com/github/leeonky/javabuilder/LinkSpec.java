package com.github.leeonky.javabuilder;

import java.util.List;

class LinkSpec {
    private final List<PropertyChain> propertyChains;

    LinkSpec(List<PropertyChain> propertyChains) {
        this.propertyChains = propertyChains;
    }

    void apply(Object object) {
        PropertyChain propertyChain = propertyChains.get(0);
        Object value = propertyChain.getFrom(object);
        for (int i = 1; i < propertyChains.size(); i++)
            propertyChains.get(i).setTo(object, value);
    }
}
