package com.projecki.fusion.serializer.location;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import org.bukkit.Location;
import org.bukkit.World;

import java.io.IOException;

public class LocationSerializer extends StdSerializer<Location> {

    public LocationSerializer() {
        this(null);
    }

    protected LocationSerializer(Class<Location> t) {
        super(t);
    }

    @Override
    public void serialize(
            Location location,
            JsonGenerator jsonGenerator,
            SerializerProvider serializerProvider
    ) throws IOException {
        jsonGenerator.writeStartObject();
        World world = location.getWorld();
        jsonGenerator.writeStringField("world", world == null ? "none" : world.getName());
        jsonGenerator.writeNumberField("x", location.getX());
        jsonGenerator.writeNumberField("y", location.getY());
        jsonGenerator.writeNumberField("z", location.getZ());
        jsonGenerator.writeNumberField("pitch", location.getPitch());
        jsonGenerator.writeNumberField("yaw", location.getYaw());
        jsonGenerator.writeEndObject();
    }
}
