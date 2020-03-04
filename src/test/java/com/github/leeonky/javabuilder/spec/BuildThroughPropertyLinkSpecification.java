package com.github.leeonky.javabuilder.spec;

import com.github.leeonky.javabuilder.BeanContext;
import com.github.leeonky.javabuilder.BeanSpecs;
import com.github.leeonky.javabuilder.FactorySet;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class BuildThroughPropertyLinkSpecification {
    private final FactorySet factorySet = new FactorySet();

    public static class Objects {
        @Getter
        @Setter
        @Accessors(chain = true)
        public static class Address {
            private String province, city;
        }

        @Getter
        @Setter
        @Accessors(chain = true)
        public static class Product {
            private Money price, tax;
        }

        @Getter
        @Setter
        @Accessors(chain = true)
        public static class Money {
            private int amount;
            private String currency;
        }

        public static class OneProduct extends BeanSpecs<Product> {
            @Override
            public void specs(BeanContext<Product> beanContext) {
                beanContext.link("price.currency", "tax.currency");
            }
        }

        public static class Municipality extends BeanSpecs<Address> {
            @Override
            public void specs(BeanContext<Address> beanContext) {
                beanContext.link("province", "city");
            }
        }

        public static class Capital extends BeanSpecs<Address> {
            @Override
            public void specs(BeanContext<Address> beanContext) {
                beanContext.property("city").value("Beijing");
                beanContext.link("province", "city");
            }
        }

        public static class IgnoreSupplierCapital extends BeanSpecs<Address> {
            @Override
            public void specs(BeanContext<Address> beanContext) {
                beanContext.property("city").from(() -> {
                    throw new RuntimeException();
                });
                beanContext.link("province", "city");
            }
        }
    }

    @Nested
    class InCurrentProperty {

        @Test
        void support_link_property_with_default_create_value() {
            Objects.Address address = factorySet.toBuild(Objects.Municipality.class).create();

            assertThat(address.getProvince()).isEqualTo(address.getCity());
        }

        @Test
        void support_link_property_with_spec_value() {
            Objects.Address address = factorySet.toBuild(Objects.Capital.class).create();

            assertThat(address.getProvince()).isEqualTo("Beijing");
        }

        @Test
        void support_link_property_with_specific_value() {
            Objects.Address address = factorySet.toBuild(Objects.Municipality.class).property("city", "City").create();

            assertThat(address.getProvince()).isEqualTo("City");
        }

        @Test
        void should_ignore_spec_when_specify_in_property() {
            Objects.Address address = factorySet.toBuild(Objects.IgnoreSupplierCapital.class).property("province", "Shanghai").create();

            assertThat(address.getCity()).isEqualTo("Shanghai");
        }
    }
}
