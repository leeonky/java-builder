package com.github.leeonky.javabuilder;

import java.util.List;
import java.util.stream.Collectors;

class LinkSpec {
    private final List<PropertyChain> propertyChains;

    LinkSpec(List<PropertyChain> propertyChains) {
        this.propertyChains = propertyChains;
    }

    public void preApply(Object object, BeanContext<?> beanContext) {
        List<PropertyChain> specified = filterSpecifiedPropertyChains(beanContext);
        if (specified.size() > 0) {
            filterUnspecifiedPropertyChains(beanContext)
                    .forEach(propertyChain -> beanContext.getBuildingContext().removeSupplierSpec(propertyChain));
            return;
        }
    }

    void apply(Object object, BeanContext<?> beanContext) {
        List<PropertyChain> specified = filterSpecifiedPropertyChains(beanContext);
        if (specified.size() > 0) {
            Object value = specified.get(0).getFrom(object);
            filterUnspecifiedPropertyChains(beanContext)
                    .forEach(propertyChain -> propertyChain.setTo(object, value));
            return;
        }
        List<PropertyChain> supplierSpecs = propertyChains.stream()
                .filter(propertyChain -> beanContext.getBuildingContext().isSupplierSpec(propertyChain))
                .collect(Collectors.toList());
        if (supplierSpecs.size() > 0) {
            Object value = supplierSpecs.get(0).getFrom(object);
            propertyChains.stream()
                    .filter(propertyChain -> !beanContext.getBuildingContext().isSupplierSpec(propertyChain))
                    .forEach(propertyChain -> propertyChain.setTo(object, value));
        }
        linkPropertyByFirstDefaultValue(object);
    }

    private List<PropertyChain> filterUnspecifiedPropertyChains(BeanContext<?> beanContext) {
        return propertyChains.stream()
                .filter(beanContext::isPropertyNotSpecified)
                .collect(Collectors.toList());
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
