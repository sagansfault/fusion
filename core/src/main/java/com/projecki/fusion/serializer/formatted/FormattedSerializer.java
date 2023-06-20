package com.projecki.fusion.serializer.formatted;

/**
 * Represents a generic implementation of a string serializer. This allows for a less-safe, but more versatile
 * de/serializer. This does not force a generic type parameter but rather all types are taken in through the
 * de/serialize methods.
 */
public interface FormattedSerializer {

    /**
     * Serializes this type to its string form. Check lower implementations for serialization types.
     *
     * @param obj The object to serialize
     * @return the serialized string
     */
    String serialize(Object obj);

    /**
     * Returns an optional potentially containing the reconstructed, deserialized object of this type. Whether a full
     * or empty optional is returned is up to the lower implementations. Generally, if the object could not be
     * deserialized, an empty optional is returned.
     *
     * @param type The type to deserialize to. Again, this isn't enforced on this serializer class but rather only on
     *             these methods so be sure your types are correct!
     * @param input The string to deserialize
     * @return A deserialized, reconstructed object of this type.
     */
    <T> T deserialize(Class<T> type, String input);
}
