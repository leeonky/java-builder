package com.github.leeonky.javabuilder;

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
    void should_call_default_factory_builder_first() {
        factorySet.onBuild(Bean.class, b -> b.setStringValue("default"));
        factorySet.onBuild(new BeanFactory());

        assertThat(factorySet.toBuild(BeanFactory.class).build())
                .hasFieldOrPropertyWithValue("stringValue", "default");
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

    @Test
    void should_use_same_sequence_references_for_same_type() {
        factorySet.onBuild(new BeanFactoryWithSeq());
        factorySet.onBuild(new BeanFactoryWithSeq2());

        assertThat(factorySet.toBuild(BeanFactoryWithSeq.class).build())
                .hasFieldOrPropertyWithValue("intValue", 1);

        assertThat(factorySet.toBuild(BeanFactoryWithSeq2.class).build())
                .hasFieldOrPropertyWithValue("intValue", 2);

        assertThat(factorySet.type(Bean.class).build())
                .hasFieldOrPropertyWithValue("intValue", 3);
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

    public static class BeanFactoryWithSeq extends FactoryDefinition<Bean> {

        @Override
        public void onBuild(Bean object, BuildContext<Bean> beanBuildContext) {
            object.setIntValue(beanBuildContext.getSequence());
        }
    }

    public static class BeanFactoryWithSeq2 extends FactoryDefinition<Bean> {

        @Override
        public void onBuild(Bean object, BuildContext<Bean> beanBuildContext) {
            object.setIntValue(beanBuildContext.getSequence());
        }
    }
}

