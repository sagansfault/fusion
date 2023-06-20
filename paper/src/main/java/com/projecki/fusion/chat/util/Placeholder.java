package com.projecki.fusion.chat.util;

import java.util.regex.Pattern;

public enum Placeholder {

    MESSAGE("%message%"),
    PLAYER_SENDER("%player_sender%"),
    PLAYER_TARGET("%player_target%"),
    SENDER_ORIGIN_SERVER("%sender_origin_server%"),
    PLAYER_CHAT_TAG("%player_chat_tag%"),
    ;

    public static final Pattern PLACEHOLDER_MATCHER = Pattern.compile("%\\S+%", Pattern.CASE_INSENSITIVE);

    private final String placeholder;

    Placeholder(String placeholder) {
        this.placeholder = placeholder;
    }

    public String getPlaceholder() {
        return placeholder;
    }
}
