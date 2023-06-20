package com.projecki.fusion.util.expression;

import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.DoubleBinaryOperator;

import static java.util.Collections.unmodifiableSet;

/**
 * @since March 14, 2022
 * @author Andavin
 */
public enum Operator {

    EXPONENT('^', Math::pow),
    MULTIPLY('*', (l, r) -> l * r),
    DIVIDE('/', (l, r) -> l / r),
    REMAINDER('%', (l, r) -> l % r),
    ADD('+', Double::sum),
    SUBTRACT('-', (l, r) -> l - r);

    static final List<Operator> VALUES = List.of(Operator.values());
    static final List<Set<Operator>> ORDER_OF_OPERATIONS = List.of(
            unmodifiableSet(EnumSet.of(EXPONENT)),
            unmodifiableSet(EnumSet.of(MULTIPLY, DIVIDE, REMAINDER)),
            unmodifiableSet(EnumSet.of(ADD, SUBTRACT))
    );

    private final char symbol;
    private final DoubleBinaryOperator operation;

    Operator(char symbol, DoubleBinaryOperator operation) {
        this.symbol = symbol;
        this.operation = operation;
    }

    /**
     * The symbol that signifies this operator in
     * an {@link Expression}.
     *
     * @return The symbol for this operator.
     */
    public char symbol() {
        return symbol;
    }

    /**
     * Apply this operator to the specified values and
     * return the result.
     *
     * @param left The first value to the left of the operator.
     * @param right The second value to the right of the operator.
     * @return The result of the operation being applied to the values.
     */
    public double apply(double left, double right) {
        return operation.applyAsDouble(left, right);
    }

    @Override
    public String toString() {
        return String.valueOf(symbol);
    }

    /**
     * Get the operator for the specified character if
     * the character matches a symbol for an operator.
     *
     * @param c The character to get the operator for.
     * @return The operator for the character.
     */
    public static Optional<Operator> valueOf(char c) {

        for (Operator operator : VALUES) {

            if (operator.symbol == c) {
                return Optional.of(operator);
            }
        }

        return Optional.empty();
    }
}
