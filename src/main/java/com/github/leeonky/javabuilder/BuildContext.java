package com.github.leeonky.javabuilder;

import com.github.leeonky.util.BeanClass;
import com.github.leeonky.util.PropertyWriter;

import java.util.Map;
import java.util.function.Supplier;

public class BuildContext<T> {
    private final Map<String, Object> params;
    private final Map<String, Object> properties;
    private final int sequence;
    private final PropertyBuilder propertyBuilder;
    private final BeanClass<T> beanClass;
    private final FactorySet factorySet;

    public BuildContext(int sequence, Map<String, Object> properties,
                        Map<String, Object> params,
                        BeanClass<T> beanClass, FactorySet factorySet) {
        this.params = params;
        this.properties = properties;
        this.sequence = sequence;
        propertyBuilder = factorySet.getPropertyBuilder();
        this.beanClass = beanClass;
        this.factorySet = factorySet;
    }

    public Map<String, Object> getParams() {
        return params;
    }

    public int getSequence() {
        return sequence;
    }

    public BeanClass<T> getBeanClass() {
        return beanClass;
    }

    public BeanAssigner assignTo(T object) {
        return new BeanAssigner(object);
    }

    public boolean notSpecified(String name) {
        return !properties.containsKey(name);
    }

    class BeanAssigner {
        private final T object;

        BeanAssigner(T object) {
            this.object = object;
        }

        BeanAssigner setDefault() {
            propertyBuilder.buildDefaultProperty(object, BuildContext.this);
            return this;
        }

        public BeanAssigner setPropertyInFactory(String property, String factory) {
            if (notSpecified(property)) {
                PropertyWriter<T> propertyWriter = beanClass.getPropertyWriter(property);
                propertyWriter.setValue(object, factorySet.type(propertyWriter.getPropertyType(), factory).build());
            }
            return this;
        }

        public BeanAssigner setPropertyInSupplier(String property, Supplier<?> supplier) {
            if (notSpecified(property))
                beanClass.setPropertyValue(object, property, supplier.get());
            return this;
        }
    }
}
