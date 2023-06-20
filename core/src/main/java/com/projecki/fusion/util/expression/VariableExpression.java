package com.projecki.fusion.util.expression;

import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * An {@link Expression} that evaluates the value of a
 * variable from the variables provided and returns whatever
 * value is found.
 *
 * @since March 14, 2022
 * @author Andavin
 */
public record VariableExpression(String variable) implements Expression {

    @Override
    public double evaluate(Map<String, Number> variableValues) {
        Number value = variableValues.get(variable);
        checkNotNull(value, "value not provided for variable '%s'", variable);
        return value.doubleValue();
    }

    @Override
    public String toString() {
        return variable;
    }
}
