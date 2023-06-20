package com.projecki.fusion.util.expression;

import java.util.Map;

/**
 * An {@link Expression} that evaluates the result of two
 * other {@link Expression Expressions} and applies an
 * {@link Operator} to the results.
 *
 * @since March 14, 2022
 * @author Andavin
 */
public record OperationExpression(Operator operator, Expression left, Expression right) implements Expression {

    @Override
    public double evaluate(Map<String, Number> variableValues) {
        return operator.apply(left.evaluate(variableValues), right.evaluate(variableValues));
    }

    @Override
    public String toString() {
        return left.toString() + operator + right.toString();
    }
}
