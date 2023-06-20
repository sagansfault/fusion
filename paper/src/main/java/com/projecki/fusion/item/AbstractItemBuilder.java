package com.projecki.fusion.item;

import com.destroystokyo.paper.profile.PlayerProfile;
import com.projecki.unversioned.craft.CraftService;
import com.projecki.unversioned.tag.TagService;
import io.papermc.paper.enchantments.EnchantmentRarity;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap.Entry;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Color;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.banner.Pattern;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.entity.EntityCategory;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionData;
import org.bukkit.potion.PotionEffect;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Stream;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;

/**
 * A builder that helps construct complex {@link ItemStack items}.
 *
 * @param <T> The type of the class that extends this one.
 *            Any instance of {@link AbstractItemBuilder} will be of this
 *            type and all builder methods will return this type.
 *            <br>
 *            The purpose is to allow extending {@link AbstractItemBuilder}
 *            and adding functionality, but still returning the
 *            subclass instance.
 */
public abstract class AbstractItemBuilder<T extends AbstractItemBuilder<T>> {

    @Nullable
    public static NamespacedKey GLOW_KEY;
    private static final String DISPLAY_TAG = "display";
    private static final String NAME_TAG = "Name", LORE_TAG = "Lore";
    private static final String BLOCK_ENTITY_TAG = "BlockEntityTag";
    private static final String BASE_TAG = "Base", PATTERNS_TAG = "Patterns";
    private static final String PATTERN_TAG = "Pattern", COLOR_TAG = "Color";

    private Component name;
    private List<Component> preLore, lore, postLore;

    private ItemStack item;
    private Material type;
    private PlayerProfile profile;
    private int amount = 1;

    private Integer damage;
    private Integer modelDataId;

    private Color color;
    private boolean glow, unbreakable;
    private Set<ItemFlag> flags;

    private Object tag;
    private List<PersistentEntry<?, ?>> dataEntries;

    private PotionData potionData;
    private final List<PotionEffect> potionEffects = new ArrayList<>(1);

    private DyeColor baseColor;
    private final List<Pattern> patterns = new ArrayList<>(5);
    private final Object2IntMap<Enchantment> enchants = new Object2IntOpenHashMap<>();

    /**
     * Create a skull item for the given {@link PlayerProfile}.
     *
     * @param profile The profile that the skin should be retrieved from.
     */
    protected AbstractItemBuilder(PlayerProfile profile) {
        this.type = Material.PLAYER_HEAD;
        this.profile = profile;
    }

    /**
     * Create a new item from the attributes of
     * the given item.
     *
     * @param item The items to copy.
     */
    protected AbstractItemBuilder(ItemStack item) {

        this.type = item.getType();
        checkArgument(this.type != Material.AIR, "air type");
        this.item = item;
        // If the item already has lore then we need to cache it
        // in order to allow pre- and post-lore
        if (item.hasItemMeta()) {

            ItemMeta meta = item.getItemMeta();
            if (meta.hasLore()) {
                this.ensureLore().addAll(requireNonNull(meta.lore()));
            }
        }
    }

    /**
     * Create a new item of the given type.
     *
     * @param type The type of item.
     */
    protected AbstractItemBuilder(Material type) {
        checkArgument(type != Material.AIR, "air type");
        this.type = type;
    }

    /**
     * Set the type of the item to be built.
     * <p>
     * If this builder is used to copy an item
     * via the constructor, the type will be set
     * on the clone of the {@link ItemStack}.
     *
     * @param type The type to set to.
     * @return This ItemBuilder.
     */
    public T type(Material type) {
        this.type = type;
        return (T) this;
    }

    /**
     * Set the amount of items to be built.
     *
     * @param amount The amount to set.
     * @return This ItemBuilder.
     */
    public T amount(int amount) {
        this.amount = amount;
        return (T) this;
    }

    /**
     * Add an {@link Enchantment} to the item. If the item
     * already has the given enchantment then it will be
     * overridden with the new value.
     *
     * @param enchant The enchantment to add.
     * @param level   The level of the enchantment to add.
     * @return This ItemBuilder.
     */
    public T enchant(Enchantment enchant, int level) {
        this.enchants.put(enchant, level);
        return (T) this;
    }

