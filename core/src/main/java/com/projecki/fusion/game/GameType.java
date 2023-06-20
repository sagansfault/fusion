package com.projecki.fusion.game;

@SuppressWarnings("SpellCheckingInspection")
public enum GameType {

    SQUIDGAMES("gridgame", "Grid Game", "GG"),
    BEDWARS_SOLO("bedwars-solo", "BedWars Solo", "BEDS"),
    BEDWARS_DUO("bedwars-duo", "BedWars Duos", "BEDD"),
    BEDWARS_TEAMS("bedwars-teams", "BedWars Teams", "BEDT"),
    PLUNDER("plunder", "Plunder", "PD"),
    BATTLEBOX("battlebox", "Battle Box", "BB"),
    BREAKOUT("breakout", "Breakout", "BR"),
    DISASTERS("disasters", "Survive the Disasters", "DI"),
    MICROGAMES("microgames", "Micro Games", "MICRO");

    private final String id;
    private final String displayName;
    private final String shortName;

    /**
     * @param id short ID that can be used for an ID in a database or something similar
     * @param displayName properly capitalized name that will be displayed to players
     * @param shortName a short version of the name that can be used with space is tight
     */
    GameType(String id, String displayName, String shortName) {
        this.id = id;
        this.displayName = displayName;
        this.shortName = shortName;
    }

    /**
     * Get a short ID that can be used for an ID in a database or something similar
     */
    public String getId() {
        return id;
    }

    /**
     * Get a properly capitalized name that will be displayed to players
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * Get a short version of the name that can be used with space is tight
     */
    public String getShortName() {
        return shortName;
    }
}
