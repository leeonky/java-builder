package com.github.leeonky.javabuilder.spec;

import com.github.leeonky.javabuilder.*;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FactorySetRepoTest {
    private FactorySet factorySet = new FactorySet();

    @Nested
    class Query {

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
        void should_support_query_with_property_chain() {
            Category category = factorySet.type(Category.class).property("name", "math").build();
            Product product = factorySet.type(Product.class).property("category", category).build();
            Order order = factorySet.type(Order.class).property("product", product).build();

            assertThat(factorySet.type(Order.class).property("product.category.name", "math").query())
                    .hasValue(order);
        }

        @Test
        void support_auto_skip_factory_name() {
            Category category = factorySet.type(Category.class).property("name", "math").build();
            Product product = factorySet.type(Product.class).property("category", category).build();
            Order order = factorySet.type(Order.class).property("product", product).build();

            assertThat(factorySet.type(Order.class).property("product(xxx).category(xxx).name", "math").query())
                    .hasValue(order);
        }
    }

    @Nested
    class BuildWithRepo {

        @Test
        void should_support_build_object_with_object_reference() {
            Product product = factorySet.type(Product.class).property("name", "book").build();

            Order order = factorySet.type(Order.class).property("product.name", "book").build();

            assertThat(order.getProduct()).isEqualTo(product);
        }

        @Test
        void should_support_build_object_with_more_nested_object_reference() {
            Category category = factorySet.type(Category.class).property("name", "math").build();
            Product product = factorySet.type(Product.class).property("category", category).build();

            Order order = factorySet.type(Order.class).property("product.category.name", "math").build();

            assertThat(order.getProduct()).isEqualTo(product);
        }

        @Test
        void should_support_build_object_with_default_property_build() {
            Order order = factorySet.type(Order.class).property("product.name", "book").build();

            assertThat(order.getProduct().getName()).isEqualTo("book");
        }

        @Test
        void support_build_property_with_factory_name() {
            factorySet.factory(Product.class).extend("ProgrammeBook", p -> p.setName("Java"));

            Order order = factorySet.type(Order.class).property("product(ProgrammeBook).category.name", "book").build();

            assertThat(order.getProduct().getName()).isEqualTo("Java");
        }
    }
}