    /**
     * @deprecated Use {@link #enchant(Enchantment, int)}
     */
    @Deprecated
    public T enchantment(Enchantment enchantment, int level, boolean force) {
        return this.enchant(enchantment, level);
    }

    /**
     * Set the color that the item should be.
     * This is applicable for leather armor and potion items etc.
     *
     * @param color The color to set the item to.
     * @return This ItemBuilder.
     */
    public T color(Color color) {
        this.color = color;
        return (T) this;
    }

    /**
     * Set the amount of damage the item has taken.
     *
     * @param damage The damage to set to.
     * @return This ItemBuilder.
     */
    public T setDamage(int damage) {
        this.damage = damage;
        return (T) this;
    }

    /**
     * Give the item a custom name.
     *
     * @param name The custom name.
     * @return This ItemBuilder.
     */
    public T name(Component name) {
        this.name = name;
        return (T) this;
    }

    /**
     * Give the item a custom name.
     *
     * @param name The custom name.
     * @return This ItemBuilder.
     */
    public T name(String name) {
        return this.name(componentOf(name));
    }

    /**
     * Give the item a custom name. This name will
     * not be parsed for formats.
     *
     * @param style The {@link Style} to give the name.
     * @param name  The custom name.
     * @return This ItemBuilder.
     */
    public T name(Style style, String name) {
        return this.name(componentOf(name).style(style));
    }

    /**
     * Give the item a custom name. This name will
     * not be parsed for formats.
     *
     * @param color The {@link TextColor} to give the name.
     * @param name  The custom name.
     * @return This ItemBuilder.
     */
    public T name(TextColor color, String name) {
        return this.name(componentOf(name).color(color));
    }

    /**
     * Give the item a custom name. This name will
     * not be parsed for formats.
     *
     * @param color      The {@link TextColor} to give the name.
     * @param decoration The {@link TextDecoration} to give the name.
     * @param name       The custom name.
     * @return This ItemBuilder.
     */
    public T name(TextColor color, TextDecoration decoration, String name) {
        return this.name(Style.style(color, decoration), name);
    }

    /**
     * Give the item a custom name. This name will
     * not be parsed for formats.
     *
     * @param color  The {@link TextColor} to give the name.
     * @param first  The first {@link TextDecoration} to give the name.
     * @param second The second {@link TextDecoration} to give the name.
     * @param name   The custom name.
     * @return This ItemBuilder.
     */
    public T name(TextColor color, TextDecoration first, TextDecoration second, String name) {
        return this.name(Style.style(color, first, second), name);
    }

    /**
     * Set the lore of the item.
     *
     * @param lore The lore to set to.
     * @return This ItemBuilder.
     */
    public T lore(String lore) {
        return this.lore(componentOf(lore));
    }

    /**
     * Set the lore of the item.
     *
     * @param lore The lore to set to.
     * @return This ItemBuilder.
     */
    public T lore(String... lore) {
        return this.lore(componentsOf(lore));
    }

    /**
     * Set the lore of the item.
     *
     * @param lore The lore to set to.
     * @return This ItemBuilder.
     */
    public T lore(Component lore) {
        this.lore = new ArrayList<>(1);
        this.lore.add(lore);
        return (T) this;
    }

    /**
     * Set the lore of the item.
     *
     * @param lore The lore to set to.
     * @return This ItemBuilder.
     */
    public T lore(Component... lore) {
        return this.lore(Arrays.asList(lore));
    }

    /**
     * Set the lore of the item.
     *
     * @param lore The lore to set to.
     * @return This ItemBuilder.
     */
    public T lore(List<Component> lore) {
        this.lore = new ArrayList<>(lore);
        return (T) this;
    }

    /**
     * Add a single line of text to the beginning of
     * the current lore of the item.
     *
     * @param line the line to add.
     * @return This ItemBuilder.
     */
    public T preLore(String line) {
        return this.preLore(componentOf(line));
    }

    /**
     * Add lines of text to the beginning of the
     * current lore of the item.
     *
     * @param lines The lines to add.
     * @return This ItemBuilder.
     */
    public T preLore(String... lines) {
        return this.preLore(componentsOf(lines));
    }

