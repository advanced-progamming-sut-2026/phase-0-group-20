package io.java.pvz.views.screens.modals;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import io.java.pvz.controllers.GameController.TravelLogController;
import io.java.pvz.models.App;
import io.java.pvz.models.game.minigame.MiniGameType;
import io.java.pvz.models.quest.Quest;
import io.java.pvz.models.quest.QuestCategory;
import io.java.pvz.utils.MinigameItemUi;
import io.java.pvz.utils.QuestItemUi;
import io.java.pvz.utils.UiFactory;
import org.jspecify.annotations.NonNull;
import pvz.libpvz.textures.TextureBank;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

public class TravelLogPanel extends Table {

    private final Skin skin;
    private final TextureBank textures;
    private Container<ScrollPane> listContainer;
    private ButtonGroup<TextButton> tabGroup;
    private TravelLogController travelLogController;

    public TravelLogPanel(Skin skin, TextureBank textures) {
        this.skin = skin;
        this.textures = textures;

        travelLogController = new TravelLogController();

        this.setFillParent(true);
        this.bottom();

        Pixmap dimPixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        dimPixmap.setColor(0, 0, 0, 0.45f);
        dimPixmap.fill();
        Image scrim = new Image(new Texture(dimPixmap));
        dimPixmap.dispose();
        this.setBackground(scrim.getDrawable());

        buildUi();
    }

    private void buildUi() {
        tabGroup = new ButtonGroup<>();
        tabGroup.setMaxCheckCount(1);
        tabGroup.setMinCheckCount(1);

        TextButton.TextButtonStyle tabStyle = createTabStyle();

        Table tabsRow = crateTabRow(tabStyle);

        ImageButton closeBtn = new ImageButton(skin, "generic_close");
        closeBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                TravelLogPanel.this.remove();
            }
        });

        Stack boardStack = new Stack();

        Table boardBg = new Table();
        boardBg.setBackground(skin.getDrawable("image_ui_quests_panel_edge_to_edge_ten"));
        boardStack.add(boardBg);

        Table boardContent = new Table();
        Label refreshLabel = new Label("Daily Activities refresh in " + getTimeUntilMidnight()
            , skin, "FBUSV8C5EI_1", Color.GOLD);
        refreshLabel.setFontScale(0.8f);
        boardContent.add(refreshLabel).padTop(25).padBottom(10).row();

        listContainer = new Container<>();
        listContainer.fill();
        boardContent.add(listContainer).expand().fill().pad(20);
        boardStack.add(boardContent);

        Table closeBtnLayer = new Table();
        closeBtnLayer.top().right();
        closeBtnLayer.add(closeBtn).padRight(15).padTop(-45).size(50, 50);
        boardStack.add(closeBtnLayer);

        Stack mainLayoutStack = new Stack();

        Table boardLayer = new Table();
        boardLayer.bottom();
        boardLayer.add(boardStack).width(1000).height(800);
        mainLayoutStack.add(boardLayer);

        Table tabsLayer = new Table();
        tabsLayer.top().left();
        tabsLayer.add(tabsRow).padLeft(30).padTop(-5);
        mainLayoutStack.add(tabsLayer);

        this.add(mainLayoutStack).width(1000).height(830);

        loadCategory(QuestCategory.DAILY);
    }

    private TextButton.@NonNull TextButtonStyle createTabStyle() {
        TextButton.TextButtonStyle tabStyle = new TextButton.TextButtonStyle();
        tabStyle.font = skin.getFont("FBUSV8C5EI_1");
        tabStyle.fontColor = Color.WHITE;
        tabStyle.up = UiFactory.imageFor(textures, "IMAGE_UI_QUESTS_DAILY_INACTIVE").getDrawable();
        tabStyle.checked = UiFactory.imageFor(textures, "IMAGE_UI_QUESTS_DAILY_ACTIVE").getDrawable();
        return tabStyle;
    }

    private @NonNull Table crateTabRow(TextButton.TextButtonStyle tabStyle) {
        Table tabsRow = new Table();
        tabsRow.left();
        tabsRow.add(createTab("Daily", QuestCategory.DAILY, tabStyle)).padRight(2).size(110, 50);
        tabsRow.add(createTab("Main", QuestCategory.MAIN, tabStyle)).padRight(2).size(110, 50);
        tabsRow.add(createTab("Epic", QuestCategory.EPIC, tabStyle)).padRight(2).size(110, 50);
        tabsRow.add(createMinigameTab("Minigame")).padRight(2).size(140, 50);
        return tabsRow;
    }

    private TextButton createTab(String title, QuestCategory category, TextButton.TextButtonStyle style) {
        TextButton tabBtn = new TextButton(title, style);
        tabGroup.add(tabBtn);
        tabBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (tabBtn.isChecked()) {
                    loadCategory(category);
                }
            }
        });
        return tabBtn;
    }

    private TextButton createMinigameTab(String title) {
        TextButton.TextButtonStyle style = new TextButton.TextButtonStyle();
        style.font = skin.getFont("FBUSV8C5EI_1");
        style.fontColor = Color.WHITE;
        style.up = UiFactory.imageFor(textures, "IMAGE_UI_QUESTS_EPIC_INACTIVE").getDrawable();
        style.checked = UiFactory.imageFor(textures, "IMAGE_UI_QUESTS_EPIC_ACTIVE").getDrawable();

        TextButton tabBtn = new TextButton(title, style);
        tabGroup.add(tabBtn);

        tabBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (tabBtn.isChecked()) {
                    loadMinigames();
                    System.out.println(travelLogController.changePage("Minigame"));
                }
            }
        });
        return tabBtn;
    }

    private void loadCategory(QuestCategory category) {
        Table itemsTable = new Table();
        itemsTable.top();

        List<Quest> allQuests = App.getActiveUser().getQuestManager().getActiveQuests();

        for (Quest quest : allQuests) {
            if (quest.getCategory() == category) {
                QuestItemUi item = new QuestItemUi(quest, skin, textures);
                itemsTable.add(item).center().padBottom(60).fillX().expandX().height(108).row();
            }
        }

        ScrollPane scrollPane = new ScrollPane(itemsTable, skin) {
            @Override
            protected void setStage(Stage stage) {
                super.setStage(stage);
                if (stage != null) stage.setScrollFocus(this);
            }
        };
        scrollPane.setScrollingDisabled(true, false);
        listContainer.setActor(scrollPane);
    }

    private void loadMinigames() {
        Table itemsTable = new Table();
        itemsTable.top();

        for (MiniGameType type : MiniGameType.values()) {
            MinigameItemUi item = new MinigameItemUi(type, skin, textures, travelLogController);
            itemsTable.add(item).center().padBottom(30).fillX().width(700).height(200).row();
        }

        ScrollPane scrollPane = new ScrollPane(itemsTable, skin) {
            @Override
            protected void setStage(Stage stage) {
                super.setStage(stage);
                if (stage != null) stage.setScrollFocus(this);
            }
        };
        scrollPane.setScrollingDisabled(true, false);

        listContainer.setActor(scrollPane);
    }

    private String getTimeUntilMidnight() {
        LocalDateTime now = LocalDateTime.now();
        Duration duration = Duration.between(now, now.toLocalDate().plusDays(1).atStartOfDay());
        return String.format("%02dh:%02dmin", duration.toHours(), duration.toMinutes() % 60);
    }
}
