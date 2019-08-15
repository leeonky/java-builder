package com.github.leeonky.javabuilder;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.*;
import java.util.Date;
import java.util.UUID;

@Getter
@Setter
@Accessors(chain = true)
public class Bean {
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
