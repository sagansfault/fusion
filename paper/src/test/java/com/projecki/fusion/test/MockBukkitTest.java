package com.projecki.fusion.test;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.ServerMock;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.TestInstance;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public abstract class MockBukkitTest {

    protected ServerMock server;

    @BeforeAll
    public void setup() {
        server = MockBukkit.mock();
    }

    @AfterAll
    public void shutdown () {
        MockBukkit.unmock();
    }
}
