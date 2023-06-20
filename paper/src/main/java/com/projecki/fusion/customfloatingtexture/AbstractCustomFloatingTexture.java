package com.projecki.fusion.customfloatingtexture;

import org.bukkit.Location;

/**
 * Represents a custom floating texture, which is just a way to display a custom texture (custom model data) at a given
 * location in the world. In most cases of the implementations, this is done with an item with custom model data on the
 * head piece of an invisible armor stand.
 */
public abstract class AbstractCustomFloatingTexture {

    protected final Location origin;
    protected final int rotation;
    protected int modelData;

    /**
     * Constructs a new custom floating texture
     *
     * @param origin The location at which to place this texture (offset may vary, test)
     * @param rotation The rotation angle of this texture in degrees. This texture does not follow the players gaze to remain
     *                 perpendicular with it at all times; it maintains its angle
     * @param modelData The custom model data
     */
    public AbstractCustomFloatingTexture(Location origin, int rotation, int modelData) {
        this.origin = origin;
        this.rotation = rotation;
        this.modelData = modelData;
    }

    /**
     * Set the custom model data on this floating texture and update the textures to reflect this change
     *
     * @param modelData The new custom model data
     */
    public void updateCustomTexture(int modelData) {
        this.modelData = modelData;
        this.onCustomTextureUpdate(modelData);
    }

    protected abstract void onCustomTextureUpdate(int modelData);

    /**
     * Deletes this custom floating texture
     */
    public abstract void delete();

    public Location getOrigin() {
        return origin;
    }

    public int getRotation() {
        return rotation;
    }

    public int getModelData() {
        return modelData;
    }
}
