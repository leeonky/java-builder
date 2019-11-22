package com.github.leeonky.javabuilder.spec;

import com.github.leeonky.javabuilder.FactorySet;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.junit.jupiter.api.Test;

import java.util.HashMap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class BuildThroughBeanClass {
    private final FactorySet factorySet = new FactorySet();

    @Test
    void register_and_build() {
        factorySet.onBuild(Bean.class, bean -> bean.setStringValue("Hello"));

        assertThat(factorySet.type(Bean.class).build())
                .hasFieldOrPropertyWithValue("stringValue", "Hello");
    }

    @Test
    void support_simple_property_build() {
        factorySet.onBuild(Bean.class, bean -> {
        });

        assertThat(factorySet.type(Bean.class).property("stringValue", "new string value").build())
                .hasFieldOrPropertyWithValue("stringValue", "new string value");
    }

    @Test
    void build_with_property_map() {
        factorySet.onBuild(Bean.class, bean -> {
        });

        assertThat(factorySet.type(Bean.class).properties(new HashMap<String, Object>() {{
            put("stringValue", "Hello");
        }}).build()).hasFieldOrPropertyWithValue("stringValue", "Hello");
    }

    @Test
    void should_raise_error_when_no_default_constructor() {
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> factorySet.onBuild(BeanWithNoDefaultConstructor.class, bean -> {
        }));

        assertThat(exception.getMessage()).isEqualTo("No default constructor of class: " + BeanWithNoDefaultConstructor.class.getName());
    }

    @Test
    void register_and_build_with_no_default_constructor() {
        factorySet.register(BeanWithNoDefaultConstructor.class, () -> {
            BeanWithNoDefaultConstructor bean = new BeanWithNoDefaultConstructor(1);
            bean.setStringValue("Hello");
            return bean;
        });

        assertThat(factorySet.type(BeanWithNoDefaultConstructor.class).build())
                .hasFieldOrPropertyWithValue("stringValue", "Hello");
    }

    @Getter
    @Setter
    @Accessors(chain = true)
    public static class Bean {
        private String stringValue;
    }

    @Getter
    @Setter
    @Accessors(chain = true)
    public class BeanWithNoDefaultConstructor {
        private final int intValue;
        private String stringValue;

        BeanWithNoDefaultConstructor(int intValue) {
            this.intValue = intValue;
        }
    }
}