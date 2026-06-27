package models;

import models.entities.plants.Plant;
import models.entities.zombies.Zombie;
import models.enums.Menu;
import models.news.News;
import models.users.User;

import java.util.ArrayList;

public class App {
    private static User activeUser;
    private static Menu activeMenu = Menu.SIGNUP_MENU;
    private static final Settings settings = Settings.getInstance();
    private static final News news = News.getInstance();
    private static ArrayList<Zombie> allZombies;
    private static ArrayList<Plant> allPlants;

    public static void changeDifficulty(int difficulty) {
        return; // We need to implement the Zombies and Plants Mechanism before make this
    }

    public static User getActiveUser() {
        return activeUser;
    }

    public static void setActiveUser(User activeUser) {
        App.activeUser = activeUser;
    }

    public static Menu getActiveMenu() {
        return activeMenu;
    }

    public static void setActiveMenu(Menu activeMenu) {
        App.activeMenu = activeMenu;
    }

    public static ArrayList<Zombie> getAllZombies() {
        return allZombies;
    }

    public static ArrayList<Plant> getAllPlants() {
        return allPlants;
    }

    public static Settings getSettings() {
        return settings;
    }

    public static News getNews() {
        return news;
    }
}
