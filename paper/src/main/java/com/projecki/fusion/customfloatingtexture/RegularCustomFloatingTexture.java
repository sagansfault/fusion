package com.projecki.fusion.customfloatingtexture;

import com.projecki.fusion.item.ItemBuilder;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;

public class RegularCustomFloatingTexture extends AbstractCustomFloatingTexture {

    private final ArmorStand stand;

    public RegularCustomFloatingTexture(Location origin, int rotation, int modelData) {
        super(origin, rotation, modelData);

        stand = (ArmorStand) origin.getWorld().spawnEntity(origin, EntityType.ARMOR_STAND);
        stand.setInvisible(true);
        stand.setGravity(false);

        stand.setHeadPose(stand.getHeadPose().add(0, rotation * Math.PI / 180, 0));
        ItemStack item = ItemBuilder.of(Material.PAPER).customModelData(modelData).build();
        stand.getEquipment().setHelmet(item);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onCustomTextureUpdate(int modelData) {
        stand.getEquipment().setHelmet(ItemBuilder.of(Material.PAPER).customModelData(modelData).build());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void delete() {
        this.stand.remove();
    }
}
