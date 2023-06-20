package com.projecki.fusion.command.base;

import co.aikar.commands.PaperCommandManager;
import com.projecki.fusion.command.CommandIssuer;
import com.projecki.fusion.command.CommonBaseCommand;
import net.kyori.adventure.text.format.TextColor;

/**
 * A Paper implementation of {@link CommonBaseCommand} adds a
 * command context to translate {@link PaperCommandIssuer} to {@link org.bukkit.command.CommandSender}
 */
public class PaperCommonBaseCommand extends CommonBaseCommand {

    /**
     * A new {@link PaperCommonBaseCommand} with the specified colors and a default prefix.
     *
     * @param primaryColor   primary used color, typically a lighter color
     * @param secondaryColor secondary color, typically a bolder, brigther color than {@code primaryColor}
     * @param manager        command this command is registered to
     */
    public PaperCommonBaseCommand(TextColor primaryColor, TextColor secondaryColor, PaperCommandManager manager) {
        super(primaryColor, secondaryColor);
        registerContext(manager);
    }

    /**
     * A new {@link PaperCommonBaseCommand} with the specified colors and prefix.
     * There is no need to color the prefix or pad it on the right, as that is already
     * done.
     *
     * @param primaryColor   primary used color, typically a lighter color
     * @param secondaryColor secondary color, typically a bolder, brigther color than {@code primaryColor}
     * @param prefix         start of all command messages and notifications
     * @param manager        command this command is registered to
     */
    public PaperCommonBaseCommand(TextColor primaryColor, TextColor secondaryColor, String prefix,
                                  PaperCommandManager manager) {
        super(primaryColor, secondaryColor, prefix);
        registerContext(manager);
    }

    private void registerContext(PaperCommandManager manager) {
        manager.getCommandContexts().registerIssuerOnlyContext(CommandIssuer.class,
                con -> new PaperCommandIssuer(con.getSender()));
    }

}
