package com.projecki.fusion.util.expression;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @since March 15, 2022
 * @author Andavin
 */
public class ExpressionTest {

    @Test
    public void testParse() {

        String str = "(9x+3)/2+y/8+9(x+y)*4/5.24";
        List<String> variables = List.of("x", "y");
        Expression expression = assertDoesNotThrow(() -> Expression.parse(str, variables));

        double x = 2, y = 5;
        Map<String, Number> values = Map.of("x", x, "y", y);
        assertEquals((9 * x + 3) / 2 + y / 8 + 9 * (x + y) * 4 / 5.24, expression.evaluate(values));
    }
}
