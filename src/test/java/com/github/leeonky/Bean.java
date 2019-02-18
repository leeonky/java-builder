package com.github.leeonky;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Getter
@Setter
@Accessors(chain = true)
class Bean {
    private String stringValue;
    private long longValue;
    private int intValue;
    private Bean beanValue;

    static class SubBean extends Bean {

    }
}
