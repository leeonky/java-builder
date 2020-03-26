package com.github.leeonky.javabuilder.spec;

import com.github.leeonky.util.BeanClass;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class QueryExpressionTest {

    static class Bean {
        public String value;
    }

    static class BeanContainer {
        public Bean bean1, bean2;
    }

    static class AnotherBeanContainer {
        public Bean bean1, bean2;
    }

    @Nested
    class SameWith {

        @Test
        void should_compare_type_spec_combination_condition_and_value() {
            assertTrue(new QueryExpression<>(BeanClass.create(BeanContainer.class), "bean1(A Bean).value", "a")
                    .sameWith(new QueryExpression<>(BeanClass.create(BeanContainer.class), "bean2(A Bean).value", "a")));

            assertFalse(new QueryExpression<>(BeanClass.create(BeanContainer.class), "bean1(A Bean).value", "a")
                    .sameWith(new QueryExpression<>(BeanClass.create(BeanContainer.class), "bean2(B Bean).value", "a")));

            assertFalse(new QueryExpression<>(BeanClass.create(BeanContainer.class), "bean1(A Bean).value", "a")
                    .sameWith(new QueryExpression<>(BeanClass.create(BeanContainer.class), "bean2(A Bean2).value", "a")));

            assertFalse(new QueryExpression<>(BeanClass.create(BeanContainer.class), "bean1(A Bean).value", "a")
                    .sameWith(new QueryExpression<>(BeanClass.create(BeanContainer.class), "bean2(A Bean).value2", "a")));

            assertFalse(new QueryExpression<>(BeanClass.create(BeanContainer.class), "bean1(A Bean).value", "a")
                    .sameWith(new QueryExpression<>(BeanClass.create(BeanContainer.class), "bean2(A Bean).value", "b")));

            assertFalse(new QueryExpression<>(BeanClass.create(BeanContainer.class), "bean1(A Bean).value", "a")
                    .sameWith(new QueryExpression<>(BeanClass.create(AnotherBeanContainer.class), "bean2(A Bean).value", "a")));
        }

        @Test
        void should_skip_combination_and_spec_when_second_is_default_build() {
            assertTrue(new QueryExpression<>(BeanClass.create(BeanContainer.class), "bean1(A Bean).value", "a")
                    .sameWith(new QueryExpression<>(BeanClass.create(BeanContainer.class), "bean2.value", "a")));
        }
    }
}