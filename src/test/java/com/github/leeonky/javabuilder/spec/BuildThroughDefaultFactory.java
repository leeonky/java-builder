package com.github.leeonky.javabuilder.spec;

import com.github.leeonky.javabuilder.FactorySet;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.*;
import java.util.Date;
import java.util.UUID;

import static com.github.leeonky.javabuilder.spec.BuildThroughDefaultFactory.Bean.Enums.A;
import static com.github.leeonky.javabuilder.spec.BuildThroughDefaultFactory.Bean.Enums.B;
import static org.assertj.core.api.Assertions.assertThat;

class BuildThroughDefaultFactory {
    private final FactorySet factorySet = new FactorySet();
    private boolean methodCalled = false;

    @Test
    void default_build() {
        assertThat(factorySet.type(Bean.class).create())
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
                .hasFieldOrPropertyWithValue("dateValue", Date.from(Instant.parse("1996-01-24T00:00:00Z")))
                .hasFieldOrPropertyWithValue("localTimeValue", LocalTime.parse("00:00:01"))
                .hasFieldOrPropertyWithValue("localDateValue", LocalDate.parse("1996-01-24"))
                .hasFieldOrPropertyWithValue("localDateTimeValue", LocalDateTime.parse("1996-01-23T00:00:01"))
                .hasFieldOrPropertyWithValue("offsetDateTimeValue", Instant.parse("1996-01-23T00:00:01Z").atZone(ZoneId.systemDefault()).toOffsetDateTime())
                .hasFieldOrPropertyWithValue("zonedDateTimeValue", Instant.parse("1996-01-23T00:00:01Z").atZone(ZoneId.systemDefault()))
                .hasFieldOrPropertyWithValue("enumValue", A);

        assertThat(factorySet.type(Bean.class).create())
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
                .hasFieldOrPropertyWithValue("dateValue", Date.from(Instant.parse("1996-01-25T00:00:00Z")))
                .hasFieldOrPropertyWithValue("localTimeValue", LocalTime.parse("00:00:02"))
                .hasFieldOrPropertyWithValue("localDateValue", LocalDate.parse("1996-01-25"))
                .hasFieldOrPropertyWithValue("localDateTimeValue", LocalDateTime.parse("1996-01-23T00:00:02"))
                .hasFieldOrPropertyWithValue("offsetDateTimeValue", Instant.parse("1996-01-23T00:00:02Z").atZone(ZoneId.systemDefault()).toOffsetDateTime())
                .hasFieldOrPropertyWithValue("zonedDateTimeValue", Instant.parse("1996-01-23T00:00:02Z").atZone(ZoneId.systemDefault()))
                .hasFieldOrPropertyWithValue("enumValue", B);
    }

    @Test
    void default_build_for_special_method() {
        factorySet.getPropertyBuilder()
                .registerThroughProperty(m -> m.getName().equals("intValue"),
                        (m, o, buildContext) -> buildContext.getCurrentSequence() + 1);

        assertThat(factorySet.type(Bean.class).create())
                .hasFieldOrPropertyWithValue("intValue", 2);

        assertThat(factorySet.type(Bean.class).create())
                .hasFieldOrPropertyWithValue("intValue", 3);
    }

    @Test
    void default_build_for_skip_special_method() {
        factorySet.getPropertyBuilder()
                .skipProperty(m -> m.getName().equals("intValue"));

        assertThat(factorySet.type(Bean.class).create())
                .hasFieldOrPropertyWithValue("intValue", 0);
    }

    @Test
    void should_skip_default_property_build_when_specify_value_in_properties() {
        factorySet.getPropertyBuilder().registerThroughType(Bean.class, (cl, pw, bc) -> {
            methodCalled = true;
            return new Bean();
        });
        Bean newBean = new Bean();

        assertThat(factorySet.type(Bean.class).property("beanValue", newBean).create().getBeanValue())
                .isEqualTo(newBean);
        assertThat(methodCalled).isFalse();
    }

    @Getter
    @Setter
    @Accessors(chain = true)
    public static class Bean {
        private String stringValue;
        private long longValue;
        private int intValue;
        private short shortValue;
        private byte byteValue;
        private double doubleValue;
        private float floatValue;
        private boolean booleanValue;

        private Long boxedLongValue;
        private Integer boxedIntValue;
        private Short boxedShortValue;
        private Byte boxedByteValue;
        private Double boxedDoubleValue;
        private Float boxedFloatValue;
        private Boolean boxedBooleanValue;

        private BigInteger bigIntegerValue;
        private BigDecimal bigDecimalValue;

        private UUID uuidValue;

        private Instant instantValue;
        private Date dateValue;
        private LocalTime localTimeValue;
        private LocalDate localDateValue;
        private LocalDateTime localDateTimeValue;
        private OffsetDateTime offsetDateTimeValue;
        private ZonedDateTime zonedDateTimeValue;

        private Bean beanValue;

        private Enums enumValue;

        public enum Enums {
            A, B
        }

        static class SubBean extends Bean {
        }
    }
}
