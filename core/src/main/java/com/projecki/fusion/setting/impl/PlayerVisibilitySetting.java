package com.projecki.fusion.setting.impl;

import com.projecki.fusion.setting.Setting;

public enum PlayerVisibilitySetting implements Setting {

    ALL("all"),
    NONE("none"),
    ;

    private final String id;

    PlayerVisibilitySetting(String id) {
        this.id = id;
    }

    @Override
    public String getId() {
        return this.id;
    }
}
