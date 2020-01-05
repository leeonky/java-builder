package com.github.leeonky.javabuilder.spec;

import com.github.leeonky.javabuilder.BeanSpecs;
import com.github.leeonky.javabuilder.Combination;
import com.github.leeonky.javabuilder.FactorySet;
import com.github.leeonky.javabuilder.SpecBuilder;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class BuildThroughSpecBuilder {
    private final FactorySet factorySet = new FactorySet();

    @Test
    void support_define_specification_with_value_in_class() {
        assertThat(factorySet.toBuild(Objects.USD.class).create())
                .hasFieldOrPropertyWithValue("currency", "USD");
    }

    @Test
    void support_define_specification_with_factory_in_class() {
        assertThat(factorySet.toBuild(Objects.ProductInUSD.class).create().getPrice())
                .hasFieldOrPropertyWithValue("currency", "USD");
    }

    @Test
    void support_build_via_specification_name() {
        factorySet.define(Objects.USD.class);

        assertThat(factorySet.toBuild("USD").create())
                .hasFieldOrPropertyWithValue("currency", "USD");
    }

    @Test
    void should_raise_error_when_specification_name_not_exist() {
        RuntimeException runtimeException = assertThrows(RuntimeException.class, () -> factorySet.toBuild("USD").create());
        assertThat(runtimeException).hasMessageContaining("Specification 'USD' not exists");
    }

    @Test
    void should_raise_error_when_name_conflict() {
        factorySet.define(Objects.USD.class);

        RuntimeException runtimeException = assertThrows(RuntimeException.class, () -> factorySet.define(Objects.ConflictNameUSD.class));
        assertThat(runtimeException).hasMessageContaining("Specification 'USD' already exists");
    }

    @Test
    void should_call_default_type_build_as_base_building() {
        factorySet.onBuild(Objects.Money.class, (m -> m.setAmount(100))).spec(builder -> builder.property("symbol").eq("$"));

        assertThat(factorySet.toBuild(Objects.USD.class).create())
                .hasFieldOrPropertyWithValue("amount", 100)
                .hasFieldOrPropertyWithValue("symbol", "$");
    }

    @Test
    void should_skip_specification_when_given_property_value_in_build() {
        assertThat(factorySet.toBuild(Objects.ProductInUSD.class).property("price", null).create().getPrice())
                .isNull();
    }

    @Test
    void should_support_use_default_type_build_in_property() {
        assertThat(factorySet.toBuild(Objects.ProductInMoney.class).create().getPrice())
                .isInstanceOf(Objects.Money.class);
    }

    @Test
    void should_support_use_supplier_in_property() {
        assertThat(factorySet.toBuild(Objects.ProductWithSupplier.class).create().getPrice().getAmount())
                .isEqualTo(100);
    }

    @Test
    void should_support_skip_supplier_in_property() {
        assertThat(factorySet.toBuild(Objects.ProductSkipSupplier.class).property("price", null).create().getPrice()).isNull();
    }

    @Test
    void support_override_sub_build_in_specification() {
        assertThat(factorySet.toBuild(Objects.ProductOverrideSpecs.class).create().getPrice().getCurrency())
                .isEqualTo("CNY");
    }

    @Test
    void support_define_combination_in_class() {
        assertThat(factorySet.toBuild(Objects.USD.class).combine("_100").create().getAmount())
                .isEqualTo(100);
    }

    @Test
    void specification_combination_name_in_method_annotation() {
        assertThat(factorySet.toBuild(Objects.USD.class).combine("200").create().getAmount())
                .isEqualTo(200);
    }

    public static class Objects {

        @Getter
        @Setter
        @Accessors(chain = true)
        public static class Money {
            private int amount;
            private String currency;
            private String symbol;
        }

        @Getter
        @Setter
        @Accessors(chain = true)
        public static class Product {
            private String name;
            private Money price;
        }

        public static class USD extends BeanSpecs<Money> {
            @Override
            public void specs(SpecBuilder<Money> specBuilder) {
                specBuilder.property("currency").eq("USD");
            }

            @Combination
            public void _100(SpecBuilder<Money> specBuilder) {
                specBuilder.property("amount").eq(100);
            }

            @Combination("200")
            public void combination200(SpecBuilder<Money> specBuilder) {
                specBuilder.property("amount").eq(200);
            }
        }

        public static class ConflictNameUSD extends BeanSpecs<Money> {
            @Override
            public String getName() {
                return "USD";
            }
        }

        public static class ProductInMoney extends BeanSpecs<Product> {
            @Override
            public void specs(SpecBuilder<Product> specBuilder) {
                specBuilder.property("price").type(Money.class);
            }
        }

        public static class ProductInUSD extends BeanSpecs<Product> {
            @Override
            public void specs(SpecBuilder<Product> specBuilder) {
                specBuilder.property("price").from(USD.class);
            }
        }

        public static class ProductWithSupplier extends BeanSpecs<Product> {
            @Override
            public void specs(SpecBuilder<Product> specBuilder) {
                specBuilder.property("price").from(() -> new Money().setAmount(100));
            }
        }

        public static class ProductOverrideSpecs extends BeanSpecs<Product> {
            @Override
            public void specs(SpecBuilder<Product> specBuilder) {
                specBuilder.property("price").from(USD.class, builder ->
                        builder.spec(specificationBuilder1 -> {
                            specificationBuilder1.property("currency").eq("CNY");
                        }));
            }
        }

        public static class ProductSkipSupplier extends BeanSpecs<Product> {
            @Override
            public void specs(SpecBuilder<Product> specBuilder) {
                specBuilder.property("price").from(Assertions::fail);
            }
        }
    }
}
