package com.github.leeonky.javabuilder;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Date;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.github.leeonky.javabuilder.Bean.Enums.A;
import static com.github.leeonky.javabuilder.Bean.Enums.B;
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
    void support_simple_property_build() {
        assertThat(factorySet.type(Bean.class).property("stringValue", "Hello").build())
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
        factorySet.onBuild(Bean.class, (bean, buildContext) -> bean.setStringValue("Hello" + buildContext.getSequence()));
        Builder<Bean> builder = factorySet.type(Bean.class);

        assertThat(builder.build().getStringValue()).isEqualTo("Hello1");
        assertThat(builder.build().getStringValue()).isEqualTo("Hello2");
    }

    @Test
    void register_with_sequence_and_params() {
        factorySet.onBuild(Bean.class, (bean, builderContext) -> bean.setStringValue("Hello " + builderContext.getParams().get("message")));

        assertThat(factorySet.type(Bean.class).params(new HashMap<String, Object>() {{
            put("message", "world");
        }}).build().getStringValue()).isEqualTo("Hello world");
    }

    @Test
    void build_object_list() {
        factorySet.onBuild(Bean.class, (bean, buildContext) -> bean.setStringValue("Hello" + buildContext.getSequence()));

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
    void register_and_build_with_no_default_constructor_and_sequence_and_params() {
        factorySet.register(BeanWithNoDefaultConstructor.class, (buildContext) -> {
            BeanWithNoDefaultConstructor bean = new BeanWithNoDefaultConstructor(buildContext.getSequence());
            bean.setStringValue(buildContext.getParams().get("stringValue").toString());
            return bean;
        });
    }

    @Test
    void add_customer_converter() {
        factorySet.onBuild(Bean.class, b -> {
        });

        factorySet.getConverter().addTypeConverter(Long.class, int.class, Long::intValue);

        assertThat(factorySet.type(Bean.class).properties(new HashMap<String, Object>() {{
            put("intValue", 1L);
        }}).build().getIntValue()).isEqualTo(1);
    }

    @Test
    void extend_factory() {
        factorySet.onBuild(Bean.class, b -> b.setStringValue("Hello"))
                .extend("extend",
                        b -> b.setStringValue(b.getStringValue() + " world"))
                .extend("extend2",
                        (b, buildContext) -> b.setStringValue(b.getStringValue() + " again " + buildContext.getSequence()))
                .extend("extend3",
                        (b, buildContext) -> b.setStringValue(b.getStringValue() + " and again " + buildContext.getParams().get("name")));

        assertThat(factorySet.type(Bean.class, "extend").build().getStringValue()).isEqualTo("Hello world");
        assertThat(factorySet.type(Bean.class, "extend2").build().getStringValue()).isEqualTo("Hello world again 2");
        assertThat(factorySet.type(Bean.class, "extend3").params(new HashMap<String, Object>() {{
            put("name", "tom");
        }}).build().getStringValue()).isEqualTo("Hello world again 3 and again tom");
    }

    @Test
    void should_raise_error_when_duplicated_extend() {
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            factorySet.factory(Bean.class)
                    .extend("extend", b -> b.setStringValue(b.getStringValue() + " world"))
                    .extend("extend", b -> b.setStringValue(b.getStringValue() + " world"));
        });
        assertThat(exception).hasMessage("Duplicated factory name[extend] for com.github.leeonky.javabuilder.Bean");
    }

    @Test
    void build_from_non_exist_factory() {
        factorySet.onBuild(Bean.class, b -> {
        }).extend("a", o -> {
        });

        RuntimeException exception = assertThrows(RuntimeException.class, () -> factorySet.type(Bean.class, "extend"));
        assertThat(exception).hasMessage("Factory[extend] for com.github.leeonky.javabuilder.Bean dose not exist");

        exception = assertThrows(RuntimeException.class, () -> factorySet.type(Bean.class, "a.b"));
        assertThat(exception).hasMessage("Factory[a.b] for com.github.leeonky.javabuilder.Bean dose not exist");

        exception = assertThrows(RuntimeException.class, () -> factorySet.type(Bean.class, "extend.extend2"));
        assertThat(exception).hasMessage("Factory[extend.extend2] for com.github.leeonky.javabuilder.Bean dose not exist");
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

    @Test
    void default_build_for_special_method() {
        factorySet.getPropertyBuilder()
                .registerFromProperty(m -> m.getName().equals("intValue"),
                        (m, o, buildContext) -> buildContext.getSequence() + 1);

        assertThat(factorySet.type(Bean.class).build())
                .hasFieldOrPropertyWithValue("intValue", 2);

        assertThat(factorySet.type(Bean.class).build())
                .hasFieldOrPropertyWithValue("intValue", 3);
    }

    @Test
    void default_build_for_skip_special_method() {
        factorySet.getPropertyBuilder()
                .skipProperty(m -> m.getName().equals("intValue"));

        assertThat(factorySet.type(Bean.class).build())
                .hasFieldOrPropertyWithValue("intValue", 0);
    }

    @Test
    void type_convert() throws ParseException {
        assertThat(new FactorySet().type(Bean.class).properties(new HashMap<String, Object>() {{
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

            put("uuidValue", "123e4567-e89b-12d3-a456-426655440000");

            put("instantValue", "2001-10-12T12:00:01.123Z");
            put("dateValue", "2001-10-12");
            put("localTimeValue", "00:00:01");
            put("localDateValue", "1996-01-24");
            put("localDateTimeValue", "1996-01-23T00:00:01");
        }}).build())
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
                .hasFieldOrPropertyWithValue("uuidValue", UUID.fromString("123e4567-e89b-12d3-a456-426655440000"))
                .hasFieldOrPropertyWithValue("instantValue", Instant.parse("2001-10-12T12:00:01.123Z"))
                .hasFieldOrPropertyWithValue("dateValue", new SimpleDateFormat("yyyy-MM-dd").parse("2001-10-12"))
                .hasFieldOrPropertyWithValue("localTimeValue", LocalTime.parse("00:00:01"))
                .hasFieldOrPropertyWithValue("localDateValue", LocalDate.parse("1996-01-24"))
                .hasFieldOrPropertyWithValue("localDateTimeValue", LocalDateTime.parse("1996-01-23T00:00:01"))
        ;
    }
}