package models.news;

import java.util.ArrayList;

public class News {

    private static News instance;

    private final ArrayList<Message> messages = new ArrayList<>();

    private News() {

    }

    public static News getInstance() {
        if (instance == null) {
            instance = new News();
        }
        return instance;
    }

    public ArrayList<Message> getMessages() {
        return messages;
    }

    public void addMessages(Message message) {
        messages.add(message);
    }
}
