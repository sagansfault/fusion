package com.projecki.fusion.util;

/**
 * A player's skin and it's associated Mojang signature
 *
 * @param skin      the value of the skin (base64 encoded)
 * @param signature the signature of the skin (base64 encoded)
 */
public record SkinPair(String skin, String signature) {

    private static final SkinPair EMPTY = new SkinPair("", "");

    /**
     * Get a {@link SkinPair} that has an empty skin and signature
     */
    public static SkinPair empty() {
        return EMPTY;
    }
}
