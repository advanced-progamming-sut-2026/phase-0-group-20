package models.enums;

import models.game.GameMode;
import models.game.GameSession;
import models.game.minigame.BowlingLevel;
import models.game.minigame.IZombieLevel;
import models.game.minigame.VaseBreakerLevel;
import views.*;

import java.util.EnumSet;
import java.util.Scanner;
import java.util.Set;

public enum Menu {
    SIGNUP_MENU(new SignUpMenu(), "signup menu"),
    LOGIN_MENU(new LoginMenu(), "login menu"),
    SETTINGS_MENU(new Setting(), "setting menu"),
    MAIN_MENU(new MainMenu(), "main menu"),
    COLLECTION_MENU(new CollectionMenu(), "collection menu"),
    LEADERBOARD_MENU(new LeaderBoard(), "leaderboard menu"),
    GREENHOUSE_MENU(new GreenHouseMenu(), "greenhouse menu"),
    PLANTSELLECTION_MENU(new PlantSelectionMenu(), "plant selection menu"),
    PROFILE_MENU(new ProfileMenu(), "profile menu"),
    SHOP_MENU(new ShopMenu(), "shop"),
    TRAVELLOG_MENU(new TravelLogMenu(), "travel log menu"),
    GAME_MENU(new GameMenu(), "game menu"),
    NEWS_MENU(new NewsMenu(), "news menu"),
    GAME_FLOW_MENU(new GameFlowMenu(), "game flow menu"),
    ;

    private final AppMenu menu;
    private final String name;

    Menu(AppMenu menu, String name) {
        this.menu = menu;
        this.name = name;
    }

    public static Menu getByName(String menuName) {
        String name = getMenuNameOnly(menuName) + " menu";
        for (Menu menu : Menu.values())
            if (menu.name.equalsIgnoreCase(name))
                return menu;
        return null;
    }

    private static String getMenuNameOnly(String menuName) {
        if (menuName == null)
            return "";
        StringBuilder name = new StringBuilder();
        for (String part : menuName.split(" ")) {
            if (part.trim().equalsIgnoreCase("menu")) break;
            name.append(part).append(" ");
        }
        return name.toString().trim().toLowerCase();
    }

    public void checkCommand(Scanner sc) {
        this.menu.check(sc);
    }

    public Set<Menu> getAllowedEntryTargets() {
        return switch (this) {
            case SIGNUP_MENU -> EnumSet.of(LOGIN_MENU);
            case LOGIN_MENU -> EnumSet.of(SIGNUP_MENU);
            case SETTINGS_MENU, NEWS_MENU, SHOP_MENU, LEADERBOARD_MENU, COLLECTION_MENU -> null;

            case MAIN_MENU -> EnumSet.of(GAME_MENU, PROFILE_MENU, SETTINGS_MENU, NEWS_MENU);

            case PLANTSELLECTION_MENU -> EnumSet.of(GAME_MENU);
            case PROFILE_MENU -> EnumSet.of(PLANTSELLECTION_MENU);
            case GAME_MENU -> EnumSet.of(COLLECTION_MENU, LEADERBOARD_MENU,
                    GREENHOUSE_MENU, PLANTSELLECTION_MENU,
                    SHOP_MENU, TRAVELLOG_MENU, NEWS_MENU);
            case GREENHOUSE_MENU -> EnumSet.of(SHOP_MENU);
            case GAME_FLOW_MENU -> null;
            case TRAVELLOG_MENU -> EnumSet.of(GAME_FLOW_MENU);
        };
    }

    public Menu getExitTarget() {
        return switch (this) {
            case SIGNUP_MENU -> null;
            case LOGIN_MENU -> SIGNUP_MENU;
            case SETTINGS_MENU, GAME_MENU, MAIN_MENU, PROFILE_MENU, NEWS_MENU -> MAIN_MENU;
            case COLLECTION_MENU, LEADERBOARD_MENU, GREENHOUSE_MENU, PLANTSELLECTION_MENU, SHOP_MENU,
                 TRAVELLOG_MENU -> GAME_MENU;
            case GAME_FLOW_MENU -> { //maybe use stack in phase2
                GameSession session = GameSession.getInstance();
                if (session != null && session.getCurrentMode() != null) {
                    GameMode mode = session.getCurrentMode();

                    if (mode instanceof VaseBreakerLevel ||
                            mode instanceof BowlingLevel ||
                            mode instanceof IZombieLevel) {
                        yield TRAVELLOG_MENU;
                    }
                }
                yield GAME_MENU;
            }
        };
    }

    public String getName() {
        return name;
    }


}
