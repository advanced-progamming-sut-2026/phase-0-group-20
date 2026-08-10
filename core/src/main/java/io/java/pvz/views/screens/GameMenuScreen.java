package io.java.pvz.views.screens;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.*;

import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Scaling;
import io.java.pvz.controllers.ButtonAnimator;
import io.java.pvz.controllers.GameMenuController;
import io.java.pvz.controllers.ScreenManager;
import io.java.pvz.loader.AssetLoader;
import io.java.pvz.models.enums.Menu;
import io.java.pvz.utils.Ids;
import io.java.pvz.utils.UiFactory;
import pvz.libpvz.textures.TextureBank;

public class GameMenuScreen extends BaseScreen {

    private final GameMenuController menuController;
    private final TextureRegion backgroundRegion;

    public GameMenuScreen(Game game) {
        super(game);
        TextureBank textures = AssetLoader.getInstance().getTextures();
        menuController = new GameMenuController(modalLayer);
        backgroundRegion = textures.region(Ids.MainMenu.BACKGROUND);

        registerMenuPanels();
        buildTopBar();
    }

    private void registerMenuPanels() {
        TextureBank textures = AssetLoader.getInstance().getTextures();
        Skin skin = AssetLoader.getInstance().getSkin();
         menuController.register(Menu.SHOP_MENU, () -> ShopModalMenu.build(menuController, textures, skin));
//         menuController.register(Menu.COLLECTION_MENU, () -> CollectionMenu.build());
//         menuController.register(Menu.GREENHOUSE_MENU, () -> GreenHouseMenu.build());
         menuController.register(Menu.LEADERBOARD_MENU, () -> LeaderboardMenu.build(menuController, textures, skin));
//         menuController.register(Menu.PLANTSELECTION_MENU, () -> PlantSelectionMenu.build());
//         menuController.register(Menu.TRAVELLOG_MENU, () -> TravelLogMenu.build());
//         menuController.register(Menu.LEVEL_SELECTION_MENU, () -> LevelSelectionMenu.build());
    }

    private void buildTopBar() {
        TextureBank textures = AssetLoader.getInstance().getTextures();
        Skin skin = AssetLoader.getInstance().getSkin();

        mainLayer.clear();
        mainLayer.setFillParent(true);
        mainLayer.top();

        Table topRow = new Table();

        Table topLeftGroup = new Table();
        topLeftGroup.add(UiFactory.iconButton(textures, skin, Ids.GameScreen.BACK_ICON, 100, 100,
            () -> ScreenManager.getInstance().popScreen())).padLeft(20).padRight(30);

        topLeftGroup.add(UiFactory.iconButton(textures, skin, Ids.GameScreen.OPTIONS_ICON,100, 100,
            () -> {
                System.out.println("Options Clicked");
                new SettingModalTable(skin).show(modalLayer,viewport);
            })).padRight(30);

        topLeftGroup.add(UiFactory.iconButton(textures, skin, Ids.GameScreen.ALMANAC_ICON,100, 100,
            () -> ScreenManager.getInstance().pushScreen(new CollectionScreen(game,skin)))).padRight(30);

        topLeftGroup.add(UiFactory.iconButton(textures, skin, Ids.GameScreen.GREENHOUSE_ICON,100, 100,
            () -> ScreenManager.getInstance().pushScreen(new ZenGarden(game)))).padRight(30);

        topLeftGroup.add(UiFactory.iconButton(textures, skin, Ids.MainMenu.NEWS_ICON,100, 100,
            () -> {
                System.out.println("News Clicked");
                new NewsModalTable(skin).show(modalLayer,viewport);
            })).padRight(30);


        Stack shopBtn = UiFactory.iconButton(textures, skin, Ids.GameScreen.SHOP_ICON,120, 120,
            () -> menuController.open(Menu.SHOP_MENU));

        topRow.add(topLeftGroup).left();
        topRow.add().expandX();
        topRow.add(shopBtn).padRight(20).right();

        mainLayer.add(topRow).growX().padTop(20).padLeft(30).padRight(30).row();

        Table subRow = new Table();

        Table subLeftGroup = new Table();
        subLeftGroup.add(UiFactory.iconButton(textures, skin, Ids.GameScreen.EVENT_ICON, 90, 90,
            () -> System.out.println("Event Clicked"))).padRight(30).padLeft(20);

        subLeftGroup.add(UiFactory.iconButton(textures, skin, Ids.MainMenu.QUESTS_ICON,90, 90,
            () ->  {
                menuController.open(Menu.TRAVELLOG_MENU);
                TravelLogPanel travelLogPanel = new TravelLogPanel(skin, textures);
                modalLayer.addActor(travelLogPanel);
            }));


        subRow.add(subLeftGroup).padTop(30).left();
        subRow.add().expandX();

        mainLayer.add(subRow).growX().padTop(5).padLeft(30).padRight(30).row();


        Table islandContainer = new Table();
        Image adventureIcon = UiFactory.imageFor(textures, Ids.GameScreen.ADVENTURE_ICON);
        adventureIcon.setScaling(Scaling.fit);

        adventureIcon.setTouchable(Touchable.enabled);

        ButtonAnimator.applyHoverAndClickEffect(adventureIcon, 1.05f, 0.95f, () -> {
            System.out.println("Adventure Island Clicked!");
            ScreenManager.getInstance().pushScreen(new ChapterSelectionScreen(game));
        });

        islandContainer.add(adventureIcon).size(500, 500).center();

        mainLayer.add(islandContainer).expand().fill().padTop(15);
    }


    private Stack createResourceDisplay(TextureBank textures, Skin skin, String imageId, String initialValue) {
        Stack displayStack = new Stack();

        Image bgImage = UiFactory.imageFor(textures, imageId);
        bgImage.setScaling(Scaling.fit);
        Container<Image> bgContainer = new Container<>(bgImage);
        bgContainer.size(200, 55);

        Label.LabelStyle labelStyle = skin.get(Label.LabelStyle.class);
        Label valueLabel = new Label(initialValue, labelStyle);
        valueLabel.setAlignment(Align.left | Align.center);

        Container<Label> labelContainer = new Container<>(valueLabel);
        labelContainer.padLeft(45);

        displayStack.add(bgContainer);
        displayStack.add(labelContainer);

        displayStack.setUserObject(valueLabel);

        return displayStack;
    }

    private TextButton createEarnButton(Skin skin, ButtonAnimator.OnClickListener clickListener) {
        TextButton earnBtn = new TextButton("Earn", skin, "green_small");

        ButtonAnimator.applyHoverAndClickEffect(earnBtn, 1.1f, 0.9f, clickListener);
        return earnBtn;
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