    /**
     * Add a single line of text to the beginning of
     * the current lore of the item.
     *
     * @param line the line to add.
     * @return This ItemBuilder.
     */
    public T preLore(Component line) {
        this.ensurePreLore().add(0, line);
        return (T) this;
    }

    /**
     * Add lines of text to the beginning of the
     * current lore of the item.
     *
     * @param lines The lines to add.
     * @return This ItemBuilder.
     */
    public T preLore(Component... lines) {
        return this.preLore(Arrays.asList(lines));
    }

    /**
     * Add lines of text to the beginning of the
     * current lore of the item.
     *
     * @param lines The lines to add.
     * @return This ItemBuilder.
     */
    public T preLore(List<Component> lines) {
        this.ensurePreLore().addAll(0, lines);
        return (T) this;
    }

    /**
     * Add a single line of text to the end of
     * the current lore of the item.
     *
     * @param line the line to add.
     * @return This ItemBuilder.
     */
    public T postLore(String line) {
        return this.postLore(componentOf(line));
    }

    /**
     * Add lines of text to end of the current lore
     * of the item.
     *
     * @param lines The lines to add.
     * @return This ItemBuilder.
     */
    public T postLore(String... lines) {
        return this.postLore(componentsOf(lines));
    }

    /**
     * Add a single line of text to the end of
     * the current lore of the item.
     *
     * @param line the line to add.
     * @return This ItemBuilder.
     */
    public T postLore(Component line) {
        this.ensurePostLore().add(line);
        return (T) this;
    }

    /**
     * Add lines of text to the end of the
     * current lore of the item.
     *
     * @param lines The lines to add.
     * @return This ItemBuilder.
     */
    public T postLore(Component... lines) {
        return this.postLore(Arrays.asList(lines));
    }

    /**
     * Add lines of text to end of the current lore
     * of the item.
     *
     * @param lines The lines to add.
     * @return This ItemBuilder.
     */
    public T postLore(List<Component> lines) {
        this.ensurePostLore().addAll(lines);
        return (T) this;
    }

    /**
     * Set the {@link ItemFlag flags} of the item.
     *
     * @param flags The {@link ItemFlag ItemFlags} to set.
     * @return This ItemBuilder.
     */
    public T flags(EnumSet<ItemFlag> flags) {
        this.flags = EnumSet.copyOf(flags);
        return (T) this;
    }

    /**
     * Set the {@link ItemFlag flags} of the item.
     *
     * @param flag  The first {@link ItemFlag} to set.
     * @param flags The remaining {@link ItemFlag ItemFlags} to set.
     * @return This ItemBuilder.
     */
    public T flags(ItemFlag flag, ItemFlag... flags) {
        this.flags = EnumSet.of(flag, flags);
        return (T) this;
    }

    /**
     * @deprecated Use {@link #flags(ItemFlag, ItemFlag...)}
     */
    @Deprecated
    public T itemFlags(ItemFlag... itemFlags) {
        return this.flags(EnumSet.copyOf(Arrays.asList(itemFlags)));
    }

    /**
     * Set whether the item is unbreakable or not.
     *
     * @param unbreakable If it is unbreakable.
     * @return This ItemBuilder.
     */
    public T unbreakable(boolean unbreakable) {
        this.unbreakable = unbreakable;
        return (T) this;
    }

    /**
     * Add an enchantment glow to the item.
     *
     * @return This ItemBuilder.
     */
    public T glow() {
        return this.glow(true);
    }

    /**
     * Set whether the item should glow.
     *
     * @param glow If the item should glow.
     * @return This ItemBuilder.
     */
    public T glow(boolean glow) {
        this.glow = glow;
        return (T) this;
    }

    /**
     * Set the base {@link DyeColor} of the item.
     *
     * @return This ItemBuilder.
     */
    public T baseColor(DyeColor baseColor) {
        this.baseColor = baseColor;
        return (T) this;
    }

    /**
     * Add a {@link Pattern} to the item.
     *
     * @param pattern The {@link Pattern} to add.
     * @return This ItemBuilder.
     */
    public T pattern(Pattern pattern) {
        this.patterns.add(pattern);
        return (T) this;
    }

