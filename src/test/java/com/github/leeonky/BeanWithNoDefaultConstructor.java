package com.github.leeonky;

class BeanWithNoDefaultConstructor {
    private final int intValue;
    private String stringValue;

    BeanWithNoDefaultConstructor(int intValue) {
        this.intValue = intValue;
    }

    public String getStringValue() {
        return stringValue;
    }

    public void setStringValue(String stringValue) {
        this.stringValue = stringValue;
    }

    public int getIntValue() {
        return intValue;
    }
}
