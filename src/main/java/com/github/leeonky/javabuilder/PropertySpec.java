package com.github.leeonky.javabuilder;

import java.util.Objects;
import java.util.function.Supplier;

class PropertySpec {
    private final Class<?> type;
    private final String conditionPropertyChain;
    private final Object conditionValue;
    private final PropertyChain property;
    private Supplier<?> supplier;

    PropertySpec(Class<?> type, PropertyChain property, Supplier<?> supplier, String conditionPropertyChain, Object conditionValue) {
        this.type = type;
        this.property = property;
        this.supplier = new SupplierOnce<>(supplier);
        this.conditionPropertyChain = conditionPropertyChain.replaceAll("\\(.*\\)", "").replaceAll(".+\\.", "");
        this.conditionValue = conditionValue;
    }

    void apply(Object object) {
        property.setTo(object, supplier.get());
    }

    void tryMerge(PropertySpec propertySpec) {
        if (type.equals(propertySpec.type)
                && conditionPropertyChain.equals(propertySpec.conditionPropertyChain)
                && Objects.equals(conditionValue, propertySpec.conditionValue)) {
            supplier = propertySpec.supplier;
        }
    }
}
