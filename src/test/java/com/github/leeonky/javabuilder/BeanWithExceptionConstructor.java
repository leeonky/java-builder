package com.github.leeonky.javabuilder;

class BeanWithExceptionConstructor {
    BeanWithExceptionConstructor() throws Exception {
        throw new Exception("");
    }
}
