package models;

import models.entities.plants.Plant;
import models.entities.zombies.Zombie;
import models.enums.Menu;
import models.game.GameSession;
import models.game.adventure.Adventure;
import models.game.adventure.Chapter;
import models.news.News;
import models.users.User;

import java.util.ArrayList;
import java.util.List;

public class App {
    private static final Settings settings = Settings.getInstance();
    private static final News news = News.getInstance();
    private static User activeUser;
    private static Menu activeMenu = Menu.SIGNUP_MENU;
    private static ArrayList<Zombie> allZombies;
    private static ArrayList<Plant> allPlants;
    private static ArrayList<Chapter> allChapters;
    private static Adventure activeAdventure;
    private static GameSession activeSession;
    private static List<User> allUsers;

    public static void changeDifficulty(int difficulty) {
        settings.setDifficulty(difficulty);
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

    public static Adventure getActiveAdventure() {
        return activeAdventure;
    }

    public static Plant findPlantByName(String name) {
        for (Plant plant : allPlants) {
            if (plant.getName().equalsIgnoreCase(name)) {
                return plant;
            }
        }
        return null;
    }

    public static Zombie findZombieByName(String name) {
        for (Zombie z : allZombies) {
            if (z.getName().equals(name)) {
                return z;
            }
        }
        return null;
    }

    public static void setAllZombies(ArrayList<Zombie> allZombies) {
        App.allZombies = allZombies;
    }

    public static void setAllPlants(ArrayList<Plant> allPlants) {
        App.allPlants = allPlants;
    }

    public static ArrayList<Chapter> getAllChapters() {
        return allChapters;
    }

    public static void setAllChapters(ArrayList<Chapter> allChapters) {
        App.allChapters = allChapters;
    }

    public static void setActiveAdventure(Adventure activeAdventure) {
        App.activeAdventure = activeAdventure;
    }

    public static GameSession getActiveSession() {
        return activeSession;
    }

    public static void setActiveSession(GameSession activeSession) {
        App.activeSession = activeSession;
    }

    public static List<User> getAllUsers() {
        return allUsers;
    }

    public static void setAllUsers(List<User> allUsers) {
        App.allUsers = allUsers;
    }
}
