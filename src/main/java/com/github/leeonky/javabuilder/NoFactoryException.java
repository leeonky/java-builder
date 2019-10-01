package com.github.leeonky.javabuilder;

public class NoFactoryException extends RuntimeException {
    NoFactoryException(String extend, Class<?> type) {
        super("Factory[" + extend + "] for " + type.getName() + " dose not exist");
    }
}
