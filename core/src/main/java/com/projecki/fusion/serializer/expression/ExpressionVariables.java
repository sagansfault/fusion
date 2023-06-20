package com.projecki.fusion.serializer.expression;

import com.projecki.fusion.util.expression.Expression;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.List;

/**
 * Denotes variables for the annotated element that are used
 * in the {@link Expression#parse(String, List)} method.
 *
 * @since June 12, 2022
 * @author Andavin
 * @see Expression
 * @see Expression#parse(String, List)
 * @see ExpressionDeserializer
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.FIELD, ElementType.PARAMETER })
public @interface ExpressionVariables {

    /**
     * The variables to use parsing the {@link Expression}.
     * <p>
     *     Each of these variables should be unique within
     *     this list.
     * </p>
     *
     * @return The list of variables.
     */
    String[] value();
}
