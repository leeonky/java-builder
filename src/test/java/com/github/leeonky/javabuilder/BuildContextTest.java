package com.github.leeonky.javabuilder;

import com.github.leeonky.util.BeanClass;
import org.junit.jupiter.api.Test;

import java.util.HashMap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;

class BuildContextTest {
    private final FactorySet factorySet = new FactorySet();
    private HashMap<String, Object> properties = new HashMap<>();
    private BuildContext<Product> beanContext = new BuildContext<>(0, properties, null, new BeanClass<>(Product.class), factorySet);
    private boolean called = false;

    @Test
    void should_support_set_by_factory_name() {
        Product product = new Product();

        beanContext.assignTo(product)
                .setPropertyDefaultInFactory("category", null);

        assertThat(product.getCategory()).isInstanceOf(Category.class);
    }

    @Test
    void alias_has_higher_priority() {
        factorySet.factory(Category.class).extend("object", c -> c.setName("fruit"));
        factorySet.factory(Category.class).extend("animal", c -> c.setName("animal")).registerAlias("object");

        Product product = new Product();

        beanContext.assignTo(product)
                .setPropertyDefaultInFactory("category", "object");

        assertThat(product.getCategory().getName()).isEqualTo("animal");
    }

    @Test
    void should_skip_assign_when_specify_in_properties() {
        properties.put("category", null);
        Product product = new Product();

        beanContext.assignTo(product)
                .setPropertyDefaultInFactory("category", null);

        assertThat(product.getCategory()).isNull();
    }

    @Test
    void should_support_set_object_supplier() {
        Product product = new Product();
        beanContext.assignTo(product)
                .setPropertyDefaultInSupplier("name", () -> "book");

        assertThat(product.getName()).isEqualTo("book");
    }

    private String getString() {
        called = true;
        return "book";
    }

    @Test
    void should_skip_get_when_specify_in_properties() {
        Product product = new Product();
        properties.put("name", "milk");

        beanContext.assignTo(product)
                .setPropertyDefaultInSupplier("name", this::getString);

        assertFalse(called);
        assertThat(product.getName()).isNull();
    }
}