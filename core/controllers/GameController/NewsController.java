package controllers.GameController;

import models.App;
import models.Result;
import models.news.Message;
import models.news.News;

public class NewsController {


    public Result showUnreadNews() {
        News news = App.getNews();

        StringBuilder stringBuilder = new StringBuilder();
        for (Message message : news.getMessages()) {
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
        News news = App.getNews();

        StringBuilder stringBuilder = new StringBuilder();
        for (Message message : news.getMessages()) {
            stringBuilder.append(message.getText()).append("\n");
            message.setUnread(false);
        }

        String text = "";
        if (!stringBuilder.isEmpty())
            text = stringBuilder.deleteCharAt(stringBuilder.length() - 1).toString();
        return new Result(true, text);
    }


}