    /**
     * Set {@link PotionData} onto the item.
     * <p>
     * This is only applicable if the item returns a
     * {@link PotionMeta} for its metadata.
     *
     * @param potionData The potion data to set.
     * @return This ItemBuilder.
     */
    public T potionData(PotionData potionData) {
        this.potionData = potionData;
        return (T) this;
    }

    /**
     * Add a {@link PotionEffect} to the item.
     * <p>
     * This is only applicable if the item returns a
     * {@link PotionMeta} for its metadata.
     *
     * @param effect The effect to add.
     * @return This ItemBuilder.
     */
    public T potionEffect(PotionEffect effect) {
        this.potionEffects.add(effect);
        return (T) this;
    }

    /**
     * Set a {@code CustomModelData} ID to the item in
     * order to texture the item according to a resource
     * pack found on the client with a matching ID.
     *
     * @param modelDataId The model ID to add to set on the item.
     * @return This ItemBuilder.
     */
    public T customModelData(int modelDataId) {
        this.modelDataId = modelDataId;
        return (T) this;
    }

    /**
     * Set the given value under the given key on the item's
     * CompoundTag wrapped in its counterpart Tag.
     *
     * @param key   The key to set the tag under.
     * @param value The value to set.
     * @return This ItemBuilder.
     */
    public T tagString(String key, Object value) {
        TagService.INSTANCE.put(this.ensureTag(), key, value);
        return (T) this;
    }

    /**
     * Set the given value under the given key on the item's
     * CompoundTag wrapped in its counterpart Tag.
     *
     * @param key   The key to set the tag under.
     * @param value The value to set.
     * @return This ItemBuilder.
     */
    public T tagString(String key, String value) {
        TagService.INSTANCE.put(this.ensureTag(), key, value);
        return (T) this;
    }

    /**
     * Set the given value under the given key on the item's
     * CompoundTag wrapped in its counterpart Tag.
     *
     * @param key   The key to set the tag under.
     * @param value The value to set.
     * @return This ItemBuilder.
     */
    public T tagByte(String key, byte value) {
        TagService.INSTANCE.put(this.ensureTag(), key, value);
        return (T) this;
    }

    /**
     * Set the given value under the given key on the item's
     * CompoundTag wrapped in its counterpart Tag.
     *
     * @param key   The key to set the tag under.
     * @param value The value to set.
     * @return This ItemBuilder.
     */
    public T tagShort(String key, short value) {
        TagService.INSTANCE.put(this.ensureTag(), key, value);
        return (T) this;
    }

    /**
     * Set the given value under the given key on the item's
     * CompoundTag wrapped in its counterpart Tag.
     *
     * @param key   The key to set the tag under.
     * @param value The value to set.
     * @return This ItemBuilder.
     */
    public T tagInt(String key, int value) {
        TagService.INSTANCE.put(this.ensureTag(), key, value);
        return (T) this;
    }

    /**
     * Set the given value under the given key on the item's
     * CompoundTag wrapped in its counterpart Tag.
     *
     * @param key   The key to set the tag under.
     * @param value The value to set.
     * @return This ItemBuilder.
     */
    public T tagLong(String key, long value) {
        TagService.INSTANCE.put(this.ensureTag(), key, value);
        return (T) this;
    }

    /**
     * Set the given value under the given key on the item's
     * CompoundTag wrapped in its counterpart Tag.
     *
     * @param key   The key to set the tag under.
     * @param value The value to set.
     * @return This ItemBuilder.
     */
    public T tagFloat(String key, float value) {
        TagService.INSTANCE.put(this.ensureTag(), key, value);
        return (T) this;
    }

    /**
     * Set the given value under the given key on the item's
     * CompoundTag wrapped in its counterpart Tag.
     *
     * @param key   The key to set the tag under.
     * @param value The value to set.
     * @return This ItemBuilder.
     */
    public T tagDouble(String key, double value) {
        TagService.INSTANCE.put(this.ensureTag(), key, value);
        return (T) this;
    }

