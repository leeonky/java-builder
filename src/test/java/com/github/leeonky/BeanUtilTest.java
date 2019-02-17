package com.github.leeonky;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.HashMap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class BeanUtilTest {

    private final BeanUtil beanUtil = new BeanUtil();

    @Test
    void should_raise_error_when_got_exception_in_setter() {
        IllegalStateException illegalStateException = assertThrows(IllegalStateException.class,
                () -> beanUtil.assignProperties(new BeanErrorSetter(), new HashMap<String, Object>() {{
                    put("value", "Hello");
                }}));
        assertThat(illegalStateException).hasMessage("Got exception in 'com.github.leeonky.BeanErrorSetter::setValue'");
    }

    @Test
    void should_raise_error_when_no_setter() {
        IllegalStateException illegalStateException = assertThrows(IllegalStateException.class,
                () -> beanUtil.assignProperties(new Bean(), new HashMap<String, Object>() {{
                    put("value", "Hello");
                }}));
        assertThat(illegalStateException).hasMessage("No setter was found in 'com.github.leeonky.Bean' for property 'value'");
    }

    @Nested
    class TypeConvert {

        @Nested
        class AnyTypeToString {

            @Test
            void not_null() {
                assertThat(beanUtil.assignProperties(new Bean(), new HashMap<String, Object>() {{
                    put("stringValue", 100);
                }})).hasFieldOrPropertyWithValue("stringValue", "100");
            }

            @Test
            void is_null() {
                assertThat(beanUtil.assignProperties(new Bean(), new HashMap<String, Object>() {{
                    put("stringValue", null);
                }})).hasFieldOrPropertyWithValue("stringValue", null);
            }
        }

        @Nested
        class StringToNumber {
            @Test
            void to_long() {
                assertThat(beanUtil.assignProperties(new Bean(), new HashMap<String, Object>() {{
                    put("longValue", "100");
                }})).hasFieldOrPropertyWithValue("longValue", 100L);
            }

            @Test
            void to_int() {
                assertThat(beanUtil.assignProperties(new Bean(), new HashMap<String, Object>() {{
                    put("intValue", "100");
                }})).hasFieldOrPropertyWithValue("intValue", 100);
            }
        }

        //assign to base type
        //no defined converter
        //no defined source type converter
    }
}