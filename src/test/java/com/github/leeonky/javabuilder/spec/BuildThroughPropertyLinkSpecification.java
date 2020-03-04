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
    }

    @Nested
    class InCurrentProperty {

        @Test
        void support_link_property_with_default_create_value() {
            Objects.Address address = factorySet.toBuild(Objects.Municipality.class).create();

            assertThat(address.getProvince()).isEqualTo(address.getCity());
        }
    }
}
