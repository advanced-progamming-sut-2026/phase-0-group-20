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
import io.java.pvz.models.game.adventure.Adventure;
import io.java.pvz.models.game.adventure.Chapter;
import io.java.pvz.models.game.adventure.SeasonType;
import io.java.pvz.utils.Ids;
import io.java.pvz.utils.UiFactory;
import pvz.libpvz.textures.TextureBank;

public class ChapterSelectionScreen extends BaseScreen {

    private static final float ISLAND_SIZE = 420f;

    private final Adventure adventure;
    private TextureRegion backgroundRegion;
    private final GameMenuController gameMenuController = new GameMenuController();

    public ChapterSelectionScreen(Game game) {
        super(game);
        this.adventure = App.getActiveAdventure();
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

        mainLayer.add(UiFactory.screenTitle("Select a Chapter", skin, 1.8f)).padTop(10).padBottom(50).row();

        Table islandsRow = new Table();
        for (Chapter chapter : adventure.getChapters()) {
            if (chapter.getSeasonType() == SeasonType.MINI_GAME) {
                continue;
            }
            islandsRow.add(buildIslandCell(textures, skin, chapter)).pad(25);
        }

        mainLayer.add(islandsRow).expand().center();
    }

    private Table buildIslandCell(TextureBank textures, Skin skin, Chapter chapter) {
        Table cell = new Table();

        Stack islandStack;

        if (chapter.isUnlocked()) {
            islandStack = UiFactory.imageHoverStack(textures, worldImageId(chapter.getSeasonType()),
                ISLAND_SIZE, ISLAND_SIZE, 1.08f, 0.94f, () -> {
                    System.out.println("Open chapter: " + chapter.getDisplayName());
                    ScreenManager.getInstance().pushScreen(new LevelSelectionScreen(game, chapter));
                });
        } else {
            islandStack = UiFactory.imageHoverStack(textures, worldImageId(chapter.getSeasonType()),
                ISLAND_SIZE, ISLAND_SIZE, 1f, 1f, null);
            ((Image) islandStack.getChildren().first()).setColor(1f, 1f, 1f, 0.5f);

            Image lockImage = UiFactory.imageFor(textures, Ids.GameScreen.LOCK_ICON);
            Container<Image> lockContainer = new Container<>(lockImage);
            lockContainer.size(90, 90);
            islandStack.add(lockContainer);
        }

        cell.add(islandStack).size(ISLAND_SIZE, ISLAND_SIZE).row();

        Label nameLabel = new Label(chapter.getDisplayName(), skin);
        nameLabel.setFontScale(1.3f);
        nameLabel.setColor(chapter.isUnlocked() ? Color.valueOf("#FFD86B") : Color.GRAY);
        cell.add(nameLabel).padTop(15);

        return cell;
    }

    private String worldImageId(SeasonType type) {
        return switch (type) {
            case ANCIENT_EGYPT -> Ids.ChapterSelect.EGYPT;
            case FROZEN_CAVES -> Ids.ChapterSelect.FROZEN_CAVES;
            case BIG_WAVE_BEACH -> Ids.ChapterSelect.BEACH;
            case DARK_AGES -> Ids.ChapterSelect.DARK_AGES;
            case MINI_GAME -> null;
        };
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
