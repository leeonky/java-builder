package com.github.leeonky;

import lombok.Getter;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.assertj.core.api.Java6Assertions.assertThat;

class ConverterTest {
    private Converter converter = new Converter();

    enum NameEnums {
        E1, E2
    }

    enum ValueEnums implements ValueEnum<Integer> {
        E1(0), E2(1);

        @Getter
        Integer value;

        ValueEnums(int i) {
            value = i;
        }
    }

    interface ValueEnum<V> {
        static <E extends ValueEnum<V>, V> E fromValue(Class<E> type, V value) {
            return Arrays.stream(type.getEnumConstants()).filter((v) -> v.getValue().equals(value))
                    .findFirst().orElseThrow(() -> new IllegalArgumentException("Unsupported enum value '" + value + "'"));
        }

        V getValue();
    }

    @Nested
    class TypeConverter {
        @Test
        void no_candidate_converter_should_return_original_value() {
            converter.addTypeConverter(Long.class, Bean.class, s -> null);

            assertThat(converter.tryConvert(Bean.class, "String")).isEqualTo("String");
        }

        @Test
        void no_defined_converter_for_target_type_should_return_original_value() {
            assertThat(converter.tryConvert(Bean.class, "String")).isEqualTo("String");
        }

        @Test
        void assign_sub_type_to_base_should_keep_original() {
            Bean.SubBean subBean = new Bean.SubBean();
            converter.addTypeConverter(Bean.SubBean.class, Bean.class, sb -> {
                throw new RuntimeException();
            });

            assertThat(converter.tryConvert(Bean.class, subBean)).isEqualTo(subBean);
        }

        @Test
        void candidate_converter() {
            converter.addTypeConverter(String.class, Integer.class, Integer::valueOf);

            assertThat(converter.tryConvert(Integer.class, "100")).isEqualTo(100);
        }

        @Test
        void candidate_converter_as_base_type() {
            converter.addTypeConverter(Object.class, String.class, o -> "Hello");

            assertThat(converter.tryConvert(String.class, new Bean())).isEqualTo("Hello");
        }
    }

    @Nested
    class EnumConvert {
        @Test
        void covert_to_enum_from_name() {
            assertThat(converter.tryConvert(NameEnums.class, "E2")).isEqualTo(NameEnums.E2);
        }

        @Test
        void convert_to_customer_enum_with_enum_type() {
            converter.addEnumConverter(Integer.class, ValueEnums.class, ValueEnum::fromValue);

            assertThat(converter.tryConvert(ValueEnums.class, 1)).isEqualTo(ValueEnums.E2);
        }

        @Test
        void convert_to_customer_enum_with_enum_type_auto_boxed() {
            converter.addEnumConverter(int.class, ValueEnums.class, ValueEnum::fromValue);

            assertThat(converter.tryConvert(ValueEnums.class, 1)).isEqualTo(ValueEnums.E2);
        }
    }
}
