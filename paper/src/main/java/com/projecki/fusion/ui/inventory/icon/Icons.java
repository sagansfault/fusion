package com.projecki.fusion.ui.inventory.icon;

import com.projecki.fusion.util.ProfileUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;

/**
 * @since April 09, 2022
 * @author Andavin
 */
public final class Icons {

    /**
     * A basic icon with a blank name (i.e. just a color).
     *
     * @see UnmodifiableIcon
     */
    public static final Icon WHITE_STAINED_GLASS_PANE = UnmodifiableIcon.of(Material.WHITE_STAINED_GLASS_PANE)
            .name(Component.empty())
            .buildIcon();

    /**
     * A basic icon with a blank name (i.e. just a color).
     *
     * @see UnmodifiableIcon
     */
    public static final Icon BLACK_STAINED_GLASS_PANE = UnmodifiableIcon.of(Material.BLACK_STAINED_GLASS_PANE)
            .name(Component.empty())
            .buildIcon();

    /**
     * A basic icon with a blank name (i.e. just a color).
     *
     * @see UnmodifiableIcon
     */
    public static final Icon RED_STAINED_GLASS_PANE = UnmodifiableIcon.of(Material.RED_STAINED_GLASS_PANE)
            .name(Component.empty())
            .buildIcon();

    /**
     * A basic arrow head that points left and is named
     * yellow {@code < Back}.
     *
     * @see UnmodifiableIcon
     */
    public static final Icon BACK_ARROW = UnmodifiableIcon.of(ProfileUtil.urlToProfile("https://textures.minecraft.net/texture/a28f7bb33f76b709b8b8c0dc86db77358e6e9018f413ef9116efbac67307f36e"))
            .name(NamedTextColor.YELLOW, "< Back")
            .customModelData(100000)
            .buildIcon();

    /**
     * A basic arrow head that points right and is named
     * yellow {@code Next >}.
     *
     * @see UnmodifiableIcon
     */
    public static final Icon NEXT_ARROW = UnmodifiableIcon.of(ProfileUtil.urlToProfile("https://textures.minecraft.net/texture/af382345fd79fc78361c61c96230c694c34479358ac0ae49c4db7a624a5afe54"))
            .name(NamedTextColor.YELLOW, "Next >")
            .customModelData(100001)
            .buildIcon();

    /**
     * A basic arrow head that points down.
     *
     * @see UnmodifiableIcon
     */
    public static final Icon DOWN_ARROW = UnmodifiableIcon.of(ProfileUtil.urlToProfile("https://textures.minecraft.net/texture/94274835fd6d1a8ba342fced867a23fa585620319e412c02cb7409b9d897ac9"))
            .customModelData(100002)
            .buildIcon();

    /**
     * A basic head that represents a minus sign
     * for removing.
     *
     * @see UnmodifiableIcon
     */
    public static final Icon MINUS = UnmodifiableIcon.of(ProfileUtil.urlToProfile("https://textures.minecraft.net/texture/a6b7e3e8031fd2b19ae5e8959e50ae3b3c3643ab6fc704a13b91ffc544843025"))
            .name(NamedTextColor.RED, "Remove")
            .customModelData(100004)
            .buildIcon();

    /**
     * A basic head that represents a plus sign
     * for adding.
     *
     * @see UnmodifiableIcon
     */
    public static final Icon PLUS = UnmodifiableIcon.of(ProfileUtil.urlToProfile("https://textures.minecraft.net/texture/c28fa434e0493a49945f29122a1a8b6bb2f88c86d31a67512474a18f7cde4449"))
            .name(NamedTextColor.GREEN, "Add")
            .customModelData(100005)
            .buildIcon();
}
