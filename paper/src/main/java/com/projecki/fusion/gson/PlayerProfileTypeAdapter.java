package com.projecki.fusion.gson;

import com.destroystokyo.paper.profile.PlayerProfile;
import com.destroystokyo.paper.profile.ProfileProperty;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.projecki.fusion.util.ProfileUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static com.projecki.fusion.util.ProfileUtil.*;

/**
 * @since July 08, 2022
 * @author Andavin
 */
public class PlayerProfileTypeAdapter extends TypeAdapter<PlayerProfile> {

    @Override
    public void write(JsonWriter out, PlayerProfile profile) throws IOException {

        out.beginObject();
        UUID id = profile.getId();
        if (id != null) {
            out.name(PROFILE_ID);
            out.value(id.toString());
        }

        String name = profile.getName();
        if (name != null) {
            out.name(PROFILE_NAME);
            out.value(name);
        }

        Set<ProfileProperty> properties = profile.getProperties();
        if (!properties.isEmpty()) {

            out.name(PROFILE_PROPERTIES);
            out.beginObject();
            for (ProfileProperty property : properties) {

                out.name(property.getName());
                out.beginObject();
                out.name(PROFILE_PROPERTY_VALUE);
                out.value(property.getValue());
                // Null check on the signature only
                String signature = property.getSignature();
                if (signature != null) {
                    out.name(PROFILE_PROPERTY_SIGNATURE);
                    out.value(signature);
                }

                out.endObject();
            }

            out.endObject();
        }

        out.endObject();
    }

    @Override
    public PlayerProfile read(JsonReader in) throws IOException {

        in.beginObject();
        UUID id = null;
        String name = null;
        List<Property> properties = null;
        while (in.hasNext()) {
            switch (in.nextName()) {
                case PROFILE_ID -> id = UUID.fromString(in.nextString());
                case PROFILE_NAME -> name = in.nextString();
                case PROFILE_PROPERTIES -> {

                    properties = new ArrayList<>();
                    in.beginObject();
                    while (in.hasNext()) {

                        String key = in.nextName();
                        String value = null, signature = null;
                        in.beginObject();
                        while (in.hasNext()) {
                            switch (in.nextName()) {
                                case PROFILE_PROPERTY_VALUE -> value = in.nextString();
                                case PROFILE_PROPERTY_SIGNATURE -> signature = in.nextString();
                            }
                        }

                        in.endObject();
                        properties.add(new Property(key, value, signature));
                    }

                    in.endObject();
                }
            }
        }

        in.endObject();

        GameProfile profile = new GameProfile(id, name);
        if (properties != null) {
            properties.forEach(p -> profile.getProperties().put(p.getName(), p));
        }

        return ProfileUtil.createProfile(profile);
    }
}
