package com.github.leeonky.javabuilder;

import com.github.leeonky.util.BeanClass;

import java.util.ArrayList;
import java.util.List;

public class SpecificationBuilder<T> {
    private final BeanClass<T> beanClass;
    private List<Specification<T>> specifications = new ArrayList<>();

    public SpecificationBuilder(BeanClass<T> beanClass) {
        this.beanClass = beanClass;
    }

    public void propertyValue(String property, Object value) {
        specifications.add(new PropertyValueSpecification(property, value));
    }

    public List<Specification<T>> collectSpecifications() {
        return specifications;
    }

    class PropertyValueSpecification implements Specification<T> {
        private final String property;
        private final Object value;

        PropertyValueSpecification(String property, Object value) {
            this.property = property;
            this.value = value;
        }

        @Override
        public void apply(T instance) {
            beanClass.setPropertyValue(instance, property, value);
        }
    }
}
