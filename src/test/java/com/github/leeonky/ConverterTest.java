package com.github.leeonky;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Java6Assertions.assertThat;

class ConverterTest {
    private Converter converter = new Converter();

    @Test
    void no_right_converter_should_return_original_value() {
        converter.addTypeConverter(Long.class, Bean.class, s -> null);

        assertThat(converter.tryConvert(Bean.class, "String")).isEqualTo("String");
    }

    @Test
    void no_defined_converter_should_return_original_value() {
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
}
