package com.github.leeonky.javabuilder.spec;

import com.github.leeonky.javabuilder.BeanSpecification;
import com.github.leeonky.javabuilder.FactorySet;
import com.github.leeonky.javabuilder.SpecificationBuilder;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class BuildThroughSpecificationBuilder {
    private final FactorySet factorySet = new FactorySet();

    @Test
    void support_define_specification_with_value_in_class() {
        factorySet.define(Objects.USD.class);

        assertThat(factorySet.toBuild(Objects.USD.class).build())
                .hasFieldOrPropertyWithValue("currency", "USD");
    }

    @Test
    void support_define_specification_with_factory_in_class() {
        factorySet.define(Objects.USD.class);
        factorySet.define(Objects.ProductInUSD.class);

        assertThat(factorySet.toBuild(Objects.ProductInUSD.class).build().getPrice())
                .hasFieldOrPropertyWithValue("currency", "USD");
    }

    @Test
    void should_call_default_type_build_as_base_building() {
        factorySet.onBuild(Objects.Money.class, (m -> m.setAmount(100)));

        factorySet.define(Objects.USD.class);

        assertThat(factorySet.toBuild(Objects.USD.class).build())
                .hasFieldOrPropertyWithValue("amount", 100);
    }

    @Test
    void should_skip_specification_when_given_property_value_in_build() {
        factorySet.define(Objects.ProductInUSD.class);

        assertThat(factorySet.toBuild(Objects.ProductInUSD.class).property("price", null).build().getPrice())
                .isNull();
    }

    public static class Objects {

        @Getter
        @Setter
        @Accessors(chain = true)
        public static class Money {
            private int amount;
            private String currency;
        }

        @Getter
        @Setter
        @Accessors(chain = true)
        public static class Product {
            private String name;
            private Money price;
            private Money discount;
        }

        public static class USD extends BeanSpecification<Money> {
            @Override
            public void specifications(SpecificationBuilder<Money> specificationBuilder) {
                specificationBuilder.propertyValue("currency", "USD");
            }
        }

        public static class ProductInUSD extends BeanSpecification<Product> {
            @Override
            public void specifications(SpecificationBuilder<Product> specificationBuilder) {
                specificationBuilder.propertyFactory("price", USD.class);
            }
        }
    }
}
