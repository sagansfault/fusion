package com.projecki.fusion.config.serialize;

import com.projecki.fusion.serializer.formatted.FormattedSerializer;

import java.util.Optional;

/**
 * @deprecated Use {@link FormattedSerializer}
 */
@Deprecated
public abstract class Serializer<T> {

    protected final Class<T> targetType;

    public Serializer(Class<T> targetType) {
        this.targetType = targetType;
    }

    /**
     * Serialises this type to its string form. Check lower implementations for serialization types.
     *
     * @param object The object to serialize
     * @return the serialized string
     */
    public abstract String serialize(T object);

    /**
     * Returns an optional potentially containing the reconstructed, deserialized object of this type. Whether a full
     * or empty optional is returned is up to the lower implementations. Generally, if the object could not be
     * deserialized, an empty optional is returned.
     *
     * @param s The string to deserialize
     * @return An optional potentially containing a deserialized, reconstructed object of this type.
     */
    public abstract Optional<T> deserialize(String s);

    /**
     * Returns the extension name for this type of file (without the dot). "json" for Json files, "yml" for YAML files
     * etc. Check the lower implementations for intricacies.
     *
     * @return The extension name for this type of file without the dot.
     */
    public abstract String getExtension();
}
