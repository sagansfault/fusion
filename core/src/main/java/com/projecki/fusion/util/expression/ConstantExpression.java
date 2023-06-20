package com.projecki.fusion.util.expression;

import java.text.NumberFormat;
import java.util.Map;

/**
 * A constant {@link Expression} that always returns the
 * same result when {@link Expression#evaluate(Map) evaluated}
 * as it has no requirement for variables.
 *
 * @since March 14, 2022
 * @author Andavin
 */
public record ConstantExpression(double value) implements Expression {

    @Override
    public double evaluate(Map<String, Number> variableValues) {
        return value;
    }

    @Override
    public String toString() {
        return NumberFormat.getInstance().format(value);
    }
}
