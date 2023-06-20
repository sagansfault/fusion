package com.projecki.fusion.serializer.formatted;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.projecki.fusion.serializer.expression.ExpressionDeserializer;
import com.projecki.fusion.util.expression.Expression;

import java.io.IOException;
import java.io.StringWriter;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class JacksonSerializer implements FormattedSerializer {

    private static final List<SimpleModule> SERIALIZER_MODULES = new ArrayList<>();

    static {
        // Register the default module
        SimpleModule module = new SimpleModule();
        // Serializers
        // -- None for now --
        // Deserializers
        module.addDeserializer(Expression.class, new ExpressionDeserializer());
        // Register
        SERIALIZER_MODULES.add(module);
    }

    private final ObjectMapper mapper;

    private JacksonSerializer(Supplier<JsonFactory> factory) {
        ObjectMapper mapper = new ObjectMapper(factory.get())
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                .setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
        SERIALIZER_MODULES.forEach(mapper::registerModule);
        this.mapper = mapper;
    }

    @Override
    public String serialize(Object obj) {

        StringWriter writer = new StringWriter();
        try {
            mapper.writeValue(writer, obj);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }

        return writer.toString();
    }

    @Override
    public <T> T deserialize(Class<T> type, String input) {
        try {
            return mapper.readValue(input, type);
        } catch (JsonProcessingException e) {
            throw new UncheckedIOException(e);
        }
    }

    /**
     * Create a new {@link JacksonSerializer} that uses the {@link YAMLFactory}
     * to serialize to and deserialize from YAML.
     *
     * @return The new {@link JacksonSerializer}.
     */
    public static JacksonSerializer ofYaml() {
        return new JacksonSerializer(YAMLFactory::new);
    }

    /**
     * Create a new {@link JacksonSerializer} that uses the {@link JsonFactory}
     * to serialize to and deserialize from JSON.
     *
     * @return The new {@link JacksonSerializer}.
     */
    public static <T> JacksonSerializer ofJson() {
        return new JacksonSerializer(JsonFactory::new);
    }

    /**
     * Register a custom {@link SimpleModule} that should be added to every
     * {@link ObjectMapper} as a default handler when processing objects.
     *
     * @param module The {@link SimpleModule} to add.
     */
    public static void registerSerializerModule(SimpleModule module) {
        SERIALIZER_MODULES.add(module);
    }
}
