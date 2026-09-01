package io.java.pvz.views.screens;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.*;

import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Scaling;
import io.java.pvz.controllers.ButtonAnimator;
import io.java.pvz.controllers.MenuScreenController;
import io.java.pvz.controllers.ScreenManager;
import io.java.pvz.loader.AssetLoader;
import io.java.pvz.models.enums.Menu;
import io.java.pvz.utils.Ids;
import io.java.pvz.utils.UiFactory;
import io.java.pvz.views.screens.modals.*;
import pvz.libpvz.textures.TextureBank;

public class GameMenuScreen extends BaseScreen {

    private final MenuScreenController menuController;
    private final TextureRegion backgroundRegion;

    public GameMenuScreen(Game game) {
        super(game);
        TextureBank textures = AssetLoader.getInstance().getTextures();
        menuController = new MenuScreenController(modalLayer);
        backgroundRegion = textures.region(Ids.MainMenu.BACKGROUND);

        registerMenuPanels();
        buildTopBar();
    }

    private void registerMenuPanels() {
        TextureBank textures = AssetLoader.getInstance().getTextures();
        Skin skin = AssetLoader.getInstance().getSkin();
         menuController.register(Menu.SHOP_MENU, () -> ShopModalMenu.build(menuController, textures, skin,modalLayer));
         menuController.register(Menu.LEADERBOARD_MENU, () -> LeaderboardMenu.build(menuController, textures, skin));
    }

    private void buildTopBar() {
        TextureBank textures = AssetLoader.getInstance().getTextures();
        Skin skin = AssetLoader.getInstance().getSkin();

        mainLayer.clear();
        mainLayer.setFillParent(true);
        mainLayer.top();

        Table topRow = buildTopRow(textures, skin);
        mainLayer.add(topRow).growX().padTop(20).padLeft(30).padRight(30).row();

        Table subRow = buildSubRow(textures, skin);
        mainLayer.add(subRow).growX().padTop(5).padLeft(30).padRight(30).row();

        Table islandContainer = buildIslandContainer(textures);
        mainLayer.add(islandContainer).expand().fill().padTop(15);
    }

    private Table buildTopRow(TextureBank textures, Skin skin) {
        Table topRow = new Table();
        Table topLeftGroup = buildTopLeftGroup(textures, skin);

        Stack shopBtn = UiFactory.iconButton(textures, skin, Ids.GameScreen.SHOP_ICON, 120, 120,
            () -> menuController.open(Menu.SHOP_MENU)
        );

        topRow.add(topLeftGroup).left();
        topRow.add().expandX();
        topRow.add(shopBtn).padRight(20).right();

        return topRow;
    }

    private Table buildTopLeftGroup(TextureBank textures, Skin skin) {
        Table group = new Table();

        group.add(UiFactory.iconButton(textures, skin, Ids.GameScreen.BACK_ICON, 100, 100,
            () -> ScreenManager.getInstance().popScreen())).padLeft(20).padRight(30);

        group.add(UiFactory.iconButton(textures, skin, Ids.GameScreen.OPTIONS_ICON, 100, 100, () -> {
            System.out.println("Options Clicked");
            new SettingModalTable(skin).show(modalLayer, viewport);
        })).padRight(30);

        group.add(UiFactory.iconButton(textures, skin, Ids.GameScreen.ALMANAC_ICON, 100, 100,
            () -> ScreenManager.getInstance().pushScreen(new CollectionScreen(game, skin)))).padRight(30);

        group.add(UiFactory.iconButton(textures, skin, Ids.GameScreen.GREENHOUSE_ICON, 100, 100,
            () -> ScreenManager.getInstance().pushScreenWithTransition(() -> new ZenGarden(game), 1.5f)
        )).padRight(30);

        return group;
    }

    private Table buildSubRow(TextureBank textures, Skin skin) {
        Table subRow = new Table();
        Table subLeftGroup = new Table();

        subLeftGroup.add(UiFactory.iconButton(textures, skin, Ids.MainMenu.QUESTS_ICON, 90, 90, () -> {
            menuController.open(Menu.TRAVELLOG_MENU);
            TravelLogPanel travelLogPanel = new TravelLogPanel(skin, textures);
            modalLayer.addActor(travelLogPanel);
        }));

        subRow.add(subLeftGroup).padTop(30).padLeft(30).left();
        subRow.add().expandX();

        return subRow;
    }

    private Table buildIslandContainer(TextureBank textures) {
        Table islandContainer = new Table();
        Image adventureIcon = UiFactory.imageFor(textures, Ids.GameScreen.ADVENTURE_ICON);

        adventureIcon.setScaling(Scaling.fit);
        adventureIcon.setTouchable(Touchable.enabled);

        ButtonAnimator.applyHoverAndClickEffect(adventureIcon, 1.05f, 0.95f, () -> {
            System.out.println("Adventure Island Clicked!");
            ScreenManager.getInstance().pushScreen(new ChapterSelectionScreen(game));
        });

        islandContainer.add(adventureIcon).size(500, 500).center();

        return islandContainer;
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
