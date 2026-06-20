package models;

import models.entities.plants.Plant;
import models.entities.zombies.Zombie;
import models.enums.Menu;
import models.users.User;

import java.util.ArrayList;

public class App {
    public static User activeUser;
    public static Menu activeMenu = Menu.SIGNUP_MENU;
    public ArrayList<Zombie> unlockedZombies;
    public ArrayList<Plant> unlockedPlants;
}
