package com.projecki.fusion.button;

import com.projecki.fusion.customfloatingtexture.RegularCustomFloatingTexture;
import org.bukkit.plugin.java.JavaPlugin;

public class RegularButton extends AbstractButton {

    public RegularButton(JavaPlugin plugin,
                         RegularCustomFloatingTexture floatingTexture,
                         float textureWidth,
                         float textureHeight,
                         double range) {
        super(plugin, floatingTexture, textureWidth, textureHeight, range);
    }
}
