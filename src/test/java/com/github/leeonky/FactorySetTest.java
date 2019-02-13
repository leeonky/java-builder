package com.github.leeonky;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.stream.Collectors;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class FactorySetTest {
    private final FactorySet factorySet = new FactorySet();

    @Test
    void register_and_build() {
        factorySet.register(Bean.class, bean -> bean.setStringValue("Hello"));

        assertThat(factorySet.type(Bean.class).build())
                .hasFieldOrPropertyWithValue("stringValue", "Hello");
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
        factorySet.register(Bean.class, (bean, seq) -> bean.setStringValue("Hello" + seq));
        Builder<Bean> builder = factorySet.type(Bean.class);

        assertThat(builder.build().getStringValue()).isEqualTo("Hello1");
        assertThat(builder.build().getStringValue()).isEqualTo("Hello2");
    }

    @Test
    void register_with_sequence_and_params() {
        factorySet.register(Bean.class, (bean, seq, params) -> bean.setStringValue("Hello " + params.get("message")));

        assertThat(factorySet.type(Bean.class).params(new HashMap<String, Object>() {{
            put("message", "world");
        }}).build().getStringValue()).isEqualTo("Hello world");
    }

    @Test
    void build_object_list() {
        factorySet.register(Bean.class, (bean, seq) -> bean.setStringValue("Hello" + seq));

        assertThat(factorySet.type(Bean.class).build(2).map(Bean::getStringValue).collect(Collectors.toList()))
                .isEqualTo(asList("Hello1", "Hello2"));
    }

    @Test
    void build_with_property() {
        factorySet.register(Bean.class, bean -> {
        });

        assertThat(factorySet.type(Bean.class).properties(new HashMap<String, Object>() {{
            put("stringValue", "Hello");
        }}).build()).hasFieldOrPropertyWithValue("stringValue", "Hello");
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