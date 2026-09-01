package io.java.pvz.controllers.GameController;

import io.java.pvz.models.App;
import io.java.pvz.models.Result;
import io.java.pvz.models.news.Message;
import io.java.pvz.models.news.MessageType;
import io.java.pvz.models.users.User;

public class NewsController {


    public String showUnreadPlantNews() {
        return getUnreadNewsByType(MessageType.PLANT);
    }

    public String showUnreadZombieNews() {
        return getUnreadNewsByType(MessageType.ZOMBIE);
    }

    public String showUnreadLevelNews() {
        return getUnreadNewsByType(MessageType.LEVEL);
    }

    public String showUnreadSeasonNews() {
        return getUnreadNewsByType(MessageType.SEASON);
    }

    public String showUnreadMinigameNews() {
        return getUnreadNewsByType(MessageType.MINIGAME);
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
}
