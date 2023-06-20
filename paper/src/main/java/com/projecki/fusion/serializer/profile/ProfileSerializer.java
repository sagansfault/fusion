package com.projecki.fusion.serializer.profile;

import com.destroystokyo.paper.profile.PlayerProfile;
import com.destroystokyo.paper.profile.ProfileProperty;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import java.io.IOException;
import java.util.Set;
import java.util.UUID;

import static com.projecki.fusion.util.ProfileUtil.*;

/**
 * @since July 07, 2022
 * @author Andavin
 */
public class ProfileSerializer extends StdSerializer<PlayerProfile> {

    public ProfileSerializer() {
        this(null);
    }

    protected ProfileSerializer(Class<PlayerProfile> t) {
        super(t);
    }

    @Override
    public void serialize(PlayerProfile profile, JsonGenerator gen, SerializerProvider provider) throws IOException {

        gen.writeStartObject();
        UUID id = profile.getId();
        if (id != null) {
            gen.writeStringField(PROFILE_ID, id.toString());
        }

        String name = profile.getName();
        if (name != null) {
            gen.writeStringField(PROFILE_NAME, name);
        }

        Set<ProfileProperty> properties = profile.getProperties();
        if (!properties.isEmpty()) {

            gen.writeFieldName(PROFILE_PROPERTIES);
            gen.writeStartObject();
            for (ProfileProperty property : properties) {

                gen.writeFieldName(property.getName());
                gen.writeStartObject();
                gen.writeStringField(PROFILE_PROPERTY_VALUE, property.getValue());
                // Null check on the signature only
                String signature = property.getSignature();
                if (signature != null) {
                    gen.writeStringField(PROFILE_PROPERTY_SIGNATURE, signature);
                }

                gen.writeEndObject();
            }

            gen.writeEndObject();
        }

        gen.writeEndObject();
    }
}
