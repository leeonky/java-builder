package com.github.leeonky;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Getter
@Setter
@Accessors(chain = true)
class BeanWithNoDefaultConstructor {
    private final int intValue;
    private String stringValue;

    BeanWithNoDefaultConstructor(int intValue) {
        this.intValue = intValue;
    }
}
