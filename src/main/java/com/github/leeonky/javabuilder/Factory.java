package com.github.leeonky.javabuilder;

import com.github.leeonky.util.BeanClass;

public interface Factory<T> {
    T newInstance(BuildContext<T> buildContext);

    BeanClass<T> getBeanClass();
}
