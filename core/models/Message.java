package models;

import models.entities.plants.Plant;
import models.entities.zombies.Zombie;

public class Message {
    private String text;
    private boolean unread;

    public static Message zombieUnlockedMessage(Zombie z) {
        return null;
    }

    public static Message plantUnlockedMessage(Plant p) {
        return null;
    }

    public static Message levelUnlockedMessage() {
        return null;
    }
}
