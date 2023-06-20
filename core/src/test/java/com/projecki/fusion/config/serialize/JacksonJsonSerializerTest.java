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
public class JacksonJsonSerializerTest {

    private final JacksonSerializer<TestRecord> serializer = JacksonSerializer.ofJson(TestRecord.class);

    @ParameterizedTest
    @MethodSource("provideJacksonJsonTest")
    void jacksonJsonSerializeTest(@NotNull TestRecord original, @NotNull String expected) {
        Assertions.assertEquals(expected, serializer.serialize(original), "Failed to serialise object properly");
    }

    @ParameterizedTest
    @MethodSource("provideJacksonJsonTest")
    void jacksonJsonDeserializeTest(@NotNull TestRecord expected, @NotNull String original) {
        Assertions.assertEquals(expected, serializer.deserialize(original).orElse(null), "Failed to serialise object properly");
    }

    private Stream<Arguments> provideJacksonJsonTest() {
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
                        "{\"name\":\"test\",\"value\":42,\"subValue\":{\"name\":\"sub test\",\"second\":12.34,\"values\":[\"test1\",\"test2\",\"test3\"]},\"values\":[{\"names\":[\"value2\",\"vass\",\"test\"],\"number\":10},{\"names\":[\"1\",\"2\",\"3\"],\"number\":123},{\"names\":[\"-34\",\"\",\"3244\"],\"number\":-234}]}"),
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
                        "{\"name\":\"second_test\",\"value\":123333,\"subValue\":{\"name\":\"new subber\",\"second\":-23444.33234,\"values\":[\"number: 3\",\"number: -123\",\"234\"]},\"values\":[]}")
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
