package com.github.leeonky;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.BigInteger;
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
        assertThat(illegalStateException).hasMessage("Got exception in 'com.github.leeonky.BeanErrorSetter::setValue(java.lang.String)', value is java.lang.String[Hello]");
    }

    @Test
    void should_raise_error_when_no_setter() {
        IllegalStateException illegalStateException = assertThrows(IllegalStateException.class,
                () -> beanUtil.assignProperties(new Bean(), new HashMap<String, Object>() {{
                    put("value", "Hello");
                }}));
        assertThat(illegalStateException).hasMessage("No setter was found in 'com.github.leeonky.Bean' for property 'value'");
    }

    @Test
    void type_convert() {
        assertThat(beanUtil.assignProperties(new Bean(), new HashMap<String, Object>() {{
            put("stringValue", new Object() {
                @Override
                public String toString() {
                    return "toString";
                }
            });
            put("longValue", "100");
            put("intValue", "100");
            put("shortValue", "100");
            put("byteValue", "100");
            put("doubleValue", "100");
            put("floatValue", "100");
            put("booleanValue", "true");

            put("boxedLongValue", "100");
            put("boxedIntValue", "100");
            put("boxedShortValue", "100");
            put("boxedByteValue", "100");
            put("boxedDoubleValue", "100");
            put("boxedFloatValue", "100");
            put("boxedBooleanValue", "true");

            put("bigIntegerValue", "100");
            put("bigDecimalValue", "100");
        }}))
                .hasFieldOrPropertyWithValue("stringValue", "toString")
                .hasFieldOrPropertyWithValue("longValue", 100L)
                .hasFieldOrPropertyWithValue("intValue", 100)
                .hasFieldOrPropertyWithValue("shortValue", (short) 100)
                .hasFieldOrPropertyWithValue("byteValue", (byte) 100)
                .hasFieldOrPropertyWithValue("doubleValue", (double) 100)
                .hasFieldOrPropertyWithValue("floatValue", (float) 100)
                .hasFieldOrPropertyWithValue("booleanValue", true)
                .hasFieldOrPropertyWithValue("boxedLongValue", 100L)
                .hasFieldOrPropertyWithValue("boxedIntValue", 100)
                .hasFieldOrPropertyWithValue("boxedShortValue", (short) 100)
                .hasFieldOrPropertyWithValue("boxedByteValue", (byte) 100)
                .hasFieldOrPropertyWithValue("boxedDoubleValue", (double) 100)
                .hasFieldOrPropertyWithValue("boxedFloatValue", (float) 100)
                .hasFieldOrPropertyWithValue("boxedBooleanValue", true)
                .hasFieldOrPropertyWithValue("bigIntegerValue", new BigInteger("100"))
                .hasFieldOrPropertyWithValue("bigDecimalValue", new BigDecimal("100"))
        ;
    }
}
