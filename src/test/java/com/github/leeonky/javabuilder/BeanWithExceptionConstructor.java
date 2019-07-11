package com.github.leeonky.javabuilder;

public class BeanWithExceptionConstructor {
    BeanWithExceptionConstructor() throws Exception {
        throw new Exception("");
    }
}
