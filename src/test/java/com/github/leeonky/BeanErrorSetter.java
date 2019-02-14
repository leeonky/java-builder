package com.github.leeonky;

class BeanErrorSetter {
    private String value;

    public void setValue(String v) {
        throw new RuntimeException("Hello");
    }
}
