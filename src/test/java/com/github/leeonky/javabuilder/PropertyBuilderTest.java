package com.github.leeonky.javabuilder;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PropertyBuilderTest {

    @Test
    void rethrow_when_got_exception_in_setter() {
        RuntimeException runtimeException = assertThrows(RuntimeException.class, () ->
                PropertyBuilder.createDefaultPropertyBuilder().apply(1, new BeanErrorSetter()));

        assertThat(runtimeException).hasMessage("Got exception in com.github.leeonky.javabuilder.BeanErrorSetter.setValue");
    }
}