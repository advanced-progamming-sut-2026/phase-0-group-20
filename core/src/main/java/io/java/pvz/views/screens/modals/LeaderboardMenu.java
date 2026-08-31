package io.java.pvz.views.screens.modals;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Scaling;
import io.java.pvz.controllers.GameController.LeaderBoardController;
import io.java.pvz.controllers.GameController.NetworkController;
import io.java.pvz.controllers.GameController.NetworkLeaderboardController;
import io.java.pvz.controllers.MenuScreenController;
import io.java.pvz.models.users.User;
import io.java.pvz.utils.Ids;
import io.java.pvz.utils.UiFactory;
import pvz.libpvz.textures.TextureBank;
import pvz.skin.BorderedTable;

import java.util.List;
import java.util.Map;


public class LeaderboardMenu extends Table {

    private static final Color BROWN = Color.valueOf("#4A3018");
    private static final Color LIGHT_BROWN = Color.valueOf("#8B5A2B");

    private final LeaderBoardController controller = new LeaderBoardController();
    private final NetworkLeaderboardController networkController = new NetworkLeaderboardController();
    private final Skin skin;
    private final TextureBank textures;
    private final Table rowsTable;

    private int requestToken = 0;

    public static Table build(MenuScreenController menuController, TextureBank texture, Skin skin) {
        return new LeaderboardMenu(menuController, texture, skin);
    }

    private LeaderboardMenu(MenuScreenController menuController, TextureBank texture, Skin skin) {
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
        int token = ++requestToken;
        String sortType = controller.getCurrentSortType();

        showLoadingRow();
        networkController.fetchLeaderboard(sortType, response -> {
            if (token != requestToken) return; // a newer refresh already superseded this one

            if (response != null && response.isSuccess()) {
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> rows = (List<Map<String, Object>>) response.getData().get("rows");
                renderNetworkRows(rows != null ? rows : List.of(), sortType);
            } else {
                rowsTable.clear();
                Label errorLabel = new Label("Failed to load from server.", skin);
                errorLabel.setColor(Color.RED);
                errorLabel.setFontScale(1.3f);
                rowsTable.add(errorLabel).colspan(3).padTop(20);
            }
        });
    }

    private void showLoadingRow() {
        rowsTable.clear();
        Label loadingLabel = new Label("Loading leaderboard...", skin);
        loadingLabel.setColor(LIGHT_BROWN);
        loadingLabel.setFontScale(1.3f);
        rowsTable.add(loadingLabel).colspan(3).padTop(20);
    }

    private void renderNetworkRows(List<Map<String, Object>> rows, String sortType) {
        rowsTable.clear();

        for (int i = 0; i < rows.size(); i++) {
            Map<String, Object> row = rows.get(i);
            String username = String.valueOf(row.get("username"));
            addRow(i, username, valueFor(row, sortType));
        }

        if (rows.isEmpty()) {
            Label emptyLabel = new Label("No players yet.", skin);
            emptyLabel.setColor(LIGHT_BROWN);
            emptyLabel.setFontScale(1.3f);
            rowsTable.add(emptyLabel).colspan(3).padTop(20);
        }
    }

    private void addRow(int index, String username, String value) {
        Label rankLabel = new Label((index + 1) + ".", skin);
        rankLabel.setColor(BROWN);
        rankLabel.setFontScale(1.6f);

        Label nameLabel = new Label(username, skin);
        nameLabel.setColor(Color.ORANGE);
        nameLabel.setFontScale(1.6f);

        Label valueLabel = new Label(value, skin);
        valueLabel.setColor(LIGHT_BROWN);
        valueLabel.setFontScale(1.4f);
        valueLabel.setAlignment(Align.right);

        rowsTable.add(rankLabel).width(60).left();
        rowsTable.add(nameLabel).expandX().left().padLeft(15);
        rowsTable.add(valueLabel).width(260).right().padRight(15);
        rowsTable.row().padTop(14);
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

    private String valueFor(Map<String, Object> row, String sortType) {
        return switch (sortType) {
            case "season" -> "Ch " + asInt(row.get("chapter")) + " - Lv " + asInt(row.get("level"));
            case "minigame" -> asInt(row.get("minigameLevelsUnlocked")) + " levels";
            case "quests" -> asInt(row.get("questsCompleted")) + " quests";
            default -> "Score: " + asInt(row.get("myPoint"));
        };
    }

    private int asInt(Object value) {
        if (value instanceof Number number) return number.intValue();
        if (value == null) return 0;
        try {
            return Integer.parseInt(String.valueOf(value));
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}
