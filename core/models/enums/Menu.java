package models.enums;

import views.*;

import java.util.Scanner;

public enum Menu {
    SIGNUP_MENU(new SignUpMenu()),
    LOGIN_MENU(new LoginMenu()),
    SETTINGS_MENU(new Setting()),
    MAIN_MENU(new MainMenu()),
    COLLECTION_MENU(new CollectionMenu()),
    LEADERBOARD_MENU(new LeaderBoard()),
    GREENHOUSE_MENU(new GreenHouse()),
    PLANTSELLECTION_MENU(new PlantSelectionMenu()),
    PROFILE_MENU(new ProfileMenu()),
    SHOP_MENU(new ShopMenu()),
    TRAVELLOG_MENU(new TravelLogMenu());

    private final AppMenu menu;

    Menu(AppMenu menu) {
        this.menu = menu;
    }

    public void checkCommand(Scanner sc) {
        this.menu.check(sc);
    }
}
