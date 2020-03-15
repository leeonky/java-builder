package com.github.leeonky.javabuilder.spec;

import java.util.function.Supplier;

public class PropertySpec {
    private final QueryExpression queryExpression;
    private final PropertyChain propertyChain;
    private Supplier<?> supplier;

    public PropertySpec(PropertyChain propertyChain, Supplier<?> supplier, QueryExpression queryExpression) {
        this.propertyChain = propertyChain;
        this.supplier = new SupplierOnce<>(supplier);
        this.queryExpression = queryExpression;
    }

    public void apply(Object object) {
        propertyChain.setTo(object, supplier.get());
    }

    public void tryMerge(PropertySpec propertySpec) {
        if (queryExpression.isSameQuery(propertySpec.queryExpression)) {
            supplier = propertySpec.supplier;
        }
    }

    public PropertyChain getPropertyChain() {
        return propertyChain;
    }

    private static class SupplierOnce<T> implements Supplier<T> {
        private final Supplier<T> supplier;

        private boolean got = false;

        private T object;

        public SupplierOnce(Supplier<T> supplier) {
            this.supplier = supplier;
        }

        @Override
        public T get() {
            if (got)
                return object;
            got = true;
            return object = supplier.get();
        }
    }
}
