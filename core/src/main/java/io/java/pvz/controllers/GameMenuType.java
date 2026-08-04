package io.java.pvz.controllers;

import java.util.EnumSet;

public enum GameMenuType {
    MAIN_MENU,
    GAME_MENU,
    COLLECTION_MENU,
    LEADERBOARD_MENU,
    GREENHOUSE_MENU,
    PLANTSELECTION_MENU,
    SHOP_MENU,
    TRAVELLOG_MENU,
    LEVEL_SELECTION_MENU;

    public EnumSet<GameMenuType> reachableMenus() {
        return switch (this) {
            case GAME_MENU -> EnumSet.of(COLLECTION_MENU, LEADERBOARD_MENU,
                GREENHOUSE_MENU, PLANTSELECTION_MENU,
                SHOP_MENU, TRAVELLOG_MENU, LEVEL_SELECTION_MENU);
            case GREENHOUSE_MENU -> EnumSet.of(SHOP_MENU);
            default -> EnumSet.of(GAME_MENU);
        };
    }

    public GameMenuType getExitTarget() {
        return switch (this) {
            case MAIN_MENU -> null;
            case GAME_MENU -> MAIN_MENU;
            case SHOP_MENU -> GREENHOUSE_MENU;
            case COLLECTION_MENU, LEADERBOARD_MENU, GREENHOUSE_MENU,
                 PLANTSELECTION_MENU, TRAVELLOG_MENU, LEVEL_SELECTION_MENU -> GAME_MENU;
        };
    }
}
