package com.projecki.fusion.util;

import org.jetbrains.annotations.NotNull;

import java.nio.ByteBuffer;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Pattern;

import static com.google.common.base.Preconditions.checkArgument;

public final class UUIDUtil {

    /**
     * Regex pattern used to get the groups in a trimmed {@link UUID}
     */
    public static final Pattern TRIMMED_UUID_PATTERN = Pattern.compile("^([a-z0-9]{8})([a-z0-9]{4})([a-z0-9]{4})([a-z0-9]{4})([a-z0-9]{12})$", Pattern.CASE_INSENSITIVE);

    /**
     * Create a new {@code byte} array from the given {@link UUID}.
     * This will return a byte array made up of exactly {@code 16}
     * bytes (two longs) from the {@link UUID#getMostSignificantBits() most}
     * significant bits and the {@link UUID#getLeastSignificantBits() least}
     * significant bits from the UUID.
     *
     * @param uuid The UUID to turn into a byte array.
     * @return The newly create byte array made from the UUID.
     */
    public static byte[] toBytes(UUID uuid) {
        ByteBuffer buf = ByteBuffer.wrap(new byte[16]);
        buf.putLong(uuid.getMostSignificantBits());
        buf.putLong(uuid.getLeastSignificantBits());
        return buf.array();
    }

    /**
     * Create a new {@link UUID} from the given {@code byte} array.
     * The byte array must be exactly {@code 16} bytes in length.
     * <p>
     * It is ideal to use this method reconstruct a UUID from a
     * byte array created using the {@link #toBytes(UUID)} method.
     *
     * @param bytes The byte array to get the UUID from.
     * @return The newly created UUID from the contents of the byte array.
     * @throws IllegalArgumentException If the byte array is not 16
     *                                  bytes in length.
     */
    public static UUID toUuid(byte[] bytes) throws IllegalArgumentException {
        checkArgument(bytes.length == 16, "must have 16 bytes");
        ByteBuffer buf = ByteBuffer.wrap(bytes);
        return new UUID(buf.getLong(), buf.getLong());
    }

    /**
     * Create a {@link UUID} from the specified {@link String}.
     * This method works for both normal UUIDs and trimmed UUIDs (e.g. when retrieved from database)
     *
     * @param input the input string
     *
     * @return an {@link Optional} containing the created {@link UUID}, this {@link Optional} will be empty if anything
     *         goes wrong while trying to create the {@link UUID}
     */
    public static Optional<UUID> createUUID(@NotNull String input) {

        // try to parse the input directly
        try {
            return Optional.of(UUID.fromString(input));
        } catch (IllegalArgumentException e) {

            var matcher = TRIMMED_UUID_PATTERN.matcher(input);
            if (!matcher.matches()) {
                return Optional.empty();
            }

            // Build the uuid string using the matched regex groups
            var builder = new StringBuilder();
            for (int i = 0; i < matcher.groupCount(); i++) {

                // if we're not at the start, append '-'
                if (i != 0) {
                    builder.append("-");
                }

                builder.append(matcher.group(i + 1)); // why is this indexed starting from 1 >:(
            }

            // Should have a valid uuid string here
            return Optional.of(UUID.fromString(builder.toString()));
        }
    }
}
