package com.github.leeonky.javabuilder.spec;

import com.github.leeonky.javabuilder.BeanContext;
import com.github.leeonky.javabuilder.BuildingContext;

import java.util.List;
import java.util.stream.Collectors;

public class LinkSpec {
    private final List<PropertyChain> propertyChains;
    private final BuildingContext buildingContext;

    public LinkSpec(BuildingContext buildingContext, List<PropertyChain> propertyChains) {
        this.buildingContext = buildingContext;
        this.propertyChains = propertyChains;
    }

    public void preApply(BeanContext<?> beanContext) {
        List<PropertyChain> specified = filterSpecifiedPropertyChains(beanContext);
        if (specified.size() > 0) {
            filterUnspecifiedPropertyChains(beanContext)
                    .forEach(propertyChain -> buildingContext.removeSupplierSpec(propertyChain));
            return;
        }
    }

    public void apply(Object object, BeanContext<?> beanContext) {
        List<PropertyChain> specified = filterSpecifiedPropertyChains(beanContext);
        if (specified.size() > 0) {
            Object value = specified.get(0).getFrom(object);
            filterUnspecifiedPropertyChains(beanContext)
                    .forEach(propertyChain -> propertyChain.setTo(object, value));
            return;
        }
        List<PropertyChain> supplierSpecs = propertyChains.stream()
                .filter(propertyChain -> buildingContext.isSupplierSpec(propertyChain))
                .collect(Collectors.toList());
        if (supplierSpecs.size() > 0) {
            Object value = supplierSpecs.get(0).getFrom(object);
            propertyChains.stream()
                    .filter(propertyChain -> !buildingContext.isSupplierSpec(propertyChain))
                    .forEach(propertyChain -> propertyChain.setTo(object, value));
        }
        linkPropertyByFirstDefaultValue(object);
    }

    private List<PropertyChain> filterUnspecifiedPropertyChains(BeanContext<?> beanContext) {
        return propertyChains.stream()
                .filter(propertyChain -> beanContext.isPropertyNotSpecified(propertyChain.getRootName()))
                .collect(Collectors.toList());
    }

    private List<PropertyChain> filterSpecifiedPropertyChains(BeanContext<?> beanContext) {
        return propertyChains.stream()
                .filter(propertyChain -> !beanContext.isPropertyNotSpecified(propertyChain.getRootName()))
                .collect(Collectors.toList());
    }

    private void linkPropertyByFirstDefaultValue(Object object) {
        PropertyChain propertyChain = propertyChains.get(0);
        Object value = propertyChain.getFrom(object);
        for (int i = 1; i < propertyChains.size(); i++)
            propertyChains.get(i).setTo(object, value);
    }
}
