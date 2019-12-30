package com.github.leeonky.javabuilder.spec;

import com.github.leeonky.javabuilder.Builder;
import com.github.leeonky.javabuilder.FactorySet;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.stream.Collectors;

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class BuildThroughBeanClass {
    private final FactorySet factorySet = new FactorySet();

    @Getter
    @Setter
    @Accessors(chain = true)
    public static class Bean {
        private String stringValue;
    }

    @Nested
    class WithNoDefaultConstructor {

        @Test
        void register_and_build_with_no_default_constructor() {
            factorySet.register(BeanWithNoDefaultConstructor.class, () -> {
                BeanWithNoDefaultConstructor bean = new BeanWithNoDefaultConstructor(1);
                bean.setStringValue("Hello");
                return bean;
            });

            assertThat(factorySet.type(BeanWithNoDefaultConstructor.class).create())
                    .hasFieldOrPropertyWithValue("stringValue", "Hello");
        }

        @Test
        void with_sequence_and_params() {
            factorySet.register(BeanWithNoDefaultConstructor.class, (buildingContext) -> {
                BeanWithNoDefaultConstructor bean = new BeanWithNoDefaultConstructor(buildingContext.getCurrentSequence());
                bean.setStringValue(buildingContext.param("stringValue"));
                return bean;
            });

            assertThat(factorySet.type(BeanWithNoDefaultConstructor.class).param("stringValue", "Hello").create())
                    .hasFieldOrPropertyWithValue("intValue", 1)
                    .hasFieldOrPropertyWithValue("stringValue", "Hello");
        }
    }

    @Nested
    class StandardBean {

        @Test
        void register_and_build() {
            factorySet.onBuild(Bean.class, bean -> bean.setStringValue("Hello"));

            assertThat(factorySet.type(Bean.class).create())
                    .hasFieldOrPropertyWithValue("stringValue", "Hello");
        }

        @Test
        void support_simple_property_build() {
            factorySet.onBuild(Bean.class, bean -> {
            });

            assertThat(factorySet.type(Bean.class).property("stringValue", "new string value").create())
                    .hasFieldOrPropertyWithValue("stringValue", "new string value");
        }

        @Test
        void register_with_sequence() {
            factorySet.onBuild(Bean.class, (bean, buildContext) -> bean.setStringValue("Hello" + buildContext.getCurrentSequence()));
            Builder<Bean> builder = factorySet.type(Bean.class);

            assertThat(builder.create().getStringValue()).isEqualTo("Hello1");
            assertThat(builder.create().getStringValue()).isEqualTo("Hello2");
        }

        @Test
        void register_with_params() {
            factorySet.onBuild(Bean.class, (bean, buildContext) -> bean.setStringValue(buildContext.param("message")));
            Builder<Bean> builder = factorySet.type(Bean.class);

            assertThat(builder.param("message", "hello").create().getStringValue()).isEqualTo("hello");
        }

        @Test
        void build_with_property_map() {
            factorySet.onBuild(Bean.class, bean -> {
            });

            assertThat(factorySet.type(Bean.class).properties(new HashMap<String, Object>() {{
                put("stringValue", "Hello");
            }}).create()).hasFieldOrPropertyWithValue("stringValue", "Hello");
        }

        @Test
        void should_raise_error_when_no_default_constructor() {
            IllegalStateException exception = assertThrows(IllegalStateException.class, () -> factorySet.onBuild(BeanWithNoDefaultConstructor.class, bean -> {
            }));

            assertThat(exception.getMessage()).isEqualTo("No default constructor of class: " + BeanWithNoDefaultConstructor.class.getName());
        }

        @Test
        void support_convert_property_value_to_target_type_in_build() {
            factorySet.onBuild(Bean.class, bean -> {
            });

            assertThat(factorySet.type(Bean.class).property("stringValue", 1).create())
                    .hasFieldOrPropertyWithValue("stringValue", "1");
        }

        @Test
        void support_specify_specifications_in_build() {
            assertThat(factorySet.type(Bean.class).specifications(specificationBuilder -> specificationBuilder.property("stringValue").hasValue("hello")).create())
                    .hasFieldOrPropertyWithValue("stringValue", "hello");
        }

        @Test
        void support_define_combination() {
            factorySet.factory(Bean.class).combinable("com", specificationBuilder -> specificationBuilder.property("stringValue").hasValue("hello"));

            assertThat(factorySet.type(Bean.class).combine("com").create())
                    .hasFieldOrPropertyWithValue("stringValue", "hello");
        }

        @Test
        void raise_error_when_combination_not_exist() {
            RuntimeException exception = assertThrows(RuntimeException.class, () -> factorySet.type(Bean.class).combine("com").create());

            assertThat(exception).hasMessageContaining("Combination 'com' not exist");
        }

        @Test
        void should_support_build_a_list() {
            assertThat(factorySet.type(Bean.class).properties(singletonList(new HashMap<String, Object>() {{
                put("stringValue", "hello");
            }})).map(Builder::create).collect(Collectors.toList()).get(0)).hasFieldOrPropertyWithValue("stringValue", "hello");
        }
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
