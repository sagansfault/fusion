package com.projecki.fusion.util.expression;

import com.projecki.fusion.util.NumberUtil;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.google.common.base.Preconditions.checkArgument;
import static com.projecki.fusion.util.expression.ExpressionUtil.CLOSE_PARENTHESIS;
import static com.projecki.fusion.util.expression.ExpressionUtil.OPEN_PARENTHESIS;
import static com.projecki.fusion.util.expression.ExpressionUtil.fixVariableCase;
import static com.projecki.fusion.util.expression.ExpressionUtil.isAlpha;
import static com.projecki.fusion.util.expression.ExpressionUtil.isOperator;
import static com.projecki.fusion.util.expression.ExpressionUtil.parseExpression;
import static com.projecki.fusion.util.expression.ExpressionUtil.replaceImplicitMultiply;

/**
 * A mathematical algebraic expression that can be evaluated
 * by passing in the required variable values.
 *
 * @since March 14, 2022
 * @author Andavin
 */
public interface Expression {

    /**
     * Evaluate this expression using the specified values
     * for each variable.
     *
     * @param variableValues The values for the variables.
     * @return The result of the evaluated expression.
     */
    double evaluate(Map<String, Number> variableValues);

    /**
     * Parse the given string into an {@link Expression} that
     * can be evaluated given the proper variable values, if required.
     *
     * @param str The string to parse as an expression.
     * @param variables All the variables available for use in the expression.
     * @return The parsed {@link Expression}.
     * @throws IllegalArgumentException If the string cannot be parsed.
     */
    static Expression parse(String str, List<String> variables) {
        validateExpressionString(str);
        str = fixVariableCase(str, variables);
        str = replaceImplicitMultiply(str, variables);
        return parseExpression(true, str, Set.copyOf(variables));
    }

    private static void validateExpressionString(String expression) {

        checkArgument(!expression.isBlank(), "blank expression");
        int alphaNumericOp = 0;
        int len = expression.length();
        for (int i = 0; i < len; i++) {

            char c = expression.charAt(i);
            switch (c) {
                // Parentheses, dots and underscore are allowed characters
                case OPEN_PARENTHESIS, CLOSE_PARENTHESIS, NumberUtil.DECIMAL, '_':
                    break;
                default:
                    checkArgument(isAlpha(c) || NumberUtil.isNumeric(c) || isOperator(c),
                            "invalid expression char '%s' at %s", expression, i);
                    alphaNumericOp++;
                    break;
            }
        }

        checkArgument(alphaNumericOp > 0, "invalid expression: %s", expression);
    }
}
