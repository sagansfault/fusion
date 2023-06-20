package com.projecki.fusion.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ResultTest {

    @Test
    public void resultTest () {
        assertTrue(getOkayResult().isOkay(), "Okay result didn't return ok!");
        assertEquals("okay!", getOkayResult().unwrap(), "Failed to unwrap result");
        assertTrue(getErrorResult().isError(), "Error result didn't return as error!");
    }

    private Result<String, String> getOkayResult() {
        return Result.ok("okay!");
    }

    private Result<String, String> getErrorResult () {
        return Result.error("error!");
    }

}
