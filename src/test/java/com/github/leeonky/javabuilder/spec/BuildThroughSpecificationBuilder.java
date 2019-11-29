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
    void support_define_specification_in_class() {
        factorySet.define(Objects.USD.class);

        assertThat(factorySet.toBuild(Objects.USD.class).build())
                .hasFieldOrPropertyWithValue("currency", "USD")
        ;
    }

    @Test
    void should_call_default_type_build_as_base_building() {
        factorySet.onBuild(Objects.Money.class, (m -> m.setAmount(100)));

        factorySet.define(Objects.USD.class);

        assertThat(factorySet.toBuild(Objects.USD.class).build())
                .hasFieldOrPropertyWithValue("amount", 100);
    }

    public static class Objects {

        @Getter
        @Setter
        @Accessors(chain = true)
        public static class Money {
            private int amount;
            private String currency;
        }

        public static class USD extends BeanSpecification<Money> {
            @Override
            public void specifications(SpecificationBuilder<Money> specificationBuilder) {
                specificationBuilder.propertyValue("currency", "USD");
            }
        }
    }
}
