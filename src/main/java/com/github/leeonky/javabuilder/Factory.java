package com.github.leeonky.javabuilder;

import com.github.leeonky.util.BeanClass;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

public interface Factory<T> {
    BeanClass<T> getBeanClass();

    int getSequence();

    T createObject(BuildContext<T> buildContext);

    Factory<T> extend(String name, BiConsumer<T, BuildContext<T>> consumer);

    default Factory<T> extend(String name, Consumer<T> consumer) {
        return extend(name, (o, buildContext) -> consumer.accept(o));
    }

    Factory<T> query(String extend);

    default Factory<T> getRoot() {
        return this;
    }
}
