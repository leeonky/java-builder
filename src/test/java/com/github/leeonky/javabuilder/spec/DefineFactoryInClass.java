package com.github.leeonky.javabuilder.spec;

import com.github.leeonky.javabuilder.Bean;
import com.github.leeonky.javabuilder.BuildContext;
import com.github.leeonky.javabuilder.FactoryDefinition;
import com.github.leeonky.javabuilder.FactorySet;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class DefineFactoryInClass {
    FactorySet factorySet = new FactorySet();

    @Test
    void should_support_define_factory_in_class() {
        factorySet.onBuild(new BeanFactory());

        Assertions.assertThat(factorySet.toBuild(BeanFactory.class).build())
                .hasFieldOrPropertyWithValue("intValue", 100);
    }

    @Test
    void should_raise_error_when_no_factory_definition() {
        RuntimeException exception = assertThrows(RuntimeException.class, () -> factorySet.toBuild(BeanFactory.class));

        assertThat(exception).hasMessage("FactoryDefinition 'com.github.leeonky.javabuilder.spec.DefineFactoryInClass$BeanFactory' does not exist");
    }

    @Test
    void should_raise_error_when_generic_type_no_specified() {
        RuntimeException exception = assertThrows(RuntimeException.class, () -> factorySet.onBuild(new InvalidFactoryDefinition<Bean>()));

        assertThat(exception).hasMessage("Invalid FactoryDefinition 'com.github.leeonky.javabuilder.spec.DefineFactoryInClass$InvalidFactoryDefinition' should specify generic type or override getType() method");
    }

    @Test
    void support_alias() {
        factorySet.onBuild(new BeanFactory()).registerAlias();

        assertThat(factorySet.toBuild("BeanFactory").build())
                .hasFieldOrPropertyWithValue("intValue", 100);
    }

    @Test
    void support_define_combination_in_definition_class() {
        factorySet.onBuild(new BeanFactory());

        assertThat(factorySet.toBuild(BeanFactory.class).combine("combine1").build())
                .hasFieldOrPropertyWithValue("intValue", 100)
                .hasFieldOrPropertyWithValue("stringValue", "cob1");
    }

    public static class BeanFactory extends FactoryDefinition<Bean> {

        @Override
        public void onBuild(Bean object, BuildContext<Bean> beanBuildContext) {
            object.setIntValue(100);
        }

        public void combine1(Bean object, BuildContext<Bean> beanBuildContext) {
            object.setStringValue("cob1");
        }
    }

    public static class InvalidFactoryDefinition<T> extends FactoryDefinition<T> {
    }
}

