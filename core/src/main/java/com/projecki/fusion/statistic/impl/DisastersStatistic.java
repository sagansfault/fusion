package com.projecki.fusion.statistic.impl;

import com.projecki.fusion.statistic.StatisticType;

public enum DisastersStatistic implements StatisticType {

    PLAYED("played"),
    WON("won"),

    ROUNDS_PLAYED("rounds_played"),
    ROUNDS_PLAYED_APOCALYPSE("rounds_played_apocalypse"),

    TIME_SURVIVED("time_survived"),
    TIME_SURVIVED_APOCALYPSE("time_survived_apocalypse"),

    LONGEST_SURVIVED("longest_survived");

    private final String id;

    DisastersStatistic(String id) {
        this.id = id;
    }

    @Override
    public String getId() {
        return id;
    }

}
