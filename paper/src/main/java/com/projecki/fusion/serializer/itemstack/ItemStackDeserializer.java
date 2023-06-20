package com.projecki.fusion.serializer.itemstack;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.projecki.fusion.item.ItemBuilder;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

public class ItemStackDeserializer extends StdDeserializer<ItemStack> {

    public ItemStackDeserializer() {
        this(null);
    }

    protected ItemStackDeserializer(Class<?> vc) {
        super(vc);
    }

    @Override
    public ItemStack deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException, JacksonException {
        JsonNode root = jsonParser.getCodec().readTree(jsonParser);
        return deserializeFromRoot(root);
    }

    /**
     * Takes a {@link JsonNode} and assuming that is the root of a serialized {@link ItemStack}, parses values and
     * wraps as an {@link ItemStack}
     *
     * @param root the serialized root node for this object
     * @return the serialized ItemStack
     * @throws ItemStackDeserializationException in the event of a serialization error
     */
    public static ItemStack deserializeFromRoot(JsonNode root) throws ItemStackDeserializationException {
        String materialName = root.get("material").textValue().toUpperCase(Locale.ROOT);

        Material material = Material.getMaterial(materialName);
        if (material == null) {
            throw new ItemStackDeserializationException("Could not find material with name present");
        }

        ItemBuilder builder = ItemBuilder.of(material);

        int amount = Optional.ofNullable(root.get("amount")).map(JsonNode::intValue).orElse(1);
        builder.amount(amount);

        if (root.has("name")) {
            Component name = LegacyComponentSerializer.legacyAmpersand().deserialize(root.get("name").textValue());
            builder.name(name);
        }

        if (root.has("lore")) {
            JsonNode loreNode = root.get("lore");
            if (loreNode.isArray()) {
                List<Component> loreComponents = new ArrayList<>();
                for (JsonNode lore : loreNode) {
                    loreComponents.add(LegacyComponentSerializer.legacyAmpersand().deserialize(lore.textValue()));
                }
                builder.lore(loreComponents);
            }
        }

        if (root.has("enchantments")) {
            JsonNode child = root.get("enchantments");
            child.fieldNames().forEachRemaining(s -> {
                Enchantment ench = Enchantment.getByKey(NamespacedKey.minecraft(s));
                if (ench == null) {
                    return;
                }
                int level = child.get(s).intValue();
                builder.enchant(ench, level);
            });
        }


        if (root.has("effects")) {
            JsonNode effectsNode = root.get("effects");
            if (effectsNode.isArray()) {
                for (JsonNode effect : effectsNode) {
                    String name = effect.get("type").textValue();
                    int amplifier = effect.get("amplifier").intValue();
                    int duration = effect.get("duration").intValue();
                    boolean ambient = Optional.ofNullable(effect.get("ambient")).map(JsonNode::booleanValue).orElse(false);
                    boolean particles = Optional.ofNullable(effect.get("particles")).map(JsonNode::booleanValue).orElse(false);
                    boolean icon = Optional.ofNullable(effect.get("icon")).map(JsonNode::booleanValue).orElse(false);

                    PotionEffectType type = PotionEffectType.getByName(name);
                    if (type == null) {
                        throw new ItemStackDeserializationException("Potion effect type not found");
                    }

                    PotionEffect potionEffect = new PotionEffect(type, duration, amplifier, ambient, particles, icon);
                    builder.potionEffect(potionEffect);
                }
            }
        }

        return builder.build();
    }

    private static final class ItemStackDeserializationException extends JsonProcessingException {
        public ItemStackDeserializationException(String msg) {
            super(msg);
        }
    }
}
