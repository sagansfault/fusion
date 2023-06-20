package com.projecki.fusion.util;

import com.destroystokyo.paper.profile.PlayerProfile;
import com.destroystokyo.paper.profile.ProfileProperty;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.projecki.unversioned.craft.CraftService;
import com.projecki.unversioned.tag.TagService;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.Map.Entry;

import static com.google.common.base.Preconditions.checkArgument;
import static org.apache.commons.lang.StringUtils.isNotEmpty;

/**
 * @since April 09, 2022
 * @author Andavin
 */
public final class ProfileUtil {

    public static final String PROFILE_ID = "Id";
    public static final String PROFILE_NAME = "Name";
    public static final String PROFILE_PROPERTIES = "Properties";
    public static final String PROFILE_PROPERTY_VALUE = "Value";
    public static final String PROFILE_PROPERTY_SIGNATURE = "Signature";
    public static final String PROFILE_TEXTURES = "textures";
    private static final UUID DEFAULT = new UUID(1371799217210408961L, -9135298389881481120L);

    /**
     * Create a new instance of {@link PlayerProfile} using the
     * specified {@link GameProfile}.
     *
     * @param profile The {@link GameProfile} to use.
     * @return The new {@link PlayerProfile}.
     */
    public static PlayerProfile createProfile(GameProfile profile) {
        return CraftService.INSTANCE.createProfile(profile);
    }

    /**
     * Create a new instance of {@link PlayerProfile} using the
     * specified values.
     *
     * @param id The {@link PlayerProfile#getId() ID} of the {@link PlayerProfile}.
     * @param name The {@link PlayerProfile#getName() name} of the {@link PlayerProfile}.
     * @return The new {@link PlayerProfile}.
     * @see #valueToProfile(String)
     */
    public static PlayerProfile createProfile(UUID id, String name) {
        return createProfile(new GameProfile(id, name));
    }

    /**
     * Create a new instance of {@link PlayerProfile} using the
     * specified values.
     *
     * @param id The {@link PlayerProfile#getId() ID} of the {@link PlayerProfile}.
     * @param name The {@link PlayerProfile#getName() name} of the {@link PlayerProfile}.
     * @param value The value to use for textures.
     * @return The new {@link PlayerProfile}.
     * @see #valueToProfile(String)
     */
    public static PlayerProfile createProfile(UUID id, String name, String value) {
        return createProfile(id, name, value, null);
    }

    /**
     * Create a new instance of {@link PlayerProfile} using the
     * specified values.
     *
     * @param id The {@link PlayerProfile#getId() ID} of the {@link PlayerProfile}.
     * @param name The {@link PlayerProfile#getName() name} of the {@link PlayerProfile}.
     * @param value The value to use for textures.
     * @param signature The signature to use for textures.
     * @return The new {@link PlayerProfile}.
     */
    public static PlayerProfile createProfile(UUID id, String name, String value, String signature) {
        GameProfile profile = new GameProfile(id, name);
        profile.getProperties().put(PROFILE_TEXTURES, new Property(PROFILE_TEXTURES, value, signature));
        return createProfile(profile);
    }

    /**
     * Serialize the specified {@link PlayerProfile} into a CompoundTag.
     *
     * @param profile The {@link PlayerProfile} to serialize.
     * @return {@code tag} after the data has been added to it.
     */
    public static <T> T profileToTag(PlayerProfile profile) {

        TagService<Object, Object> tagService = TagService.INSTANCE;
        Object tag = tagService.createCompoundTag();
        String name = profile.getName();
        if (isNotEmpty(name)) {
            tagService.put(tag, PROFILE_NAME, name);
        }

        tagService.put(tag, PROFILE_ID, profile.getId());
        Set<ProfileProperty> properties = profile.getProperties();
        if (!properties.isEmpty()) {

            Object propertiesTag = tagService.createCompoundTag();
            for (ProfileProperty property : properties) {

                List<Object> valuesTag = tagService.createListTag();
                Object propertyTag = tagService.createCompoundTag();
                tagService.put(propertyTag, PROFILE_PROPERTY_VALUE, property.getValue());
                String signature = property.getSignature();
                if (signature != null) {
                    tagService.put(propertyTag, PROFILE_PROPERTY_SIGNATURE, signature);
                }

                tagService.put(propertiesTag, property.getName(), valuesTag);
            }

            tagService.put(tag, PROFILE_PROPERTIES, propertiesTag);
        }

        return (T) tag;
    }

    /**
     * Create a new {@link PlayerProfile} from the specified CompoundTag.
     *
     * @param tag The CompoundTag to create the {@link PlayerProfile} from.
     * @return The newly created {@linkplain PlayerProfile}.
     * @throws IllegalArgumentException If the data on the tag or the
     *                                  format of the data is incorrect.
     */
    public static PlayerProfile tagToProfile(Object tag) throws IllegalArgumentException {

        TagService<Object, Object> tagService = TagService.INSTANCE;
        UUID id = tagService.getUUID(tag, PROFILE_ID);
        checkArgument(id != null, "invalid compound tag");

        Map<String, Object> tags = tagService.tags(tag);
        GameProfile profile = new GameProfile(id, tagService.getAsString(tags.get(PROFILE_NAME)));
        Object properties = tags.get(PROFILE_PROPERTIES);
        if (properties != null) {

            for (Entry<String, Object> entry : tagService.tags(properties).entrySet()) {

                String key = entry.getKey();
                List<Object> list = (List<Object>) entry.getValue();
                for (Object t : list) {

                    Map<String, Object> compound = tagService.tags(t);
                    String value = tagService.getAsString(compound.get(PROFILE_PROPERTY_VALUE));
                    String signature = tagService.getAsString(compound.get(PROFILE_PROPERTY_SIGNATURE));
                    if (signature != null) {
                        profile.getProperties().put(key, new Property(key, value, signature));
                    } else {
                        profile.getProperties().put(key, new Property(key, value));
                    }
                }
            }
        }

        return createProfile(profile);
    }

    /**
     * Create a new {@link PlayerProfile} from the specified URL.
     *
     * @param url The URL to create the {@link PlayerProfile} from.
     * @return The newly created {@link PlayerProfile}.
     */
    public static PlayerProfile urlToProfile(String url) {
        String json = "{\"" + PROFILE_TEXTURES + "\":{\"SKIN\":{\"url\":\"" + url + "\"}}}";
        return valueToProfile(Base64.getEncoder().encodeToString(json.getBytes(StandardCharsets.UTF_8)));
    }

    /**
     * Create a new {@link PlayerProfile} from the specified
     * base 64 encoded value.
     *
     * @param value The base 64 encoded value.
     * @return The newly created {@link PlayerProfile}.
     */
    public static PlayerProfile valueToProfile(String value) {
        return createProfile(DEFAULT, null, value);
    }
}
