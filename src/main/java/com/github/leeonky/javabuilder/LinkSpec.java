package com.github.leeonky.javabuilder;

import java.util.List;
import java.util.stream.Collectors;

class LinkSpec {
    private final List<PropertyChain> propertyChains;

    LinkSpec(List<PropertyChain> propertyChains) {
        this.propertyChains = propertyChains;
    }

    void apply(Object object, BeanContext<?> beanContext) {
        List<PropertyChain> specified = filterSpecifiedPropertyChains(beanContext);
        List<PropertyChain> frees = propertyChains.stream()
                .filter(beanContext::isPropertyNotSpecified)
                .collect(Collectors.toList());
        if (specified.size() > 0) {
            Object value = specified.get(0).getFrom(object);
            frees.forEach(propertyChain -> propertyChain.setTo(object, value));
        } else {
            linkPropertyByFirstDefaultValue(object);
        }
    }

    private List<PropertyChain> filterSpecifiedPropertyChains(BeanContext<?> beanContext) {
        return propertyChains.stream()
                .filter(propertyChain -> !beanContext.isPropertyNotSpecified(propertyChain))
                .collect(Collectors.toList());
    }

    private void linkPropertyByFirstDefaultValue(Object object) {
        PropertyChain propertyChain = propertyChains.get(0);
        Object value = propertyChain.getFrom(object);
        for (int i = 1; i < propertyChains.size(); i++)
            propertyChains.get(i).setTo(object, value);
    }
}
