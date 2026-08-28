package io.java.pvz.views.screens;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import io.java.pvz.controllers.AudioManager;
import io.java.pvz.controllers.GameController.GameMenuController;
import io.java.pvz.controllers.GameController.TravelLogController;
import io.java.pvz.controllers.ScreenManager;
import io.java.pvz.loader.AssetLoader;
import io.java.pvz.models.App;
import io.java.pvz.models.Result;
import io.java.pvz.models.game.adventure.Chapter;
import io.java.pvz.models.game.events.GameEvent;
import io.java.pvz.models.game.events.GameEventMessenger;
import io.java.pvz.models.game.events.GameEventPayload;
import io.java.pvz.models.game.minigame.MiniGameType;
import io.java.pvz.utils.Ids;
import io.java.pvz.utils.UiFactory;
import io.java.pvz.views.screens.gameflow.GameFlowScreen;
import org.jspecify.annotations.NonNull;
import pvz.libpvz.textures.TextureBank;

public class LevelSelectionScreen extends BaseScreen {

    private static final int LEVELS_PER_CHAPTER = 4;
    private static final int MINI_GAME_LEVELS = 3;
    private static final float NODE_SIZE = 180f;
    private static final float PATH_AREA_WIDTH = 1700f;
    private static final float LOWER_Y = 100f;
    private static final float UPPER_Y = 420f;
    private static final float GROUP_HEIGHT = UPPER_Y + NODE_SIZE + 40f;
    private MiniGameType minigameType;


    private final Chapter chapter;
    private TextureRegion backgroundRegion;
    private final GameMenuController gameMenuController = new GameMenuController();
    private TravelLogController travelLogController;

    public LevelSelectionScreen(Game game, Chapter chapter) {
        super(game);
        this.chapter = chapter;
        buildUi();
    }

    public LevelSelectionScreen(Game game, MiniGameType miniGameType, TravelLogController travellog) {
        super(game);
        this.chapter = null;
        minigameType = miniGameType;
        this.travelLogController = travellog;
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

        if (chapter != null) {
            mainLayer.add(UiFactory.screenTitle(chapter.getDisplayName(), skin, 1.8f)).padTop(10).padBottom(40).row();
        } else {
            mainLayer.add(UiFactory.screenTitle(minigameType.getName(), skin, 1.8f)).padTop(10).padBottom(40).row();
        }


        mainLayer.add(buildPathGroup(textures, skin)).expand().center();
    }

    private Group buildPathGroup(TextureBank textures, Skin skin) {
        Group group = new Group();
        group.setSize(PATH_AREA_WIDTH, GROUP_HEIGHT);

        int levelCount = (chapter != null) ? LEVELS_PER_CHAPTER : MINI_GAME_LEVELS;

        float[] centerX = new float[levelCount];
        float[] centerY = new float[levelCount];

        calculateNodePositions(centerX, centerY, levelCount);
        addConnectorsToGroup(group, textures, centerX, centerY, levelCount);
        addNodesToGroup(group, textures, skin, centerX, centerY, levelCount);

        return group;
    }

    private void calculateNodePositions(float[] centerX, float[] centerY, int levelCount) {
        float spacingX = (PATH_AREA_WIDTH - NODE_SIZE) / (levelCount - 1);

        for (int i = 0; i < levelCount; i++) {
            centerX[i] = NODE_SIZE / 2f + i * spacingX;
            float y = (i % 2 == 0) ? LOWER_Y : UPPER_Y;
            centerY[i] = y + NODE_SIZE / 2f;
        }
    }

    private void addConnectorsToGroup(
        Group group, TextureBank textures, float[] centerX, float[] centerY, int levelCount
    ) {
        for (int i = 0; i < levelCount - 1; i++) {
            boolean pathReached = isLevelUnlocked(i + 1);
            group.addActor(createConnector(
                textures,
                centerX[i], centerY[i],
                centerX[i + 1], centerY[i + 1],
                pathReached
            ));
        }
    }

    private void addNodesToGroup(
        Group group, TextureBank textures, Skin skin, float[] centerX, float[] centerY, int levelCount
    ) {
        for (int i = 0; i < levelCount; i++) {
            Stack node = buildLevelNode(textures, skin, i);
            node.setPosition(centerX[i] - NODE_SIZE / 2f, centerY[i] - NODE_SIZE / 2f);
            group.addActor(node);
        }
    }

