package models.news;

import models.entities.plants.Plant;
import models.entities.zombies.Zombie;

public class Message {
    private String text;
    private boolean unread = true;

    public Message(String text) {
        this.text = text;
    }

    public Message() {

    }

    public static Message zombieUnlockedMessage(Zombie z) {
        String text = "New Zombie with the name " + z.getName() + " Unlocked.Congrats!!!";
        return new Message(text);

    }

    public static Message plantUnlockedMessage(Plant p) {
        //As soon as Elyas finishes writing the code to unlock the plants in each level
        String text = "New Plant with the  name " + p.getName() + " Unlocked.Congrats!!!";
        return new Message(text);
    }

    public static Message levelUnlockedMessage(String chapterName, int levelNumber) {
        String text = "Level " + levelNumber + " of Chapter " + chapterName + " Unlocked. Congrats!!!";
        return new Message(text);
    }

    public static Message chapterUnlockedMessage(String chapterName) {
        String text = "New Chapter " + chapterName + " Unlocked. Congrats!!!";
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
