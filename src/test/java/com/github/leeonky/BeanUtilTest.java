package com.github.leeonky;

import org.junit.jupiter.api.Test;

import java.util.HashMap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class BeanUtilTest {

    @Test
    void should_raise_error_when_got_exception_in_setter() {
        IllegalStateException illegalStateException = assertThrows(IllegalStateException.class,
                () -> new BeanUtil<>(new BeanErrorSetter()).assignProperties(new HashMap<String, Object>() {{
                    put("value", "Hello");
                }}));
        assertThat(illegalStateException).hasMessage("Got exception in 'com.github.leeonky.BeanErrorSetter::setValue'");
    }

    @Test
    void should_raise_error_when_no_setter() {
        IllegalStateException illegalStateException = assertThrows(IllegalStateException.class,
                () -> new BeanUtil<>(new Bean()).assignProperties(new HashMap<String, Object>() {{
                    put("value", "Hello");
                }}));
        assertThat(illegalStateException).hasMessage("No setter was found in 'com.github.leeonky.Bean' for property 'value'");
    }
}