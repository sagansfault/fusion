package com.projecki.fusion.item;

import com.google.common.collect.ImmutableSet;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.stream.Collectors;

public class ItemHash {

    /**
     * All types of persistent data since persistent data without knowing the type, so we check all types
     */
    private static final ImmutableSet<PersistentDataType<?, ?>> NBT_TYPES = ImmutableSet.of(
            PersistentDataType.STRING, PersistentDataType.DOUBLE, PersistentDataType.FLOAT,
            PersistentDataType.BYTE, PersistentDataType.SHORT, PersistentDataType.LONG, PersistentDataType.INTEGER,
            PersistentDataType.BYTE_ARRAY, PersistentDataType.LONG_ARRAY, PersistentDataType.INTEGER_ARRAY,
            PersistentDataType.TAG_CONTAINER_ARRAY, PersistentDataType.TAG_CONTAINER);

    /**
     * Compute the hash for the specified {@link ItemStack}.
     * The hash is computed based on the following attributes:
     * <ul>
     *     <li>type</li>
     *     <li>durability</li>
     *     <li>display name</li>
     *     <li>lore</li>
     *     <li>enchantments</li>
     *     <li>item flags</li>
     *     <li>nbt data using {@link PersistentDataContainer}</li>
     * </ul>
     * <p>
     * This "algorithm" should probably be kept a secret lol
     *
     * @param itemStack the item-stack to compute the hash for
     *
     * @return the hash of the item stack represented as hex
     */
    public static String getHexItemHash(@NotNull ItemStack itemStack) {
        return getHexItemHash(itemStack, false);
    }

    /**
     * Compute the hash for the specified {@link ItemStack}.
     * The hash is computed based on the following attributes:
     * <ul>
     *     <li>type</li>
     *     <li>the amount of the item if enabled</li>
     *     <li>durability</li>
     *     <li>display name</li>
     *     <li>lore</li>
     *     <li>enchantments</li>
     *     <li>item flags</li>
     *     <li>nbt data using {@link PersistentDataContainer}</li>
     * </ul>
     *
     * This "algorithm" should probably be kept a secret lol
     *
     * @param itemStack  the item-stack to compute the hash for
     * @param hashAmount whether to use the item amount in the hash
     *
     * @return the hash of the item stack represented as hex
     */
    public static String getHexItemHash(@NotNull ItemStack itemStack, boolean hashAmount) {

        // use the sha256 algorithm to compute a hash, this hash is then converted to a hex string
        return buildHash(itemStack, hashAmount).getHex();
    }


    /**
     * Compute the hash for the specified {@link ItemStack}.
     * The hash is computed based on the following attributes:
     * <ul>
     *     <li>type</li>
     *     <li>durability</li>
     *     <li>display name</li>
     *     <li>lore</li>
     *     <li>enchantments</li>
     *     <li>item flags</li>
     *     <li>nbt data using {@link PersistentDataContainer}</li>
     * </ul>
     *
     * This "algorithm" should probably be kept a secret lol
     *
     * @param itemStack  the item-stack to compute the hash for
     *
     * @return the hash of the item stack as a raw byte array
     */
    public static byte[] hashItem (@NotNull ItemStack itemStack) {
        return hashItem(itemStack, false);
    }

    /**
     * Compute the hash for the specified {@link ItemStack}.
     * The hash is computed based on the following attributes:
     * <ul>
     *     <li>type</li>
     *     <li>the amount of the item if enabled</li>
     *     <li>durability</li>
     *     <li>display name</li>
     *     <li>lore</li>
     *     <li>enchantments</li>
     *     <li>item flags</li>
     *     <li>nbt data using {@link PersistentDataContainer}</li>
     * </ul>
     *
     * This "algorithm" should probably be kept a secret lol
     *
     * @param itemStack  the item-stack to compute the hash for
     * @param hashAmount whether to use the item amount in the hash
     *
     * @return the hash of the item stack as a raw byte array
     */
    public static byte[] hashItem(@NotNull ItemStack itemStack, boolean hashAmount) {
        return buildHash(itemStack, hashAmount).getHash();
    }

