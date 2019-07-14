package com.github.leeonky.javabuilder;

import org.junit.jupiter.api.Test;

import java.util.HashMap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class FactorySetRepoTest {
    private FactorySet factorySet = new FactorySet();

    @Test
    void should_cache_object() {
        Bean bean = factorySet.type(Bean.class).property("stringValue", "hello").build();

        Bean queriedBean = factorySet.type(Bean.class).property("stringValue", "hello").query().get();

        assertTrue(bean == queriedBean);

        factorySet.getDataRepository().clear();
        assertThat(factorySet.type(Bean.class).property("stringValue", "hello").query()).isEmpty();
    }

    @Test
    void should_support_auto_convert_in_query() {
        Bean bean = factorySet.type(Bean.class).property("intValue", 1).build();

        assertTrue(bean == factorySet.type(Bean.class).property("intValue", "1").query().get());
    }

    @Test
    void should_support_build_object_with_object_reference() {
        Product product = factorySet.type(Product.class).property("name", "book").build();

        Order order = factorySet.type(Order.class).properties(new HashMap<String, Object>() {{
            put("product.name", "book");
        }}).build();

        assertThat(order.getProduct()).isEqualTo(product);
    }
}
