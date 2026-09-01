package io.java.pvz.views.screens.modals;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.*;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.Viewport;
import io.java.pvz.controllers.GameController.NewsController;
import io.java.pvz.utils.UiFactory;
import pvz.skin.BorderedTable;

public class NewsModalTable extends BorderedTable {
    private final NewsController controller = new NewsController();
    private Actor blocker;

    public NewsModalTable(Skin skin) {
        super();
        pad(40, 35, 35, 35);
        buildContent(skin);
        setSize(700, 900);
    }

    private void buildContent(Skin skin) {
        Table headerTable = new Table();

        TextButton closeBtn = UiFactory.getCloseBtn(skin, this::remove);

        Label titleLabel = new Label("News and Updates", skin, "big");
        titleLabel.setColor(Color.valueOf("#4A3018"));
        titleLabel.setFontScale(1.5f);
        titleLabel.setAlignment(Align.center);

        headerTable.add(closeBtn).size(45, 45).left();
        headerTable.add(titleLabel).expandX().center();

        add(headerTable).growX().padBottom(15).row();

        Table contentTable = new Table();
        contentTable.top().pad(10);

        buildNewsList(skin, contentTable, true);

        ScrollPane scrollPane = new ScrollPane(contentTable, skin);
        scrollPane.setFadeScrollBars(false);
        scrollPane.setScrollingDisabled(true, false);

        add(scrollPane).grow().row();
    }

    private void buildNewsList(Skin skin, Table contentTable, boolean unreadOnly) {
        contentTable.clearChildren();

        String plantNews = controller.showPlantNews(unreadOnly);
        String zombieNews = controller.showZombieNews(unreadOnly);
        String seasonNews = controller.showSeasonNews(unreadOnly);
        String minigameNews = controller.showMinigameNews(unreadOnly);
        String levelNews = controller.showLevelNews(unreadOnly);

        addNewsCategory(contentTable, skin, "Unlocked Plants", plantNews);
        addNewsCategory(contentTable, skin, "Unlocked Zombies", zombieNews);
        addNewsCategory(contentTable, skin, "New Season", seasonNews);
        addNewsCategory(contentTable, skin, "New Minigame", minigameNews);
        addNewsCategory(contentTable, skin, "New Level", levelNews);

        if (!contentTable.hasChildren()) {
            Table emptyContainer = new Table();
            emptyContainer.top();

            String messageText = unreadOnly ? "There is no unread messages" : "Inbox is empty";
            Label emptyLabel = new Label(messageText, skin);
            emptyLabel.setColor(Color.BROWN);
            emptyLabel.setFontScale(1.8f);
            emptyLabel.setAlignment(Align.center);
            emptyContainer.add(emptyLabel).padBottom(25).row();

            if (unreadOnly) {
                TextButton showAllBtn = new TextButton("Show All News", skin,"brown");
                showAllBtn.getLabel().setFontScale(1.1f);
                showAllBtn.addListener(new ClickListener() {
                    @Override
                    public void clicked(InputEvent event, float x, float y) {
                        buildNewsList(skin, contentTable, false);
                    }
                });
                emptyContainer.add(showAllBtn).size(220, 60).center();
            }

            contentTable.add(emptyContainer).expand().center();
        }
    }

    private void addNewsCategory(Table table, Skin skin, String headerTitle, String newsText) {
        if (newsText == null || newsText.trim().isEmpty()) {
            return;
        }

        Label headerLabel = new Label(headerTitle, skin);
        headerLabel.setFontScale(1.5f);
        headerLabel.setColor(Color.valueOf("#684222"));

        table.add(headerLabel).left().padTop(15).padBottom(5).row();

        Label messageLabel = new Label(newsText, skin, "medium_outline");
        messageLabel.setColor(Color.valueOf("#FBF8EB"));
        messageLabel.setWrap(true);
        messageLabel.setFontScale(0.7f);
        messageLabel.setAlignment(Align.topLeft);

        table.add(messageLabel).growX().padLeft(25).padBottom(25).row();
    }

    public void show(Group targetLayer, Viewport viewport) {
        float width = viewport.getWorldWidth();
        float height = viewport.getWorldHeight();

        Table blockerTable = new Table();
        blockerTable.setSize(width, height);
        blockerTable.setTouchable(Touchable.enabled);

        blockerTable.addListener(new InputListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                return true;
            }
        });

        this.blocker = blockerTable;
        targetLayer.addActor(blocker);

        this.setPosition(
            Math.round((width - this.getWidth()) / 2f),
            Math.round((height - this.getHeight()) / 2f)
        );

        targetLayer.addActor(this);
    }

    @Override
    public boolean remove() {
        if (blocker != null) {
            blocker.remove();
        }
        return super.remove();
    }
}
