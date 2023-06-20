package com.projecki.fusion.statistic.impl;

import com.projecki.fusion.statistic.StatisticType;

public enum NetworkWideStatistic implements StatisticType {

    PLAY_TIME("play_time")
    ;

    private final String id;

    NetworkWideStatistic(String id) {
        this.id = id;
    }

    @Override
    public String getId() {
        return this.id;
    }
}
