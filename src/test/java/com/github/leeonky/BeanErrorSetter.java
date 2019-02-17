package com.github.leeonky;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Getter
@Setter
@Accessors(chain = true)
class BeanErrorSetter {
    private String value;

    public void setValue(String v) {
        throw new RuntimeException("Hello");
    }
}
