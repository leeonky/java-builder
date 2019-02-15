package com.github.leeonky;

class BeanWithExceptionConstructor {
    BeanWithExceptionConstructor() throws Exception {
        throw new Exception("");
    }
}
