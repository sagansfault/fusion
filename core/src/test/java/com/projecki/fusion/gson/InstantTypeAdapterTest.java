package com.projecki.fusion.gson;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.time.Instant;
import java.util.stream.Stream;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class InstantTypeAdapterTest {

    private final Gson gson = new GsonBuilder()
            .registerTypeAdapter(Instant.class, new IsoFormatInstantTypeAdapter())
            .create();

    @ParameterizedTest
    @MethodSource("provideSerialiseTests")
    void serialiseTest(@NotNull Instant instant, @NotNull String expected) {
        Assertions.assertEquals(expected, gson.toJson(instant));
    }

    private static Stream<Arguments> provideSerialiseTests() {
        Instant now = Instant.now();
        return Stream.of(
                Arguments.of(now, "\"" + now.toString() + "\""),
                Arguments.of(Instant.ofEpochMilli(1650441328585L), "\"2022-04-20T07:55:28.585Z\""),
                Arguments.of(Instant.ofEpochMilli(1650441354250L), "\"2022-04-20T07:55:54.250Z\""),
                Arguments.of(Instant.ofEpochMilli(0L), "\"1970-01-01T00:00:00Z\"")
        );
    }

    @ParameterizedTest
    @MethodSource("provideDeserialiseTests")
    void deserialiseTest (@NotNull String json, @NotNull Instant expected) {
        Assertions.assertEquals(expected, gson.fromJson(json, Instant.class));
    }

    private static Stream<Arguments> provideDeserialiseTests () {
        Instant now = Instant.now();
        return Stream.of(
                Arguments.of("\"" + now.toString() + "\"", now),
                Arguments.of("\"2022-04-20T07:55:28.585Z\"", Instant.ofEpochMilli(1650441328585L)),
                Arguments.of("\"2022-04-20T07:55:54.250Z\"", Instant.ofEpochMilli(1650441354250L)),
                Arguments.of("\"1970-01-01T00:00:00Z\"", Instant.ofEpochMilli(0L))
        );
    }
}
