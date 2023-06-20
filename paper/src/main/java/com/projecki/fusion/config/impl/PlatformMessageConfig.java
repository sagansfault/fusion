package com.projecki.fusion.config.impl;

import com.projecki.fusion.util.PlatformSpecificMessage;

import java.util.Map;

public class PlatformMessageConfig {

    private Map<String, PlatformSpecificMessage> messages;

    public PlatformMessageConfig() {
    }

    public PlatformMessageConfig(Map<String, PlatformSpecificMessage> messages) {
        this.messages = messages;
    }

    public Map<String, PlatformSpecificMessage> getMessages() {
        return messages;
    }
}
