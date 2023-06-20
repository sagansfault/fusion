package com.projecki.fusion.statistic.impl;

import com.projecki.fusion.statistic.StatisticType;

public enum SquidgameStatistic implements StatisticType {

    PARTICIPATION("participation"),
    ROUNDS_SURVIVED("rounds_survived"),
    GAMES_WON("games_won");

    private final String id;

    SquidgameStatistic(String id) {
        this.id = id;
    }

    @Override
    public String getId() {
        return id;
    }
}
