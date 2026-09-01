package io.java.pvz.controllers.GameController;

import io.java.pvz.models.App;
import io.java.pvz.models.news.Message;
import io.java.pvz.models.news.MessageType;
import io.java.pvz.models.users.User;

public class NewsController {

    public String showPlantNews(boolean unreadOnly) {
        return unreadOnly ? getUnreadNewsByType(MessageType.PLANT) : getAllNewsByType(MessageType.PLANT);
    }

    public String showZombieNews(boolean unreadOnly) {
        return unreadOnly ? getUnreadNewsByType(MessageType.ZOMBIE) : getAllNewsByType(MessageType.ZOMBIE);
    }

    public String showLevelNews(boolean unreadOnly) {
        return unreadOnly ? getUnreadNewsByType(MessageType.LEVEL) : getAllNewsByType(MessageType.LEVEL);
    }

    public String showSeasonNews(boolean unreadOnly) {
        return unreadOnly ? getUnreadNewsByType(MessageType.SEASON) : getAllNewsByType(MessageType.SEASON);
    }

    public String showMinigameNews(boolean unreadOnly) {
        return unreadOnly ? getUnreadNewsByType(MessageType.MINIGAME) : getAllNewsByType(MessageType.MINIGAME);
    }

    private String getUnreadNewsByType(MessageType targetType) {
        User activeUser = App.getActiveUser();
        StringBuilder stringBuilder = new StringBuilder();

        for (Message message : activeUser.getInbox()) {
            if (message.isUnread() && message.getMessageType() == targetType) {
                stringBuilder.append(message.getText()).append("\n\n");
                message.setUnread(false);
            }
        }

        if (stringBuilder.isEmpty()) {
            return null;
        }

        return stringBuilder.deleteCharAt(stringBuilder.length() - 1).toString();
    }

    private String getAllNewsByType(MessageType targetType) {
        User activeUser = App.getActiveUser();
        StringBuilder stringBuilder = new StringBuilder();

        for (Message message : activeUser.getInbox()) {
            if (message.getMessageType() == targetType) {
                stringBuilder.append(message.getText()).append("\n\n");
                message.setUnread(false);
            }
        }

        if (stringBuilder.isEmpty()) {
            return null;
        }

        return stringBuilder.deleteCharAt(stringBuilder.length() - 1).toString();
    }
}
