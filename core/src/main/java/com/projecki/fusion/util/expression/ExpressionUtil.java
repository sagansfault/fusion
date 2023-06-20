package com.projecki.fusion.util.expression;

import com.projecki.fusion.util.NumberUtil;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;
import static com.projecki.fusion.util.expression.Operator.ORDER_OF_OPERATIONS;

/**
 * @since March 14, 2022
 * @author Andavin
 */
public final class ExpressionUtil {

    public static final char OPEN_PARENTHESIS = '(', CLOSE_PARENTHESIS = ')';

    /**
     * Determine whether the specified character represents
     * an alphabetic symbol in the english/latin alphabet.
     *
     * @param c The character to determine.
     * @return If the character is an alphabetic symbol.
     */
    public static boolean isAlpha(char c) {
        return 'a' <= c && c <= 'z' || 'A' <= c && c <= 'Z';
    }

    /**
     * Determine whether the specified character represents
     * an {@link Operator} symbol.
     *
     * @param c The character to determine.
     * @return If the character is an {@link Operator} symbol.
     */
    public static boolean isOperator(char c) {

        for (Operator operator : Operator.VALUES) {

            if (operator.symbol() == c) {
                return true;
            }
        }

        return false;
    }

    /**
     * Replace each occurrence of a variable with the proper
     * case-sensitive version provided.
     *
     * @param str The string expression to fix the variables within.
     * @param variables The variable with proper case.
     * @return The expression with proper case sensitivity.
     */
    public static String fixVariableCase(String str, List<String> variables) {

        String lower = str.toLowerCase(Locale.ROOT);
        StringBuilder replaced = new StringBuilder(str);
        for (String variable : variables) {

            int index = lower.indexOf(variable.toLowerCase(Locale.ROOT));
            if (index != -1) {
                replaced.replace(index, index + variable.length(), variable);
            }
        }

        return replaced.toString();
    }

    /**
     * Replace the multiplication without an operator symbols to add
     * in the symbol. For example, replace {@code 9x} with {@code 9*x}.
     * <p>
     * Note that variables are case-sensitive.
     *
     * @param str       The expression string to search and replace.
     * @param variables The variables that are applicable to the expression.
     * @return The expression string with the replacements.
     */
    public static String replaceImplicitMultiply(String str, List<String> variables) {

        StringBuilder expression = new StringBuilder(str);
        for (String variable : variables) {

            for (int index = expression.indexOf(variable); index != -1;
                 index = expression.indexOf(variable, index + variable.length())) {

                // If the surrounding characters are not operators, then insert
                // a multiplication symbol between them and the variable
                int previousIndex = index - 1;
                if (previousIndex >= 0) {

                    char c = expression.charAt(previousIndex);
                    if (c != OPEN_PARENTHESIS && !isOperator(c)) {
                        expression.insert(index++, Operator.MULTIPLY.symbol()); // Increment for the next insertion
                    }
                }

                int nextIndex = index + variable.length();
                if (nextIndex < expression.length()) {

                    char c = expression.charAt(nextIndex);
                    if (c != CLOSE_PARENTHESIS && !isOperator(c)) {
                        expression.insert(nextIndex, Operator.MULTIPLY.symbol());
                    }
                }
            }
        }

        String openPar = String.valueOf(OPEN_PARENTHESIS);
        int open = expression.indexOf(openPar);
        while (open != -1) {

            if (open > 0) { // Not the first char
                // Replace parentheses that are back to back )( with )*(
                // Replace parentheses that are following a number 9(exp) with 9*(exp)
                char prevChar = expression.charAt(open - 1);
                if (prevChar == CLOSE_PARENTHESIS || NumberUtil.isNumeric(prevChar)) {
                    expression.insert(open, Operator.MULTIPLY.symbol());
                }
            }

            open = expression.indexOf(openPar, open + 1);
        }

        String closePar = String.valueOf(CLOSE_PARENTHESIS);
        int close = expression.indexOf(closePar);
        while (close != -1) {

            if (close < expression.length() - 1) { // Not the last char
                // Replace parentheses that are followed by a number (exp)9 with (exp)*9
                char nextChar = expression.charAt(close + 1);
                if (nextChar == CLOSE_PARENTHESIS || NumberUtil.isNumeric(nextChar)) {
                    expression.insert(close + 1, Operator.MULTIPLY.symbol());
                }
            }

            close = expression.indexOf(closePar, close + 1);
        }

        return expression.toString();
    }

