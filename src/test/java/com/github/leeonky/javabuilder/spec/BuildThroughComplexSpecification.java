package com.github.leeonky.javabuilder.spec;

import com.github.leeonky.javabuilder.BeanSpecification;
import com.github.leeonky.javabuilder.FactorySet;
import com.github.leeonky.javabuilder.SpecificationBuilder;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class BuildThroughComplexSpecification {
    private final FactorySet factorySet = new FactorySet();

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
            private String shortName;
            private String shortCut;
            private Money price;
            private Money discount;
        }

        public static class ProductWithName extends BeanSpecification<Product> {

            @Override
            public void specifications(SpecificationBuilder<Product> specificationBuilder) {
                specificationBuilder.propertyDependsOn("shortCut", "shortName",
                        (shortName) -> String.valueOf(((String) shortName).charAt(0)));

                specificationBuilder.propertyDependsOn("shortName", "name",
                        (name) -> ((String) name).split(" ")[0]);
            }
        }
    }

    @Nested
    class ValueDependent {

        @Test
        void should_support_build_property_value_from_dependency_chain_with_dependency_orders() {
            factorySet.define(Objects.ProductWithName.class);

            assertThat(factorySet.toBuild(Objects.ProductWithName.class).property("name", "java book").build())
                    .hasFieldOrPropertyWithValue("shortName", "java")
                    .hasFieldOrPropertyWithValue("shortCut", "j")
            ;
        }

        @Test
        void should_skip_dependency_logic_when_specify_value_in_property() {
            factorySet.define(Objects.ProductWithName.class);

            assertThat(factorySet.toBuild(Objects.ProductWithName.class).property("shortName", "book").build())
                    .hasFieldOrPropertyWithValue("shortCut", "b")
            ;
        }
    }
}
