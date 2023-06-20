package com.projecki.fusion.util.expression;

import org.junit.jupiter.api.Test;

import java.util.List;

import static com.projecki.fusion.util.expression.ExpressionUtil.Section;
import static com.projecki.fusion.util.expression.ExpressionUtil.fixVariableCase;
import static com.projecki.fusion.util.expression.ExpressionUtil.isAlpha;
import static com.projecki.fusion.util.expression.ExpressionUtil.isOperator;
import static com.projecki.fusion.util.expression.ExpressionUtil.replaceImplicitMultiply;
import static com.projecki.fusion.util.expression.ExpressionUtil.sectionParentheses;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @since March 14, 2022
 * @author Andavin
 */
public class ExpressionUtilTest {

    @Test
    public void testAlpha() {

        assertFalse(isAlpha((char) ('a' - 1)));
        assertFalse(isAlpha((char) ('z' + 1)));
        for (char c = 'a'; c <= 'z'; c++) {
            assertTrue(isAlpha(c));
        }

        assertFalse(isAlpha((char) ('A' - 1)));
        assertFalse(isAlpha((char) ('Z' + 1)));
        for (char c = 'A'; c <= 'Z'; c++) {
            assertTrue(isAlpha(c));
        }
    }

    @Test
    public void testOperator() {
        assertFalse(isOperator('c'));
        assertFalse(isOperator('$'));
        assertTrue(isOperator('*'));
        assertTrue(isOperator('+'));
        assertTrue(isOperator('^'));
        assertTrue(isOperator('%'));
    }

    @Test
    public void testFixVariableCase() {
        String expression = "(X*9)/2+alITtlevar";
        List<String> variables = List.of("x", "aLittleVar");
        String result = fixVariableCase(expression, variables);
        assertEquals("(x*9)/2+aLittleVar", result);
    }

    @Test
    public void testReplaceImplicitMultiply() {
        String expression = "(9x)+2(2aLittleVar5)x3(5+aLittleVar)2";
        List<String> variables = List.of("x", "aLittleVar");
        String result = replaceImplicitMultiply(expression, variables);
        assertEquals("(9*x)+2*(2*aLittleVar*5)*x*3*(5+aLittleVar)*2", result);
    }

    @Test
    public void testSimpleSectionParentheses() {
        String expression = "(9x+3)/2+y";
        List<Section> sections = sectionParentheses(expression);
        assertEquals(2, sections.size());
        assertEquals("9x+3", sections.get(0).str());
        assertEquals("/2+y", sections.get(1).str());
    }

    @Test
    public void testComplexSectionParentheses() {
        String expression = "(9*8(other))(b+a*(v))";
        List<Section> sections = sectionParentheses(expression);
        assertEquals(2, sections.size());
        assertEquals("9*8(other)", sections.get(0).str());
        assertEquals("b+a*(v)", sections.get(1).str());
    }
}
