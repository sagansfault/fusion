package com.projecki.fusion.serializer.itemstack;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionEffect;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public class ItemStackSerializer extends StdSerializer<ItemStack> {

    public ItemStackSerializer() {
        this(null);
    }

    protected ItemStackSerializer(Class<ItemStack> t) {
        super(t);
    }

    @Override
    public void serialize(ItemStack itemStack, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        jsonGenerator.writeStartObject();
        jsonGenerator.writeStringField("material", itemStack.getType().name());
        jsonGenerator.writeNumberField("amount", itemStack.getAmount());

        ItemMeta meta = itemStack.getItemMeta();
        Component displayName = meta.displayName();
        if (displayName != null) {
            jsonGenerator.writeStringField("name", LegacyComponentSerializer.legacyAmpersand().serialize(displayName));
        }
        List<Component> lore = meta.lore();
        if (lore != null) {
            jsonGenerator.writeFieldName("lore");
            jsonGenerator.writeStartArray();
            for (Component component : lore) {
                jsonGenerator.writeString(LegacyComponentSerializer.legacyAmpersand().serialize(component));
            }
            jsonGenerator.writeEndArray();
        }

        Map<Enchantment, Integer> enchantments = itemStack.getEnchantments();
        if (!enchantments.isEmpty()) {
            jsonGenerator.writeFieldName("enchantments");
            jsonGenerator.writeStartObject();
            for (Map.Entry<Enchantment, Integer> entry : enchantments.entrySet()) {
                Enchantment enchantment = entry.getKey();
                int level = entry.getValue();
                jsonGenerator.writeNumberField(enchantment.getKey().getKey(), level);
            }
            jsonGenerator.writeEndObject();
        }

        if (meta instanceof PotionMeta potionMeta) {
            List<PotionEffect> effects = potionMeta.getCustomEffects();
            if (!effects.isEmpty()) {
                jsonGenerator.writeFieldName("effects");
                jsonGenerator.writeStartArray();
                for (PotionEffect effect : effects) {
                    jsonGenerator.writeStartObject();
                    jsonGenerator.writeStringField("type", effect.getType().getName());
                    jsonGenerator.writeNumberField("amplifier", effect.getAmplifier());
                    jsonGenerator.writeNumberField("duration", effect.getDuration());
                    jsonGenerator.writeBooleanField("ambient", effect.isAmbient());
                    jsonGenerator.writeBooleanField("particles", effect.hasParticles());
                    jsonGenerator.writeBooleanField("icon", effect.hasIcon());
                    jsonGenerator.writeEndObject();
                }
                jsonGenerator.writeEndArray();
            }
        }

        jsonGenerator.writeEndObject();
    }
}