    /**
     * Parse the specified string as an {@link Expression}.
     * <br>
     * If the string begins with an {@link Operator} symbol, then
     * it will be applied to the specified previous expression.
     * <p>
     * Note that variables are case-sensitive. Also, no operator
     * multiplication such as {@code 9x} is not supported.
     * <br>
     * It is recommended to use methods such as {@link #fixVariableCase(String, List)}
     * and {@link #replaceImplicitMultiply(String, List)} on an expression
     * before this method is invoked.
     *
     * @param first The previous expression to apply to.
     * @param str The string to parse as an {@link Expression}.
     * @param variables The allowed variables within the expression.
     * @return The parsed {@link Expression}.
     * @throws IllegalArgumentException If an unknown variable is encountered.
     */
    public static Expression parseExpression(boolean first, String str, Set<String> variables) {

        List<Token> tokens = tokenizeExpression(first, str, variables);
        if (tokens.size() == 1) {
            return tokens.get(0).expression();
        }
        // Reduce the expressions down by combining pairs in the order of operations
        for (Set<Operator> operators : ORDER_OF_OPERATIONS) {

            ListIterator<Token> itr = tokens.listIterator();
            while (itr.hasNext()) {

                Token left = itr.next();
                if (!itr.hasNext()) {
                    break;
                }

                Token right = itr.next();
                Optional<Operator> operator = left.rightOp().filter(operators::contains)
                        .or(() -> right.leftOp().filter(operators::contains));
                if (operator.isEmpty()) {
                    itr.previous();
                    continue;
                }

                itr.remove(); // Remove the right expression
                itr.previous(); // Go to the left
                // Replace it with the combination of the two expressions
                itr.set(new Token(
                        new OperationExpression(
                                operator.get(),
                                left.expression(),
                                right.expression()
                        ),
                        left.leftOp(),
                        right.rightOp()
                ));
            }
        }

        checkState(tokens.size() == 1, "unable to combine all expressions");
        return tokens.get(0).expression();
    }

    /**
     * Break down the given expression string into {@link Token}.
     *
     * @param first If this is the first expression in the parsing order.
     * @param str The string to tokenize.
     * @param variables The allowed variables within the expression.
     * @return The {@link Token ExpressionTokens}.
     */
    public static List<Token> tokenizeExpression(boolean first, String str, Set<String> variables) {
        // Section out the parentheses expressions
        List<Section> sections = sectionParentheses(str);
        int size = sections.size();
        checkArgument(size > 0, "invalid expression: %s", str);
        List<Token> tokens = new ArrayList<>();
        if (size == 1) {
            // Split the expressions around operators into nodes
            int len = str.length();
            checkArgument(len > 0, "empty expression");
            Optional<Operator> leftOp = Operator.valueOf(str.charAt(0));
            // Ensure that the expression doesn't start with an operator
            // (i.e. there is a previous expression)
            checkArgument(!first || leftOp.isEmpty(),
                    "dangling operator: %s", str);

            StringBuilder intervening = new StringBuilder();
            for (int i = leftOp.isPresent() ? 1 : 0; i < len; i++) {

                char c = str.charAt(i);
                Optional<Operator> rightOp = Operator.valueOf(c);
                if (rightOp.isPresent()) {
                    // The intervening value is always a variable or a constant
                    Expression interExp = parseVariableOrConstant(intervening.toString(), variables);
                    tokens.add(new Token(interExp, leftOp, rightOp));
                    intervening.setLength(0); // Clear
                    leftOp = rightOp;
                } else {
                    // Intervening value to parse later
                    intervening.append(c);
                }
            }

            if (!intervening.isEmpty()) {
                Expression interExp = parseVariableOrConstant(intervening.toString(), variables);
                tokens.add(new Token(interExp, leftOp, Optional.empty()));
            }
        } else {

            Iterator<Section> itr = sections.iterator();
            Optional<Operator> carriedLeftOp = Operator.valueOf(sections.get(0).str().charAt(0));
            while (itr.hasNext()) {

                Section section = itr.next();
                String s = section.str();
                Optional<Operator> rightOp = Operator.valueOf(s.charAt(s.length() - 1));
                if (rightOp.isPresent() && s.length() == 1 && itr.hasNext()) {
                    // A single character section that is only an operator
                    // Move on to the next section and adjust the operators accordingly
                    carriedLeftOp = rightOp;
                    first = true;
                } else if (section.encapsulated()) {
                    Expression exp = parseExpression(first, s, variables);
                    tokens.add(new Token(exp, carriedLeftOp, rightOp));
                    first = false;
                } else {
                    tokens.addAll(tokenizeExpression(first, s, variables));
                    first = false;
                }
            }
        }

        return tokens;
    }

