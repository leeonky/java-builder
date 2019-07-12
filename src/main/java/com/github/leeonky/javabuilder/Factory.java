package com.github.leeonky.javabuilder;

import com.github.leeonky.util.BeanClass;

import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public interface Factory<T> {
    BeanClass<T> getBeanClass();

    int getSequence();

    T createObject(int sequence, Map<String, ?> params);

    Factory<T> extend(String name, TriConsumer<T, Integer, Map<String, ?>> consumer);

    Factory<T> query(String extend);

    default Factory<T> extend(String name, BiConsumer<T, Integer> consumer) {
        return extend(name, (o, i, p) -> consumer.accept(o, i));
    }

    default Factory<T> extend(String name, Consumer<T> consumer) {
        return extend(name, (o, i, p) -> consumer.accept(o));
    }

    default Factory<T> getRoot() {
        return this;
    }
}
