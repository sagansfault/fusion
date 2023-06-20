package com.projecki.fusion.serializer.formatted;

import com.google.gson.Gson;
import com.projecki.fusion.FusionCore;

/**
 * @since July 08, 2022
 * @author Andavin
 */
public class GsonSerializer implements FormattedSerializer {

    private final Gson gson;

    private GsonSerializer(Gson gson) {
        this.gson = gson;
    }

    @Override
    public String serialize(Object obj) {
        return gson.toJson(obj);
    }

    @Override
    public <T> T deserialize(Class<T> type, String input) {
        return gson.fromJson(input, type);
    }

    /**
     * Create a new {@link GsonSerializer} with the default {@link Gson}
     * instance as the serializer.
     *
     * @return The new {@link GsonSerializer}.
     */
    public static GsonSerializer of() {
        return new GsonSerializer(FusionCore.GSON);
    }

    /**
     * Create a new {@link GsonSerializer} with a custom {@link Gson} instance.
     *
     * @param gson The {@link Gson} instance to use as the serializer.
     * @return The new {@link GsonSerializer}.
     */
    public static GsonSerializer of(Gson gson) {
        return new GsonSerializer(gson);
    }
}