    /**
     * Section off the given expression string into separate string
     * expressions separating each by the parentheses it is encapsulated by.
     * <p>
     * If there are no parentheses present in the given string, then
     * a list containing the same value given will be returned.
     *
     * @param str The expression string to section off.
     * @return The different expression sections.
     */
    public static List<Section> sectionParentheses(String str) {

        int opening = str.indexOf(OPEN_PARENTHESIS);
        if (opening == -1) { // No opening parenthesis
            // Do a quick check for any dangling close parentheses
            int close = str.indexOf(CLOSE_PARENTHESIS);
            checkArgument(close == -1, "dangling close parenthesis at %s", close);
            return List.of(new Section(str, false));
        }

        int nested = 0;
        int len = str.length();
        boolean closed = false;
        List<Section> sections = new ArrayList<>(5);
        StringBuilder current = new StringBuilder(len - 2);
        for (int i = opening + 1; i < len; i++) {

            char c = str.charAt(i);
            switch (c) {
                case OPEN_PARENTHESIS -> {

                    if (closed) { // A new opening parenthesis

                        opening = i;
                        closed = false;
                        if (!current.isEmpty()) { // Separate the between section
                            sections.add(new Section(current.toString(), false));
                            current.setLength(0);
                        }
                    } else {
                        // Count each new opening parenthesis
                        // we find before the appropriate closing
                        nested++;
                        current.append(c);
                    }
                }
                case CLOSE_PARENTHESIS -> {

                    if (nested == 0) {
                        closed = true;
                        sections.add(new Section(current.toString(), true));
                        current.setLength(0); // Clear contents
                    } else {
                        nested--;
                        current.append(c);
                    }
                }
                default -> current.append(c);
            }
        }

        checkArgument(closed, "unclosed parenthesis at %s", opening);
        if (!current.isEmpty()) {
            sections.add(new Section(current.toString(), false));
        }

        return sections;
    }

    /**
     * Parse the given string as either a {@link VariableExpression}
     * or a {@link ConstantExpression}.
     *
     * @param str The string to parse.
     * @param variables The allowed variables.
     * @return The parsed {@link Expression}.
     * @throws IllegalArgumentException If the string was not a variable
     *                                  or a constant.
     */
    public static Expression parseVariableOrConstant(String str, Set<String> variables) {

        if (variables.contains(str)) {
            return new VariableExpression(str);
        }

        try {
            return new ConstantExpression(Double.parseDouble(str));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("unknown variable encountered: '" + str + '\'');
        }
    }

    public record Section(String str, boolean encapsulated) {
    }

    public record Token(Expression expression, Optional<Operator> leftOp, Optional<Operator> rightOp) {

        @Override
        public String toString() {
            return leftOp.map(Operator::toString).orElse("") +
                    expression +
                    rightOp.map(Operator::toString).orElse("");
        }
    }
}
