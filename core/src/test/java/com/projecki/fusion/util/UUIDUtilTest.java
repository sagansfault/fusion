package com.projecki.fusion.util;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class UUIDUtilTest {

    @Test
    public void testUUIDToBytes() {
        UUID uuid = UUID.randomUUID();
        byte[] bytes = UUIDUtil.toBytes(uuid);
        assertEquals(uuid, UUIDUtil.toUuid(bytes));
    }

    @Test
    public void createUUIDTest() {
        var uuid = UUIDUtil.createUUID("9f777b02900d455e8b8d4fc742976deb").orElse(null);

        // Make sure uuid gets created properly
        assertNotNull(uuid, "Failed to create UUID!");

        // Make sure it matches the right value
        assertEquals(uuid, UUID.fromString("9f777b02-900d-455e-8b8d-4fc742976deb"), "Created UUID doesn't match expected value!");
    }
}
