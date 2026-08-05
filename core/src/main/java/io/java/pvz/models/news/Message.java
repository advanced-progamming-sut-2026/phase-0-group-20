package io.java.pvz.models.news;

import io.java.pvz.models.entities.plants.Plant;
import io.java.pvz.models.entities.zombies.Zombie;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class Message {
    private String text;
    private boolean unread = true;
    private MessageType messageType;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy/MM/dd");

    public Message(String text,MessageType messageType) {
        this.text = appendDate(text);
        this.messageType = messageType;
    }

    public Message() {
    }

    private String appendDate(String baseText) {
        String currentDate = LocalDate.now().format(DATE_FORMATTER);
        return currentDate + " - " + baseText;
    }

    public static Message zombieUnlockedMessage(Zombie z) {
        String text = "New Zombie with the name " + z.getName() + " Unlocked. Congrats!!!";
        return new Message(text, MessageType.ZOMBIE);
    }

    public static Message plantUnlockedMessage(Plant p) {
        String text = "New Plant with the name " + p.getName() + " Unlocked. Congrats!!!";
        return new Message(text, MessageType.PLANT);
    }

    public static Message levelUnlockedMessage(String chapterName, int levelNumber) {
        String text = "Level " + levelNumber + " of Chapter " + chapterName + " Unlocked. Congrats!!!";
        return new Message(text, MessageType.LEVEL);
    }

    public static Message chapterUnlockedMessage(String chapterName) {
        String text = "New Chapter " + chapterName + " Unlocked. Congrats!!!";
        return new Message(text,MessageType.SEASON);
    }

    public static Message minigameUnlockedMessage(String name) {
        String text = "Minigame " + name.toUpperCase() + " is now accessible. Enjoy!!";
        return new Message(text, MessageType.MINIGAME);
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

    public MessageType getMessageType() {
        return messageType;
    }
}
