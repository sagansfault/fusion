package com.projecki.fusion.serializer.expression;

import com.projecki.fusion.config.serialize.JacksonSerializer;
import com.projecki.fusion.util.expression.Expression;
import org.junit.jupiter.api.Test;

import java.io.UncheckedIOException;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * @since June 12, 2022
 * @author Andavin
 */
public class ExpressionDeserializerTest {

    @Test
    void parsePresentRecordExpression() {
        JacksonSerializer<PresentTestRecord> serializer = JacksonSerializer.ofYaml(PresentTestRecord.class);
        assertDoesNotThrow(() -> serializer.deserialize("expression: 2*var"));
        assertThrows(UncheckedIOException.class, () -> serializer.deserialize("expression: 2*val"));
    }

    @Test
    void parsePresentFieldExpression() {
        JacksonSerializer<PresentTestClass> serializer = JacksonSerializer.ofYaml(PresentTestClass.class);
        assertDoesNotThrow(() -> serializer.deserialize("expression: 2*var"));
        assertThrows(UncheckedIOException.class, () -> serializer.deserialize("expression: 2*val"));
    }

    @Test
    void parseAbsentRecordExpression() {
        JacksonSerializer<AbsentTestRecord> serializer = JacksonSerializer.ofYaml(AbsentTestRecord.class);
        assertDoesNotThrow(() -> serializer.deserialize("expression: 2*5+6"));
        assertThrows(UncheckedIOException.class, () -> serializer.deserialize("expression: 2*val"));
    }

    @Test
    void parseAbsentFieldExpression() {
        JacksonSerializer<AbsentTestClass> serializer = JacksonSerializer.ofYaml(AbsentTestClass.class);
        assertDoesNotThrow(() -> serializer.deserialize("expression: 2*5+6"));
        assertThrows(UncheckedIOException.class, () -> serializer.deserialize("expression: 2*var"));
    }

    @Test
    void parseDoubleRecordExpression() {
        JacksonSerializer<DoubleTestRecord> serializer = JacksonSerializer.ofYaml(DoubleTestRecord.class);
        assertDoesNotThrow(() -> serializer.deserialize("exp1: 2*var\nexp2: 1*val+9"));
        assertThrows(UncheckedIOException.class, () -> serializer.deserialize("exp2: 2*var"));
    }

    public record PresentTestRecord(@ExpressionVariables("var") Expression expression) {
    }

    public record AbsentTestRecord(Expression expression) {
    }

    public record DoubleTestRecord(@ExpressionVariables("var") Expression exp1,
                                   @ExpressionVariables("val") Expression exp2) {
    }

    public static final class PresentTestClass {

        @ExpressionVariables("var")
        private Expression expression;

        public Expression getExpression() {
            return expression;
        }
    }

    public static final class AbsentTestClass {

        private Expression expression;

        public Expression getExpression() {
            return expression;
        }
    }
}
