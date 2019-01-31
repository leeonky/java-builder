package com.github.leeonky;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class FactorySetTest {
    private final FactorySet factorySet = new FactorySet();

    static class Bean {
        private String strValue;

        public void setStrValue(String strValue) {
            this.strValue = strValue;
        }
    }

    @Nested
    class Register {

        @Test
        void register_and_build() {
            factorySet.register(Bean.class, bean -> bean.setStrValue("Hello"));

            assertThat(factorySet.type(Bean.class).build())
                    .hasFieldOrPropertyWithValue("strValue", "Hello");
        }
    }
}