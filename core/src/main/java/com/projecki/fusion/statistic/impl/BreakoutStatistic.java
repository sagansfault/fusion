package com.projecki.fusion.statistic.impl;

import com.projecki.fusion.statistic.StatisticType;

public enum BreakoutStatistic implements StatisticType {

    PLAYED("played"),
    PLAYED_PRISONER("played_prisoner"),
    PLAYED_GUARD("played_guard"),

    SURVIVED("survived"),
    SURVIVED_FIRST("survived_first"),

    WON_AS_GUARD("won_as_guard"),
    PRISONERS_ELIMINATED("prisoners_eliminated"),

    ROUNDS_PASSED("rounds_passed"),
    ELIMINATED("eliminated"),

    TIME_IN_BED("time_in_bed"),
    TIME_ON_MAX_THREAT("time_on_max_threat"),
    HEARTBEATS("heartbeats"),
    DIRT_BROKEN("dirt_broken"),
    CODE_FRAGMENTS_FOUND("fragments_found"),
    CODES_ENTERED("codes_entered"),
    CODES_ENTERED_CORRECT("codes_entered_correct"),

    DOORS_OPENED("doors_opened"),
    DOORS_KNOCKED("doors_knocked");

    private final String id;

    BreakoutStatistic(String id) {
        this.id = id;
    }

    @Override
    public String getId() {
        return this.id;
    }

}
