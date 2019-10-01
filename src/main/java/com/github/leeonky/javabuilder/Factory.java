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

    Factory<T> registerAlias();

    Factory<T> registerAlias(String alias);

    Factory<T> canCombine(String name, BiConsumer<T, BuildContext<T>> combination);

    default Factory<T> canCombine(String name, Consumer<T> combination) {
        return canCombine(name, (o, buildContext) -> combination.accept(o));
    }

    void combineBuild(T object, String name, BuildContext<T> buildContext);
}
