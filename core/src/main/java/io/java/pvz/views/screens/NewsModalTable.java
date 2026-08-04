package io.java.pvz.views.screens;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.utils.Align;
import io.java.pvz.controllers.GameController.NewsController;
import io.java.pvz.models.Result;
import io.java.pvz.utils.UiFactory;
import pvz.skin.BorderedTable;

public class NewsModalTable extends BorderedTable {
    private final NewsController controller = new NewsController();

    public NewsModalTable(Skin skin) {
        super();

        pad(40, 35, 35, 35);

        buildContent(skin);

        setSize(700, 900);
    }

    private void buildContent(Skin skin) {
        Table headerTable = new Table();

        TextButton closeBtn = UiFactory.getCloseBtn(skin, this::remove);

        Label titleLabel = new Label("News and Updates", skin,"big");
        titleLabel.setColor(Color.valueOf("#4A3018"));
        titleLabel.setFontScale(1.5f);
        titleLabel.setAlignment(Align.center);

        headerTable.add(closeBtn).size(45, 45).left();
        headerTable.add(titleLabel).expandX().center();

        add(headerTable).growX().padBottom(15).row();

        Table contentTable = new Table();
        contentTable.top().pad(10);

        Result result = null;
        String newsText = (result != null) ? result.toString() : "";

        fillTheContent(skin, newsText, contentTable);
        ScrollPane scrollPane = new ScrollPane(contentTable, skin);
        scrollPane.setFadeScrollBars(false);
        scrollPane.setScrollingDisabled(true, false);

        add(scrollPane).grow().row();
    }

    private void fillTheContent(Skin skin, String newsText, Table contentTable) {
        if (newsText == null || newsText.trim().isEmpty()) {
            Label emptyLabel = new Label("There is no unread message", skin);
            emptyLabel.setColor(Color.BROWN);
            emptyLabel.setFontScale(3f);
            emptyLabel.setAlignment(Align.center);
            contentTable.add(emptyLabel).expand().center();
        } else {
            String[] messages = newsText.split("\n");

            for (String msg : messages) {
                if (msg.trim().isEmpty()) continue;

                Label msgLabel = new Label(msg, skin);
                msgLabel.setColor(Color.valueOf("#4A3018"));
                msgLabel.setWrap(true);
                msgLabel.setAlignment(Align.topLeft);

                contentTable.add(msgLabel).growX().padBottom(25).row();
            }
        }
    }
}
