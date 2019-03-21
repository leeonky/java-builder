package com.github.leeonky;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Date;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.github.leeonky.Bean.Enums.A;
import static com.github.leeonky.Bean.Enums.B;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class FactorySetTest {
    private final FactorySet factorySet = new FactorySet();

    @Test
    void register_and_build() {
        factorySet.onBuild(Bean.class, bean -> bean.setStringValue("Hello"));

        assertThat(factorySet.type(Bean.class).build())
                .hasFieldOrPropertyWithValue("stringValue", "Hello");
    }

    @Test
    void should_raise_error_when_no_default_constructor() {
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> factorySet.onBuild(BeanWithNoDefaultConstructor.class, bean -> {
        }));

        assertThat(exception.getMessage()).isEqualTo("No default constructor of class: " + BeanWithNoDefaultConstructor.class.getName());
    }

    @Test
    void should_re_raise_error_when_got_exception_in_constructor() {
        factorySet.onBuild(BeanWithExceptionConstructor.class, bean -> {
        });

        assertThrows(IllegalStateException.class, () -> factorySet.type(BeanWithExceptionConstructor.class).build());
    }

    @Test
    void register_with_sequence() {
        factorySet.onBuild(Bean.class, (bean, seq) -> bean.setStringValue("Hello" + seq));
        Builder<Bean> builder = factorySet.type(Bean.class);

        assertThat(builder.build().getStringValue()).isEqualTo("Hello1");
        assertThat(builder.build().getStringValue()).isEqualTo("Hello2");
    }

    @Test
    void register_with_sequence_and_params() {
        factorySet.onBuild(Bean.class, (bean, seq, params) -> bean.setStringValue("Hello " + params.get("message")));

        assertThat(factorySet.type(Bean.class).params(new HashMap<String, Object>() {{
            put("message", "world");
        }}).build().getStringValue()).isEqualTo("Hello world");
    }

    @Test
    void build_object_list() {
        factorySet.onBuild(Bean.class, (bean, seq) -> bean.setStringValue("Hello" + seq));

        assertThat(factorySet.type(Bean.class).build(2).map(Bean::getStringValue).collect(Collectors.toList()))
                .isEqualTo(asList("Hello1", "Hello2"));
    }

    @Test
    void build_with_property() {
        factorySet.onBuild(Bean.class, bean -> {
        });

        assertThat(factorySet.type(Bean.class).properties(new HashMap<String, Object>() {{
            put("stringValue", "Hello");
        }}).build()).hasFieldOrPropertyWithValue("stringValue", "Hello");
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

    @Test
    void register_and_build_with_no_default_constructor_and_sequence() {
        factorySet.register(BeanWithNoDefaultConstructor.class, BeanWithNoDefaultConstructor::new);

        assertThat(factorySet.type(BeanWithNoDefaultConstructor.class).build())
                .hasFieldOrPropertyWithValue("intValue", 1);
        assertThat(factorySet.type(BeanWithNoDefaultConstructor.class).build())
                .hasFieldOrPropertyWithValue("intValue", 2);
    }

    @Test
    void register_and_build_with_no_default_constructor_and_sequence_and_params() {
        factorySet.register(BeanWithNoDefaultConstructor.class, (seq, params) -> {
            BeanWithNoDefaultConstructor bean = new BeanWithNoDefaultConstructor(seq);
            bean.setStringValue(params.get("stringValue").toString());
            return bean;
        });
    }

    @Test
    void add_customer_converter() {
        factorySet.onBuild(Bean.class, b -> {
        });

        factorySet.registerConverter(converter -> converter.addTypeConverter(Long.class, int.class, Long::intValue));

        assertThat(factorySet.type(Bean.class).properties(new HashMap<String, Object>() {{
            put("intValue", 1L);
        }}).build().getIntValue()).isEqualTo(1);
    }

    @Test
    void extend_factory() {
        factorySet.onBuild(Bean.class, b -> b.setStringValue("Hello"))
                .extend("extend",
                        b -> b.setStringValue(b.getStringValue() + " world"))
                .extend("extend.extend2",
                        (b, i) -> b.setStringValue(b.getStringValue() + " again " + i))
                .extend("extend.extend2.extend3",
                        (b, i, p) -> b.setStringValue(b.getStringValue() + " and again " + p.get("name")));

        assertThat(factorySet.type(Bean.class, "extend").build().getStringValue()).isEqualTo("Hello world");
        assertThat(factorySet.type(Bean.class, "extend.extend2").build().getStringValue()).isEqualTo("Hello world again 2");
        assertThat(factorySet.type(Bean.class, "extend.extend2.extend3").params(new HashMap<String, Object>() {{
            put("name", "tom");
        }}).build().getStringValue()).isEqualTo("Hello world again 3 and again tom");
    }

    @Test
    void extend_from_non_exist_factory() {
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            factorySet.onBuild(Bean.class, b -> b.setStringValue("Hello"))
                    .extend("extend.non-exist",
                            b -> b.setStringValue(b.getStringValue() + " world"));
        });
        assertThat(exception).hasMessage("Factory[extend] for com.github.leeonky.Bean dose not exist");
    }

    @Test
    void build_from_non_exist_factory() {
        factorySet.onBuild(Bean.class, b -> {
        }).extend("a", o -> {
        });

        RuntimeException exception = assertThrows(RuntimeException.class, () -> factorySet.type(Bean.class, "extend"));
        assertThat(exception).hasMessage("Factory[extend] for com.github.leeonky.Bean dose not exist");

        exception = assertThrows(RuntimeException.class, () -> factorySet.type(Bean.class, "a.b"));
        assertThat(exception).hasMessage("Factory[a.b] for com.github.leeonky.Bean dose not exist");

        exception = assertThrows(RuntimeException.class, () -> factorySet.type(Bean.class, "extend.extend2"));
        assertThat(exception).hasMessage("Factory[extend.extend2] for com.github.leeonky.Bean dose not exist");
    }

    @Test
    void default_build() {
        assertThat(factorySet.type(Bean.class).build())
                .hasFieldOrPropertyWithValue("stringValue", "stringValue1")
                .hasFieldOrPropertyWithValue("longValue", 1L)
                .hasFieldOrPropertyWithValue("intValue", 1)
                .hasFieldOrPropertyWithValue("shortValue", (short) 1)
                .hasFieldOrPropertyWithValue("byteValue", (byte) 1)
                .hasFieldOrPropertyWithValue("doubleValue", (double) 1)
                .hasFieldOrPropertyWithValue("floatValue", (float) 1)
                .hasFieldOrPropertyWithValue("booleanValue", true)
                .hasFieldOrPropertyWithValue("boxedLongValue", 1L)
                .hasFieldOrPropertyWithValue("boxedIntValue", 1)
                .hasFieldOrPropertyWithValue("boxedShortValue", (short) 1)
                .hasFieldOrPropertyWithValue("boxedByteValue", (byte) 1)
                .hasFieldOrPropertyWithValue("boxedDoubleValue", (double) 1)
                .hasFieldOrPropertyWithValue("boxedFloatValue", (float) 1)
                .hasFieldOrPropertyWithValue("boxedBooleanValue", true)
                .hasFieldOrPropertyWithValue("bigIntegerValue", new BigInteger("1"))
                .hasFieldOrPropertyWithValue("bigDecimalValue", new BigDecimal("1"))
                .hasFieldOrPropertyWithValue("uuidValue", UUID.fromString("00000000-0000-0000-0000-000000000001"))
                .hasFieldOrPropertyWithValue("instantValue", Instant.parse("1996-01-23T00:00:01Z"))
                .hasFieldOrPropertyWithValue("dateValue", Date.from(Instant.parse("1996-01-23T00:00:00Z")))
                .hasFieldOrPropertyWithValue("localTimeValue", LocalTime.parse("00:00:01"))
                .hasFieldOrPropertyWithValue("localDateValue", LocalDate.parse("1996-01-23"))
                .hasFieldOrPropertyWithValue("localDateTimeValue", LocalDateTime.parse("1996-01-23T00:00:01"))
                .hasFieldOrPropertyWithValue("enumValue", A);

        assertThat(factorySet.type(Bean.class).build())
                .hasFieldOrPropertyWithValue("stringValue", "stringValue2")
                .hasFieldOrPropertyWithValue("longValue", 2L)
                .hasFieldOrPropertyWithValue("intValue", 2)
                .hasFieldOrPropertyWithValue("shortValue", (short) 2)
                .hasFieldOrPropertyWithValue("byteValue", (byte) 2)
                .hasFieldOrPropertyWithValue("doubleValue", (double) 2)
                .hasFieldOrPropertyWithValue("floatValue", (float) 2)
                .hasFieldOrPropertyWithValue("booleanValue", false)
                .hasFieldOrPropertyWithValue("boxedLongValue", 2L)
                .hasFieldOrPropertyWithValue("boxedIntValue", 2)
                .hasFieldOrPropertyWithValue("boxedShortValue", (short) 2)
                .hasFieldOrPropertyWithValue("boxedByteValue", (byte) 2)
                .hasFieldOrPropertyWithValue("boxedDoubleValue", (double) 2)
                .hasFieldOrPropertyWithValue("boxedFloatValue", (float) 2)
                .hasFieldOrPropertyWithValue("boxedBooleanValue", false)
                .hasFieldOrPropertyWithValue("bigIntegerValue", new BigInteger("2"))
                .hasFieldOrPropertyWithValue("bigDecimalValue", new BigDecimal("2"))
                .hasFieldOrPropertyWithValue("uuidValue", UUID.fromString("00000000-0000-0000-0000-000000000002"))
                .hasFieldOrPropertyWithValue("instantValue", Instant.parse("1996-01-23T00:00:02Z"))
                .hasFieldOrPropertyWithValue("dateValue", Date.from(Instant.parse("1996-01-24T00:00:00Z")))
                .hasFieldOrPropertyWithValue("localTimeValue", LocalTime.parse("00:00:02"))
                .hasFieldOrPropertyWithValue("localDateValue", LocalDate.parse("1996-01-24"))
                .hasFieldOrPropertyWithValue("localDateTimeValue", LocalDateTime.parse("1996-01-23T00:00:02"))
                .hasFieldOrPropertyWithValue("enumValue", B);
    }
}