package com.projecki.fusion.config;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

import java.util.List;

public class MotdContainer {

    private Component motd;

    public Component getMotd() {
        return motd;
    }

    public void setMotd(List<String> motd) {
        this.motd = LegacyComponentSerializer.legacyAmpersand()
                .deserialize(String.join("\n", motd));
    }
}
