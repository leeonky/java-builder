package com.github.leeonky.javabuilder.spec;

import com.github.leeonky.javabuilder.BeanSpecification;
import com.github.leeonky.javabuilder.Combination;
import com.github.leeonky.javabuilder.FactorySet;
import com.github.leeonky.javabuilder.SpecificationBuilder;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

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
    void support_build_via_specification_name() {
        factorySet.define(Objects.USD.class);

        assertThat(factorySet.toBuild("USD").build())
                .hasFieldOrPropertyWithValue("currency", "USD");
    }

    @Test
    void should_raise_error_when_specification_not_exist() {
        RuntimeException runtimeException = assertThrows(RuntimeException.class, () -> factorySet.toBuild(Objects.USD.class).build());
        assertThat(runtimeException).hasMessageContaining("Specification 'com.github.leeonky.javabuilder.spec.BuildThroughSpecificationBuilder$Objects$USD' not exists");
    }

    @Test
    void should_raise_error_when_specification_name_not_exist() {
        RuntimeException runtimeException = assertThrows(RuntimeException.class, () -> factorySet.toBuild("USD").build());
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

    @Test
    void should_support_use_supplier_in_property() {
        factorySet.define(Objects.ProductWithSupplier.class);

        assertThat(factorySet.toBuild(Objects.ProductWithSupplier.class).build().getPrice().getAmount())
                .isEqualTo(100);
    }

    @Test
    void support_override_sub_build_in_specification() {
        factorySet.define(Objects.USD.class);
        factorySet.define(Objects.ProductOverrideSpecification.class);

        assertThat(factorySet.toBuild(Objects.ProductOverrideSpecification.class).build().getPrice().getCurrency())
                .isEqualTo("CNY");
    }

    @Test
    void support_define_combination_in_class() {
        factorySet.define(Objects.USD.class);

        assertThat(factorySet.toBuild(Objects.USD.class).combine("_100").build().getAmount())
                .isEqualTo(100);
    }

    @Test
    void specification_combination_name_in_method_annotation() {
        factorySet.define(Objects.USD.class);

        assertThat(factorySet.toBuild(Objects.USD.class).combine("200").build().getAmount())
                .isEqualTo(200);
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
                specificationBuilder.property("currency").hasValue("USD");
            }

            @Combination
            public void _100(SpecificationBuilder<Money> specificationBuilder) {
                specificationBuilder.property("amount").hasValue(100);
            }

            @Combination("200")
            public void combination200(SpecificationBuilder<Money> specificationBuilder) {
                specificationBuilder.property("amount").hasValue(200);
            }
        }

        public static class ConflictNameUSD extends BeanSpecification<Money> {
            @Override
            public String getName() {
                return "USD";
            }
        }

        public static class ProductInUSD extends BeanSpecification<Product> {
            @Override
            public void specifications(SpecificationBuilder<Product> specificationBuilder) {
                specificationBuilder.property("price").buildFrom(USD.class);
            }
        }

        public static class ProductWithSupplier extends BeanSpecification<Product> {
            @Override
            public void specifications(SpecificationBuilder<Product> specificationBuilder) {
                specificationBuilder.property("price").buildFrom(() -> new Money().setAmount(100));
            }
        }

        public static class ProductOverrideSpecification extends BeanSpecification<Product> {
            @Override
            public void specifications(SpecificationBuilder<Product> specificationBuilder) {
                specificationBuilder.property("price").buildFrom(USD.class, builder ->
                        builder.specifications(specificationBuilder1 -> {
                            specificationBuilder1.property("currency").hasValue("CNY");
                        }));
            }
        }
    }
}
