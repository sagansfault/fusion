package com.projecki.fusion.config.serialize;

import com.google.gson.JsonParseException;
import com.projecki.fusion.FusionCore;

import java.util.Optional;

/**
 * @deprecated Use {@link com.projecki.fusion.serializer.formatted.GsonSerializer}
 */
@Deprecated
public class JsonSerializer<T> extends Serializer<T> {

    public JsonSerializer(Class<T> targetType) {
        super(targetType);
    }

    @Override
    public final String serialize(T object) {
        return FusionCore.GSON.toJson(object);
    }

    @Override
    public final Optional<T> deserialize(String s) {
        try {
            return Optional.of(FusionCore.GSON.fromJson(s, super.targetType));
        } catch (JsonParseException ignored) {
            return Optional.empty();
        }
    }

    @Override
    public String getExtension() {
        return "json";
    }
}
