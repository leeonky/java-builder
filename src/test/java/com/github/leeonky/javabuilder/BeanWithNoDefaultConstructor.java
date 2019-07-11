package com.github.leeonky.javabuilder;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Getter
@Setter
@Accessors(chain = true)
public class BeanWithNoDefaultConstructor {
    private final int intValue;
    private String stringValue;

    BeanWithNoDefaultConstructor(int intValue) {
        this.intValue = intValue;
    }
}
