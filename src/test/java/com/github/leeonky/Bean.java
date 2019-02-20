package com.github.leeonky;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.math.BigInteger;

@Getter
@Setter
@Accessors(chain = true)
class Bean {
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

    private Bean beanValue;

    static class SubBean extends Bean {

    }
}
