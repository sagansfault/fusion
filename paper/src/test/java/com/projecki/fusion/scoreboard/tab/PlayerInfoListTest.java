package com.projecki.fusion.scoreboard.tab;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Andavin
 * @since May 31, 2022
 */
public class PlayerInfoListTest {

    @Test
    public void testOverlap() {
        PlayerInfoList first = PlayerInfoList.of(0, 0, 10);
        PlayerInfoList second = PlayerInfoList.of(0, 5, 10);
        assertEquals(0, first.compareTo(second));
    }

    @Test
    public void testNoOverlap() {
        PlayerInfoList first = PlayerInfoList.of(0, 0, 10);
        PlayerInfoList second = PlayerInfoList.of(0, 10, 10);
        assertEquals(-10, first.compareTo(second));
    }
}
