package com.projecki.fusion.util;

/**
 * Message that is different when viewed from either bedrock or java
 */
public class PlatformSpecificMessage {

    private String java;
    private String bedrock;

    public PlatformSpecificMessage() {}

    /**
     * Message that is different when viewed from either bedrock or java
     *
     * @param javaMessage    message as it should be viewed for java clients
     * @param bedrockMessage message as it should be viewed for bedrock clients
     */
    public PlatformSpecificMessage(String javaMessage, String bedrockMessage) {
        this.java = javaMessage;
        this.bedrock = bedrockMessage;
    }

    /**
     * Get the message as it should be viewed for java clients
     *
     * @return message for java clients
     */
    public String getJavaMessage() {
        return java;
    }

    /**
     * Get the message as it should be viewed for bedrock clients
     *
     * @return message for bedrock clients
     */
    public String getBedrockMessage() {
        return bedrock;
    }
}
