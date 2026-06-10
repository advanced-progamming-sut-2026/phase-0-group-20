package models;

import models.entities.plants.Plant;
import models.entities.zombies.Zombie;
import models.enums.Menu;
import models.users.User;

import java.util.ArrayList;

public class App {
    private static User activeUser;
    private static Menu activeMenu;
    private ArrayList<Zombie> unlockedZombies;
    private ArrayList<Plant> unlockedPlants;
}
