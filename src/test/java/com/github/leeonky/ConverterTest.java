package com.github.leeonky;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Java6Assertions.assertThat;

class ConverterTest {
    private Converter converter = new Converter();

    @Test
    void no_candidate_converter_should_return_original_value() {
        converter.addTypeConverter(Long.class, Bean.class, s -> null);

        assertThat(converter.tryConvert(Bean.class, "String")).isEqualTo("String");
    }

    @Test
    void no_defined_converter_for_target_type_should_return_original_value() {
        assertThat(converter.tryConvert(Bean.class, "String")).isEqualTo("String");
    }

    @Test
    void assign_sub_type_to_base_should_keep_original() {
        Bean.SubBean subBean = new Bean.SubBean();
        converter.addTypeConverter(Bean.SubBean.class, Bean.class, sb -> {
            throw new RuntimeException();
        });

        assertThat(converter.tryConvert(Bean.class, subBean)).isEqualTo(subBean);
    }

    @Test
    void candidate_converter() {
        converter.addTypeConverter(String.class, Integer.class, Integer::valueOf);

        assertThat(converter.tryConvert(Integer.class, "100")).isEqualTo(100);
    }

    @Test
    void candidate_converter_as_base_type() {
        converter.addTypeConverter(Object.class, String.class, o -> "Hello");

        assertThat(converter.tryConvert(String.class, new Bean())).isEqualTo("Hello");
    }
}
