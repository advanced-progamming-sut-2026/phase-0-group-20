package models.news;

import models.entities.plants.Plant;
import models.entities.zombies.Zombie;

public class Message {
    private final String text;
    private boolean unread = true;

    public Message(String text) {
        this.text = text;
    }

    public static Message zombieUnlockedMessage(Zombie z) {
        String text = "New Zombie with the name " + z.getName() + " Unlocked.Congrats!!!";
        return new Message(text);

    }

    public static Message plantUnlockedMessage(Plant p) {
        String text = "New Plant with the  name " + p.getName() + " Unlocked.Congrats!!!";
        return new Message(text);
    }

    public static Message levelUnlockedMessage(int number) {
        String text = "The main level Num #" + number + " Unlocked.Congrats!!!";
        return new Message(text);
    }

    public static Message minigameUnlockedMessage(String name) {
        String text = "Minigame " + name.toUpperCase() + " is now accessible.Enjoy!!";
        return new Message(text);
    }

    public String getText() {
        return text;
    }

    public boolean isUnread() {
        return unread;
    }

    public void setUnread(boolean unread) {
        this.unread = unread;
    }
}
