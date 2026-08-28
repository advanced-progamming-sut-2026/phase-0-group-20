package io.java.pvz.views.screens;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Scaling;
import io.java.pvz.controllers.ButtonAnimator;
import io.java.pvz.controllers.MenuScreenController;
import io.java.pvz.controllers.MenuController.MainMenuController;
import io.java.pvz.controllers.ScreenManager;
import io.java.pvz.loader.AssetLoader;
import io.java.pvz.models.App;
import io.java.pvz.utils.Ids;
import io.java.pvz.utils.UiFactory;
import io.java.pvz.views.screens.modals.*;
import pvz.libpvz.textures.TextureBank;
import pvz.skin.BorderedTable;

public class MainMenuScreen extends BaseScreen {

    private TextureRegion backgroundRegion;

    public MainMenuScreen(Game game) {
        super(game);
        buildUI();
    }

    private void buildUI() {
        TextureBank textures = AssetLoader.getInstance().getTextures();
        Skin skin = AssetLoader.getInstance().getSkin();
        backgroundRegion = textures.region(Ids.MainMenu.BACKGROUND);
        mainLayer.clear();
        mainLayer.setFillParent(true);
        Table center = new Table();
        mainLayer.add(buildTopButtons(textures, skin, center)).growX().top().row();

        Table logoTable = new Table();
        Image logo = UiFactory.imageFor(textures, Ids.MainMenu.LOGO);
        logo.setScaling(Scaling.fit);
        logoTable.add(logo).size(900, 500);

        mainLayer.add(logoTable).padTop(60).row();

        mainLayer.add().expandY().row();


        Table nameEntryTable = new Table();
        TextureRegion nameBgRegion = textures.region(Ids.MainMenu.NAME_ENTRY_ICON);
        if (nameBgRegion != null) {
            nameEntryTable.setBackground(new TextureRegionDrawable(nameBgRegion));
        }

        Image profileIcon = UiFactory.imageFor(textures, Ids.MainMenu.PROFILE_ICON);
        profileIcon.setScaling(Scaling.fit);
        ButtonAnimator.applyHoverAndClickEffect(profileIcon, 1.1f, 0.9f, () -> {
            System.out.println("Profile Icon Clicked!");
            ProfileModalTable profileModal = new ProfileModalTable(skin);
            profileModal.show(modalLayer, viewport);
        });

        TextField.TextFieldStyle baseStyle = skin.get(TextField.TextFieldStyle.class);
        TextField.TextFieldStyle customFieldStyle = new TextField.TextFieldStyle(baseStyle);
        customFieldStyle.background = null;
        customFieldStyle.focusedBackground = null;

        customFieldStyle.font = skin.getFont("FBUSV8C5EI_1");

        TextField nameField = new TextField(App.getActiveUser().getNickname(), customFieldStyle);
        nameField.setAlignment(Align.center);

        nameEntryTable.add(nameField).expandX().fillX().padRight(15);
        nameEntryTable.add(profileIcon).left().size(40, 40).pad(15);

        center.add(nameEntryTable).width(360).height(65).padBottom(30).row();

        mainLayer.add(buildBottomButtons(textures, skin, center)).growX().padBottom(60).bottom();
    }

    private Table buildTopButtons(TextureBank textures, Skin skin, Table center) {
        Table topContainer = new Table();

        BorderedTable borderedLogoutBtn = new BorderedTable();
        borderedLogoutBtn.setTouchable(Touchable.enabled);
        borderedLogoutBtn.pad(10, 30, 10, 30);
        Label.LabelStyle labelStyle = new Label.LabelStyle();
        labelStyle.font = skin.getFont("FBUSV8C5EI_1");
        labelStyle.fontColor = Color.BROWN;

        Label logoutLabel = new Label("Logout", labelStyle);
        logoutLabel.setFontScale(1.3f);

        borderedLogoutBtn.add(logoutLabel).center();

        ButtonAnimator.applyHoverAndClickEffect(borderedLogoutBtn, 1.1f, 0.9f, () -> {
            System.out.println("Logout Clicked!");
            new MainMenuController().logout();
            ScreenManager.getInstance().setRootScreen(new SignupScreen(game));
            ScreenManager.getInstance().pushScreen(new LoginScreen(game));
        });

        topContainer.add(center).expandX().padBottom(70).center().bottom();
        topContainer.add(borderedLogoutBtn).right().padRight(30).padTop(40).width(200).height(100).top();

        return topContainer;
    }

    private Table buildBottomButtons(TextureBank textures, Skin skin, Table center) {

        TextButton playBtn = new TextButton("Play", skin, "purple");
        playBtn.getLabel().setFontScale(1.5f);
        ButtonAnimator.applyHoverAndClickEffect(playBtn, 1.1f, 0.9f, () -> {
            System.out.println("Play Button clicked!");
            ScreenManager.getInstance().pushScreen(new GameMenuScreen(game));

        });
        center.add(playBtn).prefSize(110).width(200).height(90).row();

        Table bottomContainer = new Table();

        Stack newsBtn = UiFactory.iconButton(textures, skin, Ids.MainMenu.NEWS_ICON, 100, 100,
            () -> {
                System.out.println("News Icon Clicked!");
                new NewsModalTable(skin).show(modalLayer,viewport);
            });
        Stack leaderboardBtn = UiFactory.iconButton(textures, skin, Ids.MainMenu.LEADERBOARD_ICON, 100, 100,
            () -> {
                System.out.println("Leader Board Clicked!");
                modalLayer.clear();

                Table leaderboardTable = LeaderboardMenu.build(
                    new MenuScreenController(modalLayer) {
                        @Override
                        public void goBack() {
                            modalLayer.clear();
                        }
                    },
                    textures,
                    skin
                );

                modalLayer.addActor(leaderboardTable);
            });
        Stack settingsBtn = UiFactory.iconButton(textures, skin, Ids.MainMenu.SETTINGS_ICON, 100, 100,
            () -> {
                System.out.println("Settings button clicked");
                new SettingModalTable(skin).show(modalLayer, viewport);
            });

        bottomContainer.add(newsBtn).padLeft(50).padRight(50).bottom();
        bottomContainer.add().bottom();
        bottomContainer.add(center).expandX().padBottom(70).center().bottom();
        bottomContainer.add(settingsBtn).padRight(50).bottom();
        bottomContainer.add(leaderboardBtn).padRight(50).bottom();

        return bottomContainer;
    }

    @Override
    public void render(float delta) {
        clearScreen(0.05f, 0.05f, 0.1f, 1f);

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
