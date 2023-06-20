package com.projecki.fusion.util;

import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class MojangAPITest {

    private final ExecutorService httpExecutor = new ThreadPoolExecutor(1, 1, 30, TimeUnit.SECONDS, new LinkedBlockingQueue<>());
    private final MojangAPI mojangAPI = new MojangAPI(httpExecutor);

    @ParameterizedTest
    @MethodSource("provideUsernamesForUniqueIdTest")
    void getUniqueIdTest (@NotNull String username, @NotNull UUID expected) throws ExecutionException, InterruptedException {
        Assertions.assertEquals(expected, mojangAPI.getUniqueId(username).get().orElse(null));
    }

    private static Stream<Arguments> provideUsernamesForUniqueIdTest () {
        return Stream.of(
                Arguments.of("oxkitsune", UUID.fromString("9f777b02-900d-455e-8b8d-4fc742976deb")),
                Arguments.of("blamekitsune", UUID.fromString("deaa9b2d-fd5d-4e01-8d21-617ac7c66de1")),
                Arguments.of("Teamplayer", UUID.fromString("1aa244b0-0d8d-4e92-9679-c5657929a2fc")),
                Arguments.of("sagansfault", UUID.fromString("537e2a3e-8dcb-4c87-bdf9-88d2fbac5d8a")),
                Arguments.of("Projecki", UUID.fromString("8f238d83-cf01-461e-aa49-086d7fb83e7d"))
        );
    }

    @ParameterizedTest
    @MethodSource("provideUniqueIdForUsernameTest")
    void getUsernameTest (@NotNull UUID uniqueId, @NotNull String expected) throws ExecutionException, InterruptedException {
        Assertions.assertEquals(expected, mojangAPI.getUsername(uniqueId).get().orElse(null));
    }

    private static Stream<Arguments> provideUniqueIdForUsernameTest () {
        return Stream.of(
                Arguments.of(UUID.fromString("9f777b02-900d-455e-8b8d-4fc742976deb"), "oxkitsune"),
                Arguments.of(UUID.fromString("deaa9b2d-fd5d-4e01-8d21-617ac7c66de1"), "blamekitsune"),
                Arguments.of(UUID.fromString("1aa244b0-0d8d-4e92-9679-c5657929a2fc"), "Teamplayer"),
                Arguments.of(UUID.fromString("537e2a3e-8dcb-4c87-bdf9-88d2fbac5d8a"), "sagansfault"),
                Arguments.of(UUID.fromString("8f238d83-cf01-461e-aa49-086d7fb83e7d"), "Projecki")
        );
    }

    @AfterAll
    void cleanup() {
        httpExecutor.shutdown();
    }
}
