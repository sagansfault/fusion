package com.projecki.fusion.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @since March 15, 2022
 * @author Andavin
 */
public class NumberUtilTest {

    @Test
    public void testNumeric() {

        assertFalse(NumberUtil.isNumeric((char) ('0' - 1)));
        assertFalse(NumberUtil.isNumeric((char) ('9' + 1)));
        for (char c = '0'; c <= '9'; c++) {
            assertTrue(NumberUtil.isNumeric(c));
        }
    }

    @Test
    public void testPlace() {
        assertEquals("1st", NumberUtil.placeOf(1));
        assertEquals("13th", NumberUtil.placeOf(13));
        assertEquals("120th", NumberUtil.placeOf(120));
        assertEquals("123rd", NumberUtil.placeOf(123));
    }

    @Test
    public void testNumeral() {
        assertEquals("I", NumberUtil.numeralOf(1));
        assertEquals("IV", NumberUtil.numeralOf(4));
        assertEquals("V", NumberUtil.numeralOf(5));
        assertEquals("VI", NumberUtil.numeralOf(6));
        assertEquals("XX", NumberUtil.numeralOf(20));
        assertEquals("LIX", NumberUtil.numeralOf(59));
    }
}
