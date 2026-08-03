package io.java.pvz.views.screens;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Scaling;
import io.java.pvz.loader.AssetLoader;
import io.java.pvz.utils.Ids;
import pvz.libpvz.textures.TextureBank;

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

        Table root = new Table();
        root.setFillParent(true);
        Table logoTable = new Table();

        Image logo = imageFor(textures, Ids.MainMenu.LOGO);
        logo.setScaling(Scaling.fit);
        logoTable.add(logo).size(700, 400);

        root.add(logoTable).padTop(200).row();
        root.add().height(100).expandY().row();

        Table center = new Table();
        Table nameEntryTable = new Table();
        TextureRegion nameBgRegion = textures.region(Ids.MainMenu.NAME_ENTRY_ICON);
        nameEntryTable.setBackground(new TextureRegionDrawable(nameBgRegion));
        Image profileIcon = imageFor(textures, Ids.MainMenu.PROFILE_ICON);
        profileIcon.setScaling(Scaling.fit);

        TextField.TextFieldStyle baseStyle = skin.get(TextField.TextFieldStyle.class);
        TextField.TextFieldStyle customFieldStyle = new TextField.TextFieldStyle(baseStyle);
        customFieldStyle.background = null;
        customFieldStyle.focusedBackground = null;

        TextField nameField = new TextField("eieio", skin);
        nameField.setAlignment(Align.center);
        nameEntryTable.add(nameField).expandX().fillX().padRight(15).padLeft(15);
        nameEntryTable.add(profileIcon).right().size(40, 40).pad(15);

        center.add(nameEntryTable).width(360).height(65).padBottom(15).row();

        TextButton playBtn = new TextButton("Play", skin, "purple");

        playBtn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                // TODO : push game screen later
            }
        });
        center.add(playBtn).prefSize(110).width(200).height(90).row();
        root.add(center).expand().center().padBottom(40).row();

        Table bottomContainer = new Table();
        Table cloudBtn = createIconButton(textures, skin, Ids.MainMenu.CLOUD_ICON);
        Table newsBtn = createIconButton(textures, skin, Ids.MainMenu.NEWS_ICON);
        Table leaderboardBtn = createIconButton(textures, skin, Ids.MainMenu.LEADERBOARD_ICON);
        Table settingsBtn = createIconButton(textures, skin, Ids.MainMenu.SETTINGS_ICON);

        bottomContainer.add(cloudBtn).padLeft(20).padRight(10);
        bottomContainer.add(newsBtn);

        bottomContainer.add().expandX();

        bottomContainer.add(settingsBtn).padRight(10);
        bottomContainer.add(leaderboardBtn).padRight(20);

        root.add(bottomContainer).growX().padBottom(20).bottom();

        mainLayer.add(root).growX().padBottom(20).bottom();

        settingsBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                System.out.println("Settings button clicked");
            }
        });
        leaderboardBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                System.out.println("leaderboard button clicked");
            }
        });
    }

    private Table createIconButton(TextureBank textures, Skin skin, String iconId) {
        Table buttonFrame = new Table();

        if (skin.has("image_ui_generic_brownbutton_10", Drawable.class)) {
            buttonFrame.setBackground(skin.getDrawable("image_ui_generic_brownbutton_10"));
        }

        Image icon = imageFor(textures, iconId);
        icon.setScaling(Scaling.fit);

        buttonFrame.add(icon).size(50, 50);

        return buttonFrame;
    }

    private Image imageFor(TextureBank textures, String imageId) {
        TextureRegion region = textures.region(imageId);
        if (region == null) {
            Gdx.app.error("MainMenuScreen", "Missing image resource: " + imageId);
            return new Image();
        }
        return new Image(new TextureRegionDrawable(region));
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
