package controllers.GameController;

import models.App;
import models.Result;
import models.news.Message;
import models.news.News;
import models.users.User;

public class NewsController {


    public Result showUnreadNews() {
        User activeUser = App.getActiveUser();

        StringBuilder stringBuilder = new StringBuilder();
        for (Message message : activeUser.getInbox()) {
            if (message.isUnread()) {
                stringBuilder.append(message.getText()).append("\n");
                message.setUnread(false);
            }
        }

        String text = "";
        if (!stringBuilder.isEmpty())
            text = stringBuilder.deleteCharAt(stringBuilder.length() - 1).toString();
        return new Result(true, text);

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


}
