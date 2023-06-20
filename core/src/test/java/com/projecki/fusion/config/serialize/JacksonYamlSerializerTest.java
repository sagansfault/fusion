package com.projecki.fusion.config.serialize;

import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class JacksonYamlSerializerTest {

    private final JacksonSerializer<TestRecord> serializer = JacksonSerializer.ofYaml(TestRecord.class);

    @ParameterizedTest
    @MethodSource("provideJacksonYmlTest")
    void jacksonYmlSerializeTest(@NotNull TestRecord original, @NotNull String expected) {
        Assertions.assertEquals(expected, serializer.serialize(original), "Failed to serialise object properly");
    }

    @ParameterizedTest
    @MethodSource("provideJacksonYmlTest")
    void jackYmlDeserializeTest(@NotNull TestRecord expected, @NotNull String original) {
        Assertions.assertEquals(expected, serializer.deserialize(original).orElse(null), "Failed to serialise object properly");
    }

    private Stream<Arguments> provideJacksonYmlTest() {
        return Stream.of(
                Arguments.of(
                        new TestRecord(
                                "test",
                                42,
                                new TestRecord.SubRecord(
                                        "sub test",
                                        12.34,
                                        List.of("test1", "test2", "test3"
                                        )
                                ),
                                List.of(
                                        new TestRecord.SubValue(new String[]{"value2", "vass", "test"}, 10),
                                        new TestRecord.SubValue(new String[]{"1", "2", "3"}, 123),
                                        new TestRecord.SubValue(new String[]{"-34", "", "3244"}, -234)
                                )
                        ),
                        "---\nname: \"test\"\nvalue: 42\nsubValue:\n  name: \"sub test\"\n  second: 12.34\n  values:\n  - \"test1\"\n  - \"test2\"\n  - \"test3\"\nvalues:\n- names:\n  - \"value2\"\n  - \"vass\"\n  - \"test\"\n  number: 10\n- names:\n  - \"1\"\n  - \"2\"\n  - \"3\"\n  number: 123\n- names:\n  - \"-34\"\n  - \"\"\n  - \"3244\"\n  number: -234\n"),
                Arguments.of(
                        new TestRecord(
                                "second_test",
                                123333,
                                new TestRecord.SubRecord(
                                        "new subber",
                                        -23444.33234,
                                        List.of("number: 3", "number: -123", "234")
                                ),
                                List.of()
                        ),
                        "---\nname: \"second_test\"\nvalue: 123333\nsubValue:\n  name: \"new subber\"\n  second: -23444.33234\n  values:\n  - \"number: 3\"\n  - \"number: -123\"\n  - \"234\"\nvalues: []\n")
        );
    }

    // complex record object for testing the serializer
    private record TestRecord(@NotNull String name, int value, @NotNull SubRecord subValue, List<SubValue> values) {

        static class SubValue {

            private String[] names;
            private int number;

            public SubValue() {

            }

            public SubValue(String[] names, int number) {
                this.names = names;
                this.number = number;
            }

            @Override
            public boolean equals(Object o) {
                if (this == o) return true;
                if (o == null || getClass() != o.getClass()) return false;
                SubValue subValue = (SubValue) o;
                return number == subValue.number && Arrays.equals(names, subValue.names);
            }

            @Override
            public int hashCode() {
                int result = Objects.hash(number);
                result = 31 * result + Arrays.hashCode(names);
                return result;
            }
        }

        private record SubRecord(@NotNull String name, double second, List<String> values) {
        }

    }

}
