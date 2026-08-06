package io.java.pvz.views.screens;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.utils.Scaling;
import io.java.pvz.controllers.ButtonAnimator;
import io.java.pvz.controllers.GameController.GameMenuController;
import io.java.pvz.controllers.ScreenManager;
import io.java.pvz.loader.AssetLoader;
import io.java.pvz.models.App;
import io.java.pvz.models.Result;
import io.java.pvz.models.enums.Menu;
import io.java.pvz.models.game.GameSession;
import io.java.pvz.models.game.adventure.Chapter;
import io.java.pvz.utils.Ids;
import io.java.pvz.utils.UiFactory;
import pvz.libpvz.textures.TextureBank;

public class LevelSelectionScreen extends BaseScreen {

    private static final Color BROWN = Color.valueOf("#4A3018");
    private static final int LEVELS_PER_CHAPTER = 4;

    private final Chapter chapter;
    private TextureRegion backgroundRegion;
    private final GameMenuController gameMenuController = new GameMenuController();

    public LevelSelectionScreen(Game game, Chapter chapter) {
        super(game);
        this.chapter = chapter;
        buildUi();
    }

    private void buildUi() {
        TextureBank textures = AssetLoader.getInstance().getTextures();
        Skin skin = AssetLoader.getInstance().getSkin();

        backgroundRegion = textures.region(Ids.MainMenu.BACKGROUND);

        mainLayer.clear();
        mainLayer.setFillParent(true);
        mainLayer.top();

        Table topBar = new Table();
        topBar.add(UiFactory.iconButton(textures, skin, Ids.GameScreen.BACK_ICON, 100, 100,
            () -> ScreenManager.getInstance().popScreen())).left();
        topBar.add().expandX();
        mainLayer.add(topBar).growX().padTop(20).padLeft(30).row();

        Label title = new Label(chapter.getDisplayName(), skin, "big");
        title.setColor(BROWN);
        title.setFontScale(1.8f);
        mainLayer.add(title).padTop(10).padBottom(40).row();

        Table path = new Table();
        for (int levelIndex = 0; levelIndex < LEVELS_PER_CHAPTER; levelIndex++) {
            boolean fromLeft = (levelIndex % 2 == 0);
            Table row = new Table();
            Table node = buildLevelNode(textures, skin, levelIndex);
            if (fromLeft) {
                row.add(node).left().padLeft(150);
                row.add().expandX();
            } else {
                row.add().expandX();
                row.add(node).right().padRight(150);
            }
            path.add(row).growX().padBottom(30).row();
        }

        ScrollPane scrollPane = new ScrollPane(path, skin);
        scrollPane.setFadeScrollBars(false);
        scrollPane.setScrollingDisabled(true, false);
        mainLayer.add(scrollPane).grow().padLeft(40).padRight(40);
    }

    private Table buildLevelNode(TextureBank textures, Skin skin, int levelIndex) {
        Table cell = new Table();

        boolean unlocked = isLevelUnlocked(levelIndex);

        Stack nodeStack = new Stack();

        Image icon = UiFactory.imageFor(textures, iconFor(levelIndex));
        icon.setScaling(Scaling.fit);
        Container<Image> iconContainer = new Container<>(icon);
        iconContainer.size(160, 160);
        nodeStack.add(iconContainer);

        Label numberLabel = new Label(String.valueOf(levelIndex + 1), skin, "big");
        numberLabel.setFontScale(1.6f);
        numberLabel.setColor(Color.WHITE);
        Container<Label> numberContainer = new Container<>(numberLabel);
        numberContainer.bottom().right().padBottom(5).padRight(10);
        nodeStack.add(numberContainer);

        if (unlocked) {
            nodeStack.setTouchable(Touchable.enabled);
            ButtonAnimator.applyHoverAndClickEffect(nodeStack, 1.08f, 0.92f, () -> {
                System.out.println("Start level " + (levelIndex + 1) + " of " + chapter.getDisplayName());
                Result result = gameMenuController.enterLevel(String.valueOf(levelIndex + 1));

                if (result.isSuccessful()) {
                    System.out.println(result.message());
                    String mapId = gameMenuController.getCurrentMapTextureId();
                    ScreenManager.getInstance().setRootScreen(new GameFlowScreen(game, mapId));
                } else {
                    System.out.println(result.message());
                    //TODO: show error as notification
                }
            });
        } else {
            icon.setColor(1f, 1f, 1f, 0.5f);
            Image lockImage = UiFactory.imageFor(textures, Ids.GameScreen.LOCK_ICON);
            lockImage.setScaling(Scaling.fit);
            Container<Image> lockContainer = new Container<>(lockImage);
            lockContainer.size(70, 70);
            nodeStack.add(lockContainer);
        }

        cell.add(nodeStack).size(160, 160);
        return cell;
    }

    private boolean isLevelUnlocked(int levelIndex) {
        return chapter.isUnlocked() && levelIndex <= chapter.getCurrentLevelIndex();
    }

    private String iconFor(int levelIndex) {
        if (levelIndex == 3) {
            return switch (chapter.getSeasonType()) {
                case ANCIENT_EGYPT -> Ids.LevelSelect.BOSS_EGYPT;
                case FROZEN_CAVES -> Ids.LevelSelect.BOSS_ICEAGE;
                case BIG_WAVE_BEACH -> Ids.LevelSelect.BOSS_BEACH;
                case DARK_AGES -> Ids.LevelSelect.BOSS_DARK;
                case MINI_GAME -> Ids.LevelSelect.NORMAL_ICON;
            };
        }
        if (levelIndex == 2) {
            return switch (chapter.getSeasonType()) {
                case ANCIENT_EGYPT -> Ids.LevelSelect.CONVEYOR_ICON;
                case FROZEN_CAVES -> Ids.LevelSelect.TIMED_ICON;
                default -> Ids.LevelSelect.SPECIAL_ICON;
            };
        }
        return Ids.LevelSelect.NORMAL_ICON;
    }

    @Override
    public void render(float delta) {
        clearScreen(0.02f, 0.15f, 0.16f, 1f);

        AssetLoader.getInstance().updateTextures();

        if (backgroundRegion != null) {
            batch.begin();
            batch.draw(backgroundRegion, 0, 0, viewport.getWorldWidth(), viewport.getWorldHeight());
            batch.end();
        }

        stage.act(delta);
        stage.draw();
    }
}