    /**
     * Set the given value under the given key on the item's
     * CompoundTag wrapped in its counterpart Tag.
     *
     * @param key   The key to set the tag under.
     * @param value The value to set.
     * @return This ItemBuilder.
     */
    public T tagBoolean(String key, boolean value) {
        TagService.INSTANCE.put(this.ensureTag(), key, value);
        return (T) this;
    }

    /**
     * Set the given value under the given key on the item's
     * CompoundTag wrapped in its counterpart Tag.
     *
     * @param key   The key to set the tag under.
     * @param value The value to set.
     * @return This ItemBuilder.
     */
    public T tagByteArray(String key, byte[] value) {
        TagService.INSTANCE.put(this.ensureTag(), key, value);
        return (T) this;
    }

    /**
     * Set the given value under the given key on the item's
     * CompoundTag wrapped in its counterpart Tag.
     *
     * @param key   The key to set the tag under.
     * @param value The value to set.
     * @return This ItemBuilder.
     */
    public T tagIntArray(String key, int[] value) {
        TagService.INSTANCE.put(this.ensureTag(), key, value);
        return (T) this;
    }

    /**
     * Set the given value under the given key on the item's
     * CompoundTag wrapped in its counterpart Tag.
     *
     * @param key   The key to set the tag under.
     * @param value The value to set.
     * @return This ItemBuilder.
     */
    public T tagUniqueId(String key, UUID value) {
        TagService.INSTANCE.put(this.ensureTag(), key, value);
        return (T) this;
    }

    /**
     * Determine whether a Tag has been added to
     * this ItemBuilder under the given key.
     *
     * @param key The key to check for.
     * @return If the Tag has been added.
     */
    public boolean hasTag(String key) {
        return tag != null && TagService.INSTANCE.tags(tag).containsKey(key);
    }

    public <TG, Z> T persistentData(@NotNull NamespacedKey key, PersistentDataType<TG, Z> type, Z value) {

        if (dataEntries == null) {
            dataEntries = new ArrayList<>(1);
        }

        this.dataEntries.add(new PersistentEntry<>(key, type, value));
        return (T) this;
    }

    public T nbtInt(@NotNull NamespacedKey key, int value) {
        return this.persistentData(key, PersistentDataType.INTEGER, value);
    }

    public T nbtString(@NotNull NamespacedKey key, @NotNull String value) {
        return this.persistentData(key, PersistentDataType.STRING, value);
    }

    public T nbtBoolean(@NotNull NamespacedKey key, boolean value) {
        return this.persistentData(key, PersistentDataType.BYTE, (byte) (value ? 0 : 1));
    }

    /**
     * @deprecated Use {@link #nbtInt(NamespacedKey, int)} instead, this deprecated version relies on nbt-lib
     * while the new version uses the {@link PersistentDataContainer} api
     */
    @Deprecated
    public T nbtInt(String key, int value) {
        return this.tagInt(key, value);
    }

    /**
     * @deprecated Use {@link #nbtString(NamespacedKey, String)} instead, this deprecated version relies on nbt-lib
     * while the new version uses the {@link PersistentDataContainer} api
     */
    @Deprecated
    public T nbtString(String key, String value) {
        return this.tagString(key, value);
    }

    /**
     * @deprecated Use {@link #nbtBoolean(NamespacedKey, boolean)} instead, this deprecated version relies on nbt-lib
     * while the new version uses the {@link PersistentDataContainer} api
     */
    @Deprecated
    public T nbtBoolean(String key, boolean value) {
        return this.tagBoolean(key, value);
    }

