package com.github.leeonky;

import org.junit.jupiter.api.Test;

import java.util.HashMap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class FactorySetTest {
    private final FactorySet factorySet = new FactorySet();

    @Test
    void register_and_build() {
        factorySet.register(Bean.class, bean -> bean.setStrValue("Hello"));

        assertThat(factorySet.type(Bean.class).build())
                .hasFieldOrPropertyWithValue("strValue", "Hello");
    }

    @Test
    void should_raise_error_when_no_default_constructor() {
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> factorySet.register(BeanWithNoDefaultConstructor.class, bean -> {
        }));

        assertThat(exception.getMessage()).isEqualTo("No default constructor of class: " + BeanWithNoDefaultConstructor.class.getName());
    }

    @Test
    void should_re_raise_error_when_got_exception_in_constructor() {
        factorySet.register(BeanWithExceptionConstructor.class, bean -> {
        });

        assertThrows(IllegalStateException.class, () -> factorySet.type(BeanWithExceptionConstructor.class).build());
    }

    @Test
    void register_with_sequence() {
        factorySet.register(Bean.class, (bean, seq) -> bean.setStrValue("Hello" + seq));
        Builder<Bean> builder = factorySet.type(Bean.class);

        assertThat(builder.build().getStrValue()).isEqualTo("Hello1");
        assertThat(builder.build().getStrValue()).isEqualTo("Hello2");
    }

    @Test
    void register_with_sequence_and_params() {
        factorySet.register(Bean.class, (bean, seq, params) -> bean.setStrValue("Hello " + params.get("message")));

        assertThat(factorySet.type(Bean.class).params(new HashMap<String, Object>() {{
            put("message", "world");
        }}).build().getStrValue()).isEqualTo("Hello world");
    }

    static class Bean {
        private String strValue;

        public String getStrValue() {
            return strValue;
        }

        public void setStrValue(String strValue) {
            this.strValue = strValue;
        }
    }

    static class BeanWithNoDefaultConstructor {
        BeanWithNoDefaultConstructor(int i) {
        }
    }

    static class BeanWithExceptionConstructor {
        BeanWithExceptionConstructor() throws Exception {
            throw new Exception("");
        }
    }
}