    /**
     * Build the hash for the specified {@link ItemStack}.
     * The hash is computed based on the following attributes:
     * <ul>
     *     <li>type</li>
     *     <li>the amount of the item if enabled</li>
     *     <li>durability</li>
     *     <li>display name</li>
     *     <li>lore</li>
     *     <li>enchantments</li>
     *     <li>item flags</li>
     *     <li>nbt data using {@link PersistentDataContainer}</li>
     * </ul>
     *
     * This "algorithm" should probably be kept a secret lol
     * @param itemStack  the item-stack to compute the hash for
     * @param hashAmount whether to use the item amount in the hash
     *
     * @return the {@link HashBuilder} instance that contains the fully built hash input string
     */
    private static HashBuilder buildHash (@NotNull ItemStack itemStack, boolean hashAmount) {
        var itemMeta = itemStack.getItemMeta();
        var hashBuilder = new HashBuilder();

        // start building the "hash"
        // the order is extremely important, as hashes will be completely different
        // if it changes!

        // add type
        hashBuilder.append("type", itemStack.getType());

        // add amount if enabled
        if (hashAmount) {
            hashBuilder.append("amount", itemStack.getAmount());
        }

        // if the item doesn't have any item meta, we can just skip this step
        if (itemMeta != null) {

            // add the item's durability
            hashBuilder.append("item_meta.damage", itemStack.getDurability());

            // add the display name if the item has one
            if (itemMeta.hasDisplayName()) {
                hashBuilder.append("item_meta.name", itemMeta.getDisplayName());
            }

            // add the lore if the item has it
            if (itemMeta.hasLore()) {
                hashBuilder.append("item_meta.lore", String.join(",", itemMeta.getLore()));
            }

            // if the item has enchants, add em to the hash
            if (itemMeta.hasEnchants()) {

                // we loop over the Enchantment.values() array to ensure the enchantments
                // will always show up in the same order regardless of their actual order on the item
                var enchants = Arrays.stream(Enchantment.values())
                        .filter(itemMeta::hasEnchant)
                        .map(enchant -> String.format("%s.%s", enchant.getKey(), itemMeta.getEnchantLevel(enchant)))
                        .collect(Collectors.joining(","));

                hashBuilder.append("item_meta.enchantments", enchants);
            }

            // we loop over the ItemFlag.values() array to ensure the flags
            // will always show up in the same order regardless of their actual order on the item
            var flags = Arrays.stream(ItemFlag.values())
                    .filter(itemMeta::hasItemFlag)
                    .map(ItemFlag::toString)
                    .collect(Collectors.joining(","));

            // make sure there's flags to add
            if (!flags.equals("")) {
                hashBuilder.append("item_meta.flags", flags);
            }

            // add all the persistent data
            for (NamespacedKey key : itemMeta.getPersistentDataContainer().getKeys()) {
                for (PersistentDataType<?, ?> type : NBT_TYPES) {
                    if (itemMeta.getPersistentDataContainer().has(key, type)) {
                        hashBuilder.append(key.toString(), itemMeta.getPersistentDataContainer().get(key, type));
                        break;
                    }
                }
            }
        }

        return hashBuilder;
    }

    /**
     * The hash builder utility class that takes care of building out the hash and computing the actual hash
     */
    private static final class HashBuilder {

        /**
         * sha256 instance used to compute the hashes
         */
        private static MessageDigest sha256;

        /**
         * a byte array that contains all byte values for possible hexadecimal characters
         */
        private static final byte[] HEX = "0123456789abcdef".getBytes(StandardCharsets.US_ASCII);

        static {
            try {
                sha256 = MessageDigest.getInstance("SHA-256");
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            }
        }

        /**
         * The {@link StringBuilder} that's used to create the string that's used in the sha256 algorithm
         */
        private final StringBuilder hashString = new StringBuilder();

        /**
         * Append the specified key and value to the hash input string.
         * This will add the key and value to the end of the string in the following format:
         * <strong>{key}:{value};</strong>
         *
         * @param key   the key of the value that's being appended to the hash input string
         * @param value the value that's being appended to the hash input string
         */
        public void append(@NotNull String key, @NotNull Object value) {
            hashString.append(key.trim())
                    .append(":")
                    .append(value.toString().trim())
                    .append(";");
        }

        /**
         * Apply the sha256 algorithm to the input string, and reset the sha256 instance for further use.
         *
         * @return an array of bytes computed using sha256 based on the input string
         */
        public byte[] getHash() {
            var input = hashString.toString();

            // hash the input
            var hash = sha256.digest(input.getBytes());
            sha256.reset(); // reset sha256 for further use

            return hash;
        }

        /**
         * Get the hash as a hex string.
         * This will compute the hash and covert it to a hex string.
         *
         * @return the hash represented as a hex string
         */
        public String getHex() {
            var hash = getHash();
            var hexChars = new byte[hash.length * 2]; // 2 hex chars per byte

            for (int i = 0; i < hash.length; i++) {

                // 0xFF here because a byte (8 bits) is unsigned
                // since indices are always integers (32 bits), we'll need the extra F
                // to make sure the sign bit and other bits aren't set, as the index needs
                // non-negative
                int index = hash[i] & 0xFF;

                // 8 bit byte, so we shift by 4 to use the msb for the first character
                hexChars[i * 2] = HEX[index >>> 4];

                // use lsb for the 2nd character
                hexChars[i * 2 + 1] = HEX[index & 0x0F];
            }

            return new String(hexChars, StandardCharsets.UTF_8);
        }
    }
}
