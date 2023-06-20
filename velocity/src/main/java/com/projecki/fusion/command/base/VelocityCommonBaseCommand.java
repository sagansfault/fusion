package com.projecki.fusion.command.base;

import co.aikar.commands.VelocityCommandManager;
import com.projecki.fusion.command.CommandIssuer;
import com.projecki.fusion.command.CommonBaseCommand;
import com.velocitypowered.api.command.CommandSource;
import net.kyori.adventure.text.format.TextColor;

/**
 * A velocity implementation of {@link CommonBaseCommand} adds a
 * command context to translate {@link VelocityCommandIssuer} to {@link CommandSource}
 */
public class VelocityCommonBaseCommand extends CommonBaseCommand {

    /**
     * A new {@link VelocityCommonBaseCommand} with the specified colors and a default prefix.
     *
     * @param primaryColor   primary used color, typically a lighter color
     * @param secondaryColor secondary color, typically a bolder, brigther color than {@code primaryColor}
     * @param manager        command this command is registered to
     */
    public VelocityCommonBaseCommand(TextColor primaryColor, TextColor secondaryColor, VelocityCommandManager manager) {
        super(primaryColor, secondaryColor);
        registerManager(manager);
    }

    /**
     * A new {@link VelocityCommonBaseCommand} with the specified colors and prefix.
     * There is no need to color the prefix or pad it on the right, as that is already
     * done.
     *
     * @param primaryColor   primary used color, typically a lighter color
     * @param secondaryColor secondary color, typically a bolder, brigther color than {@code primaryColor}
     * @param prefix         start of all command messages and notifications
     * @param manager        command this command is registered to
     */
    public VelocityCommonBaseCommand(TextColor primaryColor, TextColor secondaryColor, String prefix,
                                     VelocityCommandManager manager) {
        super(primaryColor, secondaryColor, prefix);
        registerManager(manager);
    }

    private void registerManager(VelocityCommandManager manager) {
        manager.getCommandContexts().registerIssuerOnlyContext(CommandIssuer.class,
                con -> new VelocityCommandIssuer(con.getSender()));
    }
}
