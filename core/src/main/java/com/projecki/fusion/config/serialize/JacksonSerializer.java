package com.projecki.fusion.config.serialize;

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
import java.util.Optional;
import java.util.function.Supplier;

/**
 * @deprecated Use {@link com.projecki.fusion.serializer.formatted.JacksonSerializer}
 * @since April 05, 2022
 * @author Andavin
 */
@Deprecated
public class JacksonSerializer<T> extends Serializer<T> {

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

    private final String extension;
    private final ObjectMapper mapper;

    private JacksonSerializer(Class<T> targetType, Supplier<JsonFactory> factory, String extension) {
        super(targetType);
        this.extension = extension;
        this.mapper = createGenericObjectMapper(factory);
    }

    public JacksonSerializer(Class<T> targetType, Supplier<JsonFactory> factory) {
        this(targetType, factory, factory.get() instanceof YAMLFactory ? "yml" : "json");
    }

    @Override
    public String serialize(T object) {

        StringWriter writer = new StringWriter();
        try {
            mapper.writeValue(writer, object);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }

        return writer.toString();
    }

    @Override
    public Optional<T> deserialize(String s) {
        try {
            return Optional.of(mapper.readValue(s, targetType));
        } catch (JsonProcessingException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public String getExtension() {
        return extension;
    }

    public static <T> JacksonSerializer<T> ofYaml(Class<T> targetType) {
        return new JacksonSerializer<>(targetType, YAMLFactory::new, "yml");
    }

    public static <T> JacksonSerializer<T> ofJson(Class<T> targetType) {
        return new JacksonSerializer<>(targetType, JsonFactory::new, "json");
    }

    public static ObjectMapper createGenericObjectMapper(Supplier<JsonFactory> factory) {
        ObjectMapper mapper = new ObjectMapper(factory.get())
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                .setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
        SERIALIZER_MODULES.forEach(mapper::registerModule);
        return mapper;
    }

    /**
     * @deprecated Use {@link com.projecki.fusion.serializer.formatted.JacksonSerializer#registerSerializerModule(SimpleModule)}
     */
    @Deprecated
    public static void registerSerializerModule(SimpleModule module) {
        SERIALIZER_MODULES.add(module);
    }
}
