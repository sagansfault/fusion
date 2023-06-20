package com.projecki.fusion.serializer.expression;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.deser.ContextualDeserializer;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.projecki.fusion.util.expression.Expression;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @since June 12, 2022
 * @author Andavin
 */
public class ExpressionDeserializer extends StdDeserializer<Expression> implements ContextualDeserializer {

    private final Map<String, List<String>> variables = new HashMap<>();

    public ExpressionDeserializer() {
        this(null);
    }

    public ExpressionDeserializer(Class<?> vc) {
        super(vc);
    }

    @Override
    public JsonDeserializer<?> createContextual(DeserializationContext ctx, BeanProperty property) throws JsonMappingException {
        ExpressionVariables variables = property.getAnnotation(ExpressionVariables.class);
        this.variables.put(property.getName(), variables != null ? List.of(variables.value()) : List.of());
        return this;
    }

    @Override
    public Expression deserialize(JsonParser p, DeserializationContext ctx) throws IOException {
        JsonNode node = p.getCodec().readTree(p);
        return Expression.parse(node.asText(), variables.get(p.getCurrentName()));
    }
}
