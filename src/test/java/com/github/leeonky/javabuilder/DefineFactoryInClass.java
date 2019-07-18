package com.github.leeonky.javabuilder;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class DefineFactoryInClass {
    FactorySet factorySet = new FactorySet();

    @Test
    void should_support_define_factory_in_class() {
        factorySet.onBuild(new BeanFactory());

        assertThat(factorySet.toBuild(BeanFactory.class).build())
                .hasFieldOrPropertyWithValue("intValue", 100);
    }

    @Test
    void should_raise_error_when_no_factory_definition() {
        RuntimeException exception = assertThrows(RuntimeException.class, () -> factorySet.toBuild(BeanFactory.class));
        assertThat(exception).hasMessage("FactoryDefinition 'com.github.leeonky.javabuilder.DefineFactoryInClass$BeanFactory' does not exist");
    }

    @Test
    void should_raise_error_when_generic_type_no_specified() {
        RuntimeException exception = assertThrows(RuntimeException.class, () -> factorySet.onBuild(new InvalidFactoryDefinition<Bean>()));

        assertThat(exception).hasMessage("Invalid FactoryDefinition 'com.github.leeonky.javabuilder.DefineFactoryInClass$InvalidFactoryDefinition' should specify generic type or override getType() method");
    }

    public static class BeanFactory extends FactoryDefinition<Bean> {

        @Override
        public void onBuild(Bean object, BuildContext<Bean> beanBuildContext) {
            object.setIntValue(100);
        }
    }

    public static class InvalidFactoryDefinition<T> extends FactoryDefinition<T> {
    }
}
