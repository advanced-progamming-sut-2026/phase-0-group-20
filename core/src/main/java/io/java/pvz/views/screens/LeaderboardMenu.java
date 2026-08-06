package io.java.pvz.views.screens;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Scaling;
import io.java.pvz.controllers.GameController.LeaderBoardController;
import io.java.pvz.controllers.GameMenuController;
import io.java.pvz.models.users.User;
import io.java.pvz.utils.Ids;
import io.java.pvz.utils.UiFactory;
import pvz.libpvz.textures.TextureBank;
import pvz.skin.BorderedTable;

import java.util.List;


public class LeaderboardMenu extends Table {

    private static final Color BROWN = Color.valueOf("#4A3018");
    private static final Color LIGHT_BROWN = Color.valueOf("#8B5A2B");

    private final LeaderBoardController controller = new LeaderBoardController();
    private final Skin skin;
    private final TextureBank textures;
    private final Table rowsTable;

    public static Table build(GameMenuController menuController, TextureBank texture, Skin skin) {
        return new LeaderboardMenu(menuController, texture, skin);
    }

    private LeaderboardMenu(GameMenuController menuController, TextureBank texture, Skin skin) {
        this.skin = skin;
        this.textures = texture;
        setFillParent(true);

        BorderedTable card = new BorderedTable();
        card.pad(35, 40, 35, 40);

        Table header = new Table();
        TextButton closeBtn = UiFactory.getCloseBtn(skin, menuController::goBack);
        Image trophyIcon = UiFactory.imageFor(textures, Ids.MainMenu.LEADERBOARD_ICON);
        trophyIcon.setScaling(Scaling.fit);

        Label titleLabel = new Label("Leaderboard", skin, "big");
        titleLabel.setColor(BROWN);
        titleLabel.setFontScale(1.5f);
        titleLabel.setAlignment(Align.center);

        header.add(closeBtn).size(45, 45).left();
        header.add(trophyIcon).size(50, 50).padLeft(20).padRight(10);
        header.add(titleLabel).expandX().center().padRight(45);

        card.add(header).growX().padTop(50).padBottom(10).row();

        Table tabs = new Table();
        tabs.add(sortTabButton("Score", "score")).padRight(10);
        tabs.add(sortTabButton("Minigame", "minigame")).padRight(10);
        tabs.add(sortTabButton("Season", "season")).padRight(10);
        tabs.add(sortTabButton("Quests", "quests"));
        card.add(tabs).padBottom(20).row();

        rowsTable = new Table();
        rowsTable.top();

        ScrollPane scrollPane = new ScrollPane(rowsTable, skin);
        scrollPane.setFadeScrollBars(false);
        scrollPane.setScrollingDisabled(true, false);

        card.add(scrollPane).size(750, 520).padBottom(60).top().row();

        add(card).width(850).height(700).center();

        refreshRows();
    }

    private TextButton sortTabButton(String label, String sortKey) {
        TextButton button = new TextButton(label, skin, "green_small");
        button.getLabel().setFontScale(1.1f);
        button.addListener(new ClickListener() {
            @Override
            public void clicked(com.badlogic.gdx.scenes.scene2d.InputEvent event, float x, float y) {
                controller.changeSortType(sortKey);
                refreshRows();
            }
        });
        return button;
    }

    private void refreshRows() {
        rowsTable.clear();
        List<User> sortedUsers = controller.getSortedUsers();
        String sortType = controller.getCurrentSortType();

        for (int i = 0; i < sortedUsers.size(); i++) {
            User user = sortedUsers.get(i);

            Label rankLabel = new Label((i + 1) + ".", skin);
            rankLabel.setColor(BROWN);
            rankLabel.setFontScale(1.6f);

            Label nameLabel = new Label(user.getUsername(), skin);
            nameLabel.setColor(Color.ORANGE);
            nameLabel.setFontScale(1.6f);

            Label valueLabel = new Label(valueFor(user, sortType), skin);
            valueLabel.setColor(LIGHT_BROWN);
            valueLabel.setFontScale(1.4f);
            valueLabel.setAlignment(Align.right);

            rowsTable.add(rankLabel).width(60).left();
            rowsTable.add(nameLabel).expandX().left().padLeft(15);
            rowsTable.add(valueLabel).width(260).right().padRight(15);
            rowsTable.row().padTop(14);
        }
    }

    private String valueFor(User user, String sortType) {
        return switch (sortType) {
            case "season" -> "Ch " + (user.getHighestUnlockedChapterIndex() + 1)
                + " - Lv " + (user.getHighestUnlockedLevelIndex() + 1);
            case "minigame" -> user.getUnlockedMinigames().size() + " levels";
            case "quests" -> user.getQuestManager().getCompletedQuestsCount() + " quests";
            default -> "Score: " + user.getHighestBonusScore();
        };
    }
}
