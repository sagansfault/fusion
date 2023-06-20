package com.projecki.fusion.serializer.profile;

import com.destroystokyo.paper.profile.PlayerProfile;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.projecki.fusion.util.ProfileUtil;

import java.io.IOException;
import java.util.UUID;
import java.util.function.Function;

import static com.projecki.fusion.util.ProfileUtil.*;

/**
 * @since July 07, 2022
 * @author Andavin
 */
public class ProfileDeserializer extends StdDeserializer<PlayerProfile> {

    public ProfileDeserializer() {
        this(null);
    }

    public ProfileDeserializer(Class<?> vc) {
        super(vc);
    }

    @Override
    public PlayerProfile deserialize(JsonParser p, DeserializationContext ctx) throws IOException {

        JsonNode node = p.getCodec().readTree(p);
        UUID id = parseOrNull(node.get(PROFILE_ID), n -> UUID.fromString(n.asText()));
        String name = parseOrNull(node.get(PROFILE_NAME), JsonNode::asText);

        GameProfile profile = new GameProfile(id, name);
        if (node.get(PROFILE_PROPERTIES) instanceof ObjectNode propertiesNode) {
            propertiesNode.fields().forEachRemaining(entry -> {
                String key = entry.getKey();
                JsonNode propertyNode = entry.getValue();
                profile.getProperties().put(key, new Property(
                        key,
                        propertyNode.get(PROFILE_PROPERTY_VALUE).asText(),
                        parseOrNull(propertyNode.get(PROFILE_PROPERTY_SIGNATURE), JsonNode::asText)
                ));
            });
        }

        return ProfileUtil.createProfile(profile);
    }

    private static <T> T parseOrNull(JsonNode node, Function<JsonNode, T> parser) {
        return node != null ? parser.apply(node) : null;
    }
}
