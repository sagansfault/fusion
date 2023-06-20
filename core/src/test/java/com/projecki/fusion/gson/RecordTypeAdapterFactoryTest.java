package com.projecki.fusion.gson;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class RecordTypeAdapterFactoryTest {

    private final Gson gson = new GsonBuilder()
            .registerTypeAdapterFactory(new RecordTypeAdapterFactory())
            .create();

    record TestRecord(@NotNull String name, @NotNull String value) {
    }

    @ParameterizedTest
    @MethodSource("provideSerialiseTests")
    void serialiseTest(@NotNull TestRecord record, @NotNull String expected) {
        Assertions.assertEquals(expected, gson.toJson(record));
    }

    private static Stream<Arguments> provideSerialiseTests() {
        return Stream.of(
                Arguments.of(new TestRecord("test1", "apples"), "{\"name\":\"test1\",\"value\":\"apples\"}"),
                Arguments.of(new TestRecord("test2", "cakes"), "{\"name\":\"test2\",\"value\":\"cakes\"}"),
                Arguments.of(new TestRecord("NAME", "VALUE"), "{\"name\":\"NAME\",\"value\":\"VALUE\"}")
        );
    }

    @ParameterizedTest
    @MethodSource("provideDeserialiseTests")
    void deserialiseTest(@NotNull String json, @NotNull TestRecord expected) {
        Assertions.assertEquals(expected, gson.fromJson(json, TestRecord.class));
    }

    private static Stream<Arguments> provideDeserialiseTests() {
        return Stream.of(
                Arguments.of("{\"name\":\"test1\",\"value\":\"apples\"}", new TestRecord("test1", "apples")),
                Arguments.of("{\"name\":\"test2\",\"value\":\"cakes\"}", new TestRecord("test2", "cakes")),
                Arguments.of("{\"name\":\"NAME\",\"value\":\"VALUE\"}", new TestRecord("NAME", "VALUE"))
        );
    }

}
