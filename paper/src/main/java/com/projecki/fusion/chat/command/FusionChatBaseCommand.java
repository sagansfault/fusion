package com.projecki.fusion.chat.command;

import com.projecki.fusion.FusionPaper;
import com.projecki.fusion.chat.pipeline.ChatPipeline;
import com.projecki.fusion.command.base.PaperCommonBaseCommand;
import net.kyori.adventure.text.format.TextColor;

public class FusionChatBaseCommand extends PaperCommonBaseCommand {

    public static final TextColor PRIMARY = TextColor.fromHexString("#9574cf");
    public static final TextColor SECONDARY = TextColor.fromHexString("#7854b8");

    protected final ChatPipeline chatPipeline;

    public FusionChatBaseCommand(ChatPipeline chatPipeline) {
        super(PRIMARY, SECONDARY, FusionPaper.getCommandManager());
        this.chatPipeline = chatPipeline;
    }
}
