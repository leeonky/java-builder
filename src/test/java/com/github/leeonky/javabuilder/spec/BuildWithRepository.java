package com.github.leeonky.javabuilder.spec;

import com.github.leeonky.javabuilder.BeanSpecification;
import com.github.leeonky.javabuilder.Combination;
import com.github.leeonky.javabuilder.FactorySet;
import com.github.leeonky.javabuilder.SpecificationBuilder;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class BuildWithRepository {
    private final FactorySet factorySet = new FactorySet();

    @Getter
    @Setter
    @Accessors(chain = true)
    public static class Bean {
        private String stringValue;
        private int intValue;
    }

    @Getter
    @Setter
    @Accessors(chain = true)
    public static class Category {
        private String name;
        private int level;
    }

    @Getter
    @Setter
    @Accessors(chain = true)
    public static class Order {
        private Product product;
    }

    @Getter
    @Setter
    @Accessors(chain = true)
    public static class Product {
        private String name;
        private Category category;
        private boolean issued;
    }

    public static class ProgrammeBook extends BeanSpecification<Product> {
        @Override
        public void specifications(SpecificationBuilder<Product> specificationBuilder) {
            specificationBuilder.property("name").eq("Java");
        }

        @Combination("Issued")
        public void issued(SpecificationBuilder<Product> specificationBuilder) {
            specificationBuilder.property("issued").eq(true);
        }
    }

    @Nested
    class Query {

        @Test
        void should_support_cache_and_query_object() {
            Bean bean = factorySet.type(Bean.class).property("stringValue", "hello").create();

            List<Bean> queriedBean = factorySet.type(Bean.class).property("stringValue", "hello").query();

            assertThat(bean).isEqualTo(queriedBean.get(0));

            factorySet.getDataRepository().clear();
            assertThat(factorySet.type(Bean.class).property("stringValue", "hello").query()).isEmpty();
        }

        @Test
        void should_support_auto_convert_in_query() {
            Bean bean = factorySet.type(Bean.class).property("intValue", 1).create();

            assertThat(bean).isEqualTo(factorySet.type(Bean.class).property("intValue", "1").query().get(0));
        }

        @Test
        void should_support_query_with_property_chain() {
            Category category = factorySet.type(Category.class).property("name", "math").create();
            Product product = factorySet.type(Product.class).property("category", category).create();
            Order order = factorySet.type(Order.class).property("product", product).create();

            assertThat(factorySet.type(Order.class).property("product.category.name", "math").query())
                    .containsOnly(order);
        }

        @Test
        void support_auto_skip_factory_name() {
            Category category = factorySet.type(Category.class).property("name", "math").create();
            Product product = factorySet.type(Product.class).property("category", category).create();
            Order order = factorySet.type(Order.class).property("product", product).create();

            assertThat(factorySet.type(Order.class).property("product.category.name", "math").query())
                    .containsOnly(order);
        }
    }

    @Nested
    class BuildWithRepo {

        @Test
        void should_support_build_object_with_object_reference() {
            Product product = factorySet.type(Product.class).property("name", "book").create();

            Order order = factorySet.type(Order.class).property("product.name", "book").create();

            assertThat(order.getProduct()).isEqualTo(product);
        }

        @Test
        void should_support_build_object_with_more_nested_object_reference() {
            Category category = factorySet.type(Category.class).property("name", "math").create();
            Product product = factorySet.type(Product.class).property("category", category).create();

            Order order = factorySet.type(Order.class).property("product.category.name", "math").create();

            assertThat(order.getProduct()).isEqualTo(product);
        }

        @Test
        void should_support_build_object_with_default_property_create_and_cache() {
            Order order = factorySet.type(Order.class).property("product.name", "book").create();

            assertThat(order.getProduct().getName()).isEqualTo("book");
            assertThat(factorySet.type(Product.class).property("name", "book").query().get(0)).isEqualTo(order.getProduct());
        }

        @Test
        void support_build_property_with_factory_name() {
            factorySet.define(ProgrammeBook.class);

            Order order = factorySet.type(Order.class).property("product(ProgrammeBook).category.name", "book").create();

            assertThat(order.getProduct().getName()).isEqualTo("Java");
        }

        @Test
        void support_build_property_with_factory_name_and_combination() {
            factorySet.define(ProgrammeBook.class);

            Order order = factorySet.type(Order.class).property("product(Issued, ProgrammeBook).category.name", "book").create();

            assertThat(order.getProduct().isIssued()).isTrue();
        }


        @Test
        void support_build_reference_object_first() {
            Product product = factorySet.type(Product.class).create();
            Order order = factorySet.type(Order.class).property("product.category.name", "book").create();

            assertThat(order).isInstanceOf(Order.class);
            assertThat(order.getProduct()).isNotEqualTo(product);
        }
    }
}