    /**
     * Build the actual {@link ItemStack} with the specifications
     * specified by this builder.
     *
     * @return The newly created ItemStack.
     */
    @SuppressWarnings("deprecation")
    protected ItemStack build() {
        // Create the item and ensure has a Minecraft mirror
        ItemStack item;
        if (this.item != null) {
            // Clone the item
            item = CraftService.INSTANCE.ensureCraftItem(
                    this.item.asQuantity(amount != 1 ? amount : this.item.getAmount()));
            if (type != item.getType()) {
                item.setType(type); // Change the type
            }
        } else {
            item = CraftService.INSTANCE.ensureCraftItem(new ItemStack(type, amount));
        }
        // The base color of a shield
        TagService<Object, Object> tagService = TagService.INSTANCE;
        if (baseColor != null) {
            tagService.put(this.ensureBlockEntityTag(), BASE_TAG, (int) baseColor.getWoolData());
        }
        // Patterns to a banner or shield
        if (!patterns.isEmpty()) {

            List<Object> list = tagService.createListTag();
            for (Pattern pattern : patterns) {
                Object tag = tagService.createCompoundTag();
                tagService.put(tag, COLOR_TAG, (int) pattern.getColor().getWoolData());
                tagService.put(tag, PATTERN_TAG, pattern.getPattern().getIdentifier());
                list.add(tag);
            }

            tagService.put(this.ensureBlockEntityTag(), PATTERNS_TAG, list);
        }
        // Extra tags on the item
        if (tag != null) {
            // Apply tags immediately to allow ItemMeta to take them into account
            Object itemTag = tagService.tag(item);
            if (itemTag != null) {
                tagService.tags(itemTag).putAll(tagService.tags(tag));
            } else {
                tagService.tag(item, tag);
            }
        }
        // Get the item meta after tags are set
        ItemMeta meta = item.getItemMeta();
        // Set the display name
        if (name != null) {
            meta.displayName(removeDefaultItalics(explicitColors(name)));
        }
        // Set the lore
        if (preLore != null || lore != null || postLore != null) {
            List<Component> lore = Stream.of(preLore, this.lore, postLore)
                    .filter(Objects::nonNull)
                    .flatMap(List::stream)
                    .map(AbstractItemBuilder::removeDefaultItalics)
                    .map(AbstractItemBuilder::explicitColors)
                    .toList();
            meta.lore(lore);
        }
        // Add persistent data entries
        if (dataEntries != null) {

            PersistentDataContainer container = meta.getPersistentDataContainer();
            for (PersistentEntry entry : dataEntries) {
                container.set(entry.key(), entry.type(), entry.value());
            }
        }
        // Set the custom model data
        if (modelDataId != null) {
            meta.setCustomModelData(modelDataId);
        }
        // Add item flags
        if (flags != null && !flags.isEmpty()) {
            meta.addItemFlags(flags.toArray(ItemFlag[]::new));
        }
        // Add glow if there are no other enchantments (which already make it glow)
        if (glow && enchants.isEmpty() && !meta.hasEnchants() && GLOW_KEY != null) {

            Enchantment glowEnchant = Enchantment.getByKey(GLOW_KEY);
            if (glowEnchant != null) {
                meta.addEnchant(glowEnchant, 1, true);
            }
        }
        // Set the skull profile
        if (profile != null && meta instanceof SkullMeta skullMeta) {
            skullMeta.setPlayerProfile(profile);
        }
        // Set the damage on the item
        if (damage != null && meta instanceof Damageable damageable) {
            damageable.setDamage(damage);
        }
        // Set the color of leather armor
        if (color != null && meta instanceof LeatherArmorMeta armorMeta) {
            armorMeta.setColor(color);
        }
        // Add potion effects and colors to things like arrows
        if (meta instanceof PotionMeta potionMeta) {

            if (potionData != null) {
                potionMeta.setBasePotionData(potionData);
            }

            if (!potionEffects.isEmpty()) {

                for (PotionEffect effect : potionEffects) {
                    potionMeta.addCustomEffect(effect, false);
                }
            }

            if (color != null) {
                potionMeta.setColor(color);
            }
        }
        // Add enchantments
        if (!enchants.isEmpty()) {

            for (Entry<Enchantment> entry : enchants.object2IntEntrySet()) {
                meta.addEnchant(entry.getKey(), entry.getIntValue(), true);
            }
        }
        // Set whether the item is unbreakable
        meta.setUnbreakable(unbreakable);

        item.setItemMeta(meta);
        return item;
    }

    private Object ensureTag() {
        return tag != null ? tag : (tag = TagService.INSTANCE.createCompoundTag());
    }

