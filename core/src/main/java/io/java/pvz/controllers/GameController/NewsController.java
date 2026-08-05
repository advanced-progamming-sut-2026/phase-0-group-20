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

    public Result showAllNews() {
        User activeUser = App.getActiveUser();

        StringBuilder stringBuilder = new StringBuilder();
        for (Message message : activeUser.getInbox()) {
            stringBuilder.append(message.getText()).append("\n");
            message.setUnread(false);
        }

        String text = "";
        if (!stringBuilder.isEmpty())
            text = stringBuilder.deleteCharAt(stringBuilder.length() - 1).toString();
        return new Result(true, text);
    }

    private String getUnreadNewsByType(MessageType targetType) {
        User activeUser = App.getActiveUser();
        StringBuilder stringBuilder = new StringBuilder();

        for (Message message : activeUser.getInbox()) {
            if (message.isUnread() && message.getMessageType() == targetType) {
                stringBuilder.append(message.getText()).append("\n");
                message.setUnread(false);
            }
        }

        if (stringBuilder.isEmpty()) {
            return null;
        }

        return stringBuilder.deleteCharAt(stringBuilder.length() - 1).toString();
    }


}