    private Image createConnector(TextureBank textures, float x1, float y1, float x2, float y2, boolean reached) {
        String connectorId = reached ? Ids.LevelSelect.CONNECTOR_FILL : Ids.LevelSelect.CONNECTOR_EMPTY;
        TextureRegion region = textures.region(connectorId);

        float dx = x2 - x1;
        float dy = y2 - y1;
        float length = (float) Math.sqrt(dx * dx + dy * dy);
        float angleDeg = (float) Math.toDegrees(Math.atan2(dy, dx));

        Image connector = new Image(new TextureRegionDrawable(region));
        connector.setSize(length, 18f);
        connector.setPosition(x1, y1 - 9f);
        connector.setOrigin(0f, 9f);
        connector.setRotation(angleDeg);

        return connector;
    }

    private Stack buildLevelNode(TextureBank textures, Skin skin, int levelIndex) {
        boolean unlocked = isLevelUnlocked(levelIndex);

        Stack nodeStack;
        if (unlocked) {
            nodeStack = UiFactory.imageHoverStack(textures, iconFor(levelIndex), NODE_SIZE, NODE_SIZE,
                1.1f, 0.9f, () -> {
                    Result result;

                    if (chapter == null) {
                        result =
                            travelLogController.startMiniGame(minigameType.getName(), String.valueOf(levelIndex + 1));
                    } else {
                        result = gameMenuController.enterLevel(String.valueOf(levelIndex + 1));
                    }

                    if (result.isSuccessful()) {
                        System.out.println(result.message());
                        String mapId = gameMenuController.getCurrentMapTextureId();
                        AudioManager.getInstance().stopMusic();

                        ScreenManager.getInstance().pushScreenWithTransition(
                            () -> new GameFlowScreen(game, mapId), 2.5f
                        );
                    } else {
                        System.out.println(result.message());
                        GameEventMessenger.getInstance().dispatch(GameEvent.NOTIFY,
                            new GameEventPayload.Builder(GameEvent.NOTIFY)
                                .message(result.message())
                                .build());
                    }
                });
        } else {
            nodeStack = UiFactory.imageHoverStack(textures, iconFor(levelIndex), NODE_SIZE, NODE_SIZE,
                1f, 1f, null);
            ((Image) nodeStack.getChildren().first()).setColor(1f, 1f, 1f, 0.5f);

            Image lockImage = UiFactory.imageFor(textures, Ids.GameScreen.LOCK_ICON);
            Container<Image> lockContainer = new Container<>(lockImage);
            lockContainer.size(70, 70);
            nodeStack.add(lockContainer);
        }

        Container<Label> numberContainer = createLevelContainer(skin, levelIndex);
        nodeStack.add(numberContainer);

        return nodeStack;
    }

    private static @NonNull Container<Label> createLevelContainer(Skin skin, int levelIndex) {
        Label numberLabel = new Label(String.valueOf(levelIndex + 1), skin, "big");
        numberLabel.setFontScale(1.6f);
        numberLabel.setColor(Color.WHITE);
        numberLabel.setAlignment(Align.bottomRight);
        Container<Label> numberContainer = new Container<>(numberLabel);
        numberContainer.bottom().right().padBottom(5).padRight(10);
        return numberContainer;
    }

    private boolean isLevelUnlocked(int levelIndex) {
        if (chapter == null) {
            try {
                int maxUnlocked = App.getActiveUser().getUnlockedLevelInMinigame(minigameType);
                return (levelIndex) <= maxUnlocked;
            } catch (Exception e) {
                return false;
            }
        } else {
            if (App.getActiveUser().getHighestUnlockedChapterIndex() > chapter.getChapterIndex()) return true;
            return chapter.isUnlocked() && levelIndex <= Math.min(chapter.getLevels().size() - 1,
                chapter.getMaxLevelIndexInThisChapter());
        }
    }

    private String iconFor(int levelIndex) {
        if (chapter == null) {
            return Ids.LevelSelect.NORMAL_ICON;
        }

        int lastIndex = chapter.getLevels().size() - 1;

        if (levelIndex == lastIndex) {
            return switch (chapter.getSeasonType()) {
                case ANCIENT_EGYPT -> Ids.LevelSelect.BOSS_EGYPT;
                case FROZEN_CAVES -> Ids.LevelSelect.BOSS_ICEAGE;
                case BIG_WAVE_BEACH -> Ids.LevelSelect.BOSS_BEACH;
                case DARK_AGES -> Ids.LevelSelect.BOSS_DARK;
                case MINI_GAME -> Ids.LevelSelect.NORMAL_ICON;
            };
        }

        if (levelIndex == lastIndex - 1 && lastIndex > 0) {
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