    private Object ensureBlockEntityTag() {

        TagService<Object, Object> tagService = TagService.INSTANCE;
        Object tag = this.ensureTag();
        Object blockEntity = tagService.tags(tag).get(BLOCK_ENTITY_TAG);
        if (blockEntity == null) {
            blockEntity = tagService.createCompoundTag();
            tagService.put(tag, BLOCK_ENTITY_TAG, blockEntity);
        }

        return blockEntity;
    }

    private List<Component> ensurePreLore() {
        return preLore != null ? preLore : (preLore = new ArrayList<>());
    }

    private List<Component> ensureLore() {
        return lore != null ? lore : (lore = new ArrayList<>());
    }

    private List<Component> ensurePostLore() {
        return postLore != null ? postLore : (postLore = new ArrayList<>());
    }

    private static Component componentOf(String str) {
        return LegacyComponentSerializer.legacySection().deserialize(str);
    }

    private static List<Component> componentsOf(String... lines) {
        LegacyComponentSerializer serializer = LegacyComponentSerializer.legacySection();
        return Stream.of(lines)
                .map(serializer::deserialize)
                .collect(toList());
    }

    private static List<Component> componentsOf(List<String> lines) {
        LegacyComponentSerializer serializer = LegacyComponentSerializer.legacySection();
        return lines.stream()
                .map(serializer::deserialize)
                .collect(toList());
    }

    private static Component explicitColors(Component component) {
        return component.colorIfAbsent(NamedTextColor.WHITE);
    }

    /**
     * Remove the default italics from the specified component
     *
     * @param component the component to remove the italics from
     * @return the component with default italics removed
     */
    private static Component removeDefaultItalics(@NotNull Component component) {

        if (component.decoration(TextDecoration.ITALIC) == TextDecoration.State.NOT_SET) {
            return component.decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE);
        }

        return component;
    }

    public static void registerGlowEnchantment(Plugin plugin) {

        var enchantKey = new NamespacedKey(plugin, "glow_enchant");
        if (Enchantment.getByKey(enchantKey) != null) {
            return;
        }

        // Register glow enchantment
        try {

            // Make sure field is accessible
            Field field = Enchantment.class.getDeclaredField("acceptingNew");
            field.setAccessible(true);

            // Set the field to true
            field.set(null, true);

            // Register the enchantment
            Enchantment.registerEnchantment(new GlowEnchantment(enchantKey));

            AbstractItemBuilder.GLOW_KEY = enchantKey;

            // Make sure to reset the field
            field.set(null, false);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    private record PersistentEntry<T, Z>(@NotNull NamespacedKey key, PersistentDataType<T, Z> type, Z value) {
    }

    public static final class GlowEnchantment extends Enchantment {

        public GlowEnchantment(@NotNull NamespacedKey key) {
            super(key);
        }

        @Override
        public @NotNull String getName() {
            return "glow";
        }

        @Override
        public int getMaxLevel() {
            return 0;
        }

        @Override
        public int getStartLevel() {
            return 0;
        }

        @SuppressWarnings("deprecation")
        @Override
        public @NotNull EnchantmentTarget getItemTarget() {
            return EnchantmentTarget.ALL; // praying this works;
        }

        @Override
        public boolean isTreasure() {
            return false;
        }

        @Override
        public boolean isCursed() {
            return false;
        }

        @Override
        public boolean conflictsWith(@NotNull Enchantment other) {
            return false;
        }

        @Override
        public boolean canEnchantItem(@NotNull ItemStack item) {
            return false;
        }

        @Override
        public @NotNull Component displayName(int level) {
            return Component.text("glow");
        }

        @Override
        public boolean isTradeable() {
            return false;
        }

        @Override
        public boolean isDiscoverable() {
            return false;
        }

        @Override
        public @NotNull EnchantmentRarity getRarity() {
            return EnchantmentRarity.COMMON;
        }

        @Override
        public float getDamageIncrease(int level, @NotNull EntityCategory entityCategory) {
            return 0;
        }

        @Override
        public @NotNull Set<EquipmentSlot> getActiveSlots() {
            return Set.of(EquipmentSlot.HEAD);
        }

        @Override
        public @NotNull String translationKey() {
            return "projecki:glow";
        }
    }
}
