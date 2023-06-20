package com.projecki.fusion.serializer.location;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

import java.io.IOException;

public class LocationDeserializer extends StdDeserializer<Location> {

    public LocationDeserializer() {
        this(null);
    }

    protected LocationDeserializer(Class<?> vc) {
        super(vc);
    }

    @Override
    public Location deserialize(
            JsonParser jsonParser,
            DeserializationContext deserializationContext
    ) throws IOException {

        JsonNode root = jsonParser.getCodec().readTree(jsonParser);
        double x = root.get("x").doubleValue();
        double y = root.get("y").doubleValue();
        double z = root.get("z").doubleValue();
        float pitch = root.get("pitch").floatValue();
        float yaw = root.get("yaw").floatValue();
        String worldName = root.get("world").textValue();
        World world = Bukkit.getWorld(worldName);

        if (world == null) {
            throw new LocationDeserializationException("Could not find world with the name present");
        }

        return new Location(world, x, y, z, yaw, pitch);
    }

    private static final class LocationDeserializationException extends JsonProcessingException {
        public LocationDeserializationException(String msg) {
            super(msg);
        }
    }
}
