package com.github.leeonky.javabuilder.spec;

import com.github.leeonky.javabuilder.BeanContext;
import com.github.leeonky.javabuilder.BeanSpecs;
import com.github.leeonky.javabuilder.FactorySet;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;

class BuildThroughComplexSpecification {
    private final FactorySet factorySet = new FactorySet();

    public static class Objects {

        @Getter
        @Setter
        @Accessors(chain = true)
        public static class Product {
            private int price, discount, tax, taxDiscount;
            private int minPriceWithoutTax;
        }

        public static class ProductWithDiscount extends BeanSpecs<Product> {

            @Override
            public void specs(BeanContext<Product> beanContext) {
                beanContext.property("taxDiscount").dependsOn("tax", (tax) -> ((int) tax) / 100);
                beanContext.property("tax").dependsOn("price", (price) -> ((int) price) / 10);
            }
        }

        public static class ProductWithMultiDependency extends BeanSpecs<Product> {

            @Override
            public void specs(BeanContext<Product> beanContext) {
                beanContext.property("minPriceWithoutTax").dependsOn(asList("tax", "price"),
                        (args) -> (int) args.get(1) - (int) args.get(0));
            }
        }

        public static class SkipSupplierWhenHasDependency extends BeanSpecs<Product> {

            @Override
            public void specs(BeanContext<Product> beanContext) {
                beanContext.property("tax").dependsOn("price", (price) -> ((int) price) / 10);
                beanContext.property("tax").from(() -> {
                    throw new RuntimeException();
                });
            }
        }

        public static class OverrideSupplierWhenDefineDependency extends BeanSpecs<Product> {

            @Override
            public void specs(BeanContext<Product> beanContext) {
                beanContext.property("tax").from(() -> {
                    throw new RuntimeException();
                });
                beanContext.property("tax").dependsOn("price", (price) -> ((int) price) / 10);
            }
        }

        @Getter
        @Setter
        @Accessors(chain = true)
        public static class Father {
            private String familyName;
            private int age;
            private Son son;
        }

        @Getter
        @Setter
        @Accessors(chain = true)
        public static class Son {
            private String familyName;
            private int age;
        }

        public static class AFather extends BeanSpecs<Father> {

            @Override
            public void specs(BeanContext<Father> beanContext) {
                beanContext.property("son").from(ASon.class);
                beanContext.property("son.familyName").dependsOn("familyName", name -> name);
            }
        }

        public static class ASon extends BeanSpecs<Son> {
            @Override
            public void specs(BeanContext<Son> beanContext) {
                beanContext.property("familyName").from(() -> {
                    throw new RuntimeException("Should not run to here");
                });
            }
        }
    }

    @Nested
    class ValueDependent {

        @Test
        void should_support_build_property_value_from_dependency_chain_with_dependency_orders() {
            assertThat(factorySet.toBuild(Objects.ProductWithDiscount.class).property("price", 10000).create())
                    .hasFieldOrPropertyWithValue("tax", 1000)
                    .hasFieldOrPropertyWithValue("taxDiscount", 10);
        }

        @Test
        void should_skip_dependency_logic_when_specify_value_in_property() {
            assertThat(factorySet.toBuild(Objects.ProductWithDiscount.class).property("tax", 100).create())
                    .hasFieldOrPropertyWithValue("taxDiscount", 1)
            ;
        }

        @Test
        void should_support_build_property_value_from_multi_dependency() {
            assertThat(factorySet.toBuild(Objects.ProductWithMultiDependency.class)
                    .property("tax", 100)
                    .property("price", 2000)
                    .create())
                    .hasFieldOrPropertyWithValue("minPriceWithoutTax", 1900)
            ;
        }

        @Test
        void should_skip_supplier_value_when_has_dependency_spec() {
            assertThat(factorySet.toBuild(Objects.SkipSupplierWhenHasDependency.class).property("price", 10000).create())
                    .hasFieldOrPropertyWithValue("tax", 1000);
        }

        @Test
        void should_override_supplier_value_when_add_dependency_spec() {
            assertThat(factorySet.toBuild(Objects.OverrideSupplierWhenDefineDependency.class).property("price", 10000).create())
                    .hasFieldOrPropertyWithValue("tax", 1000);
        }
    }

    @Nested
    class SubValueDependent {

        @Test
        void should_set_via_dependency() {
            Objects.Father father = factorySet.toBuild(Objects.AFather.class).property("familyName", "Zhang").create();
            assertThat(father.getFamilyName()).isEqualTo(father.getSon().getFamilyName());
        }

        @Test
        void should_ignore_when_specify_in_property() {
            Objects.Father father = factorySet.toBuild(Objects.AFather.class)
                    .property("familyName", "Zhang")
                    .property("son", new Objects.Son().setFamilyName("Wang"))
                    .create();
            assertThat(father.getSon().getFamilyName()).isEqualTo("Wang");
        }
    }
}
