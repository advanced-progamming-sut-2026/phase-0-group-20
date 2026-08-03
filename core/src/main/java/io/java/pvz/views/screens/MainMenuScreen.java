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

        Table logoTable = new Table();
        Image logo = imageFor(textures, Ids.MainMenu.LOGO);
        logo.setScaling(Scaling.fit);
        logoTable.add(logo).size(700, 400);

        mainLayer.add(logoTable).padTop(100).row();

        mainLayer.add().expandY().row();

        Table center = new Table();

        Table nameEntryTable = new Table();
        TextureRegion nameBgRegion = textures.region(Ids.MainMenu.NAME_ENTRY_ICON);
        if (nameBgRegion != null) {
            nameEntryTable.setBackground(new TextureRegionDrawable(nameBgRegion));
        }

        Image profileIcon = imageFor(textures, Ids.MainMenu.PROFILE_ICON);
        profileIcon.setScaling(Scaling.fit);

        TextField.TextFieldStyle baseStyle = skin.get(TextField.TextFieldStyle.class);
        TextField.TextFieldStyle customFieldStyle = new TextField.TextFieldStyle(baseStyle);
        customFieldStyle.background = null;
        customFieldStyle.focusedBackground = null;

        customFieldStyle.font = skin.getFont("FBUSV8C5EI_1");

        TextField nameField = new TextField("eieio", customFieldStyle);
        nameField.setAlignment(Align.center);

        nameEntryTable.add(nameField).expandX().fillX().padRight(15);
        nameEntryTable.add(profileIcon).left().size(40, 40).pad(15);


        center.add(nameEntryTable).width(360).height(65).padBottom(15).row();

        TextButton playBtn = new TextButton("Play", skin, "purple");
        playBtn.getLabel().setFontScale(1.5f);
        playBtn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                // TODO : push game screen later
            }
        });
        center.add(playBtn).prefSize(110).width(200).height(90).row();

        Table bottomContainer = new Table();

        Stack cloudBtn = createIconButton(textures, skin, Ids.MainMenu.CLOUD_ICON);
        Stack newsBtn = createIconButton(textures, skin, Ids.MainMenu.NEWS_ICON);
        Stack leaderboardBtn = createIconButton(textures, skin, Ids.MainMenu.LEADERBOARD_ICON);
        Stack settingsBtn = createIconButton(textures, skin, Ids.MainMenu.SETTINGS_ICON);

        bottomContainer.add(cloudBtn).padLeft(50).padRight(50).bottom();
        bottomContainer.add(newsBtn).bottom();

        bottomContainer.add(center).expandX().padBottom(50).center().bottom();

        bottomContainer.add(settingsBtn).padRight(50).bottom();
        bottomContainer.add(leaderboardBtn).padRight(50).bottom();

        mainLayer.add(bottomContainer).growX().padBottom(60).bottom();

        settingsBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                System.out.println("Settings button clicked");
            }
        });

        leaderboardBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                System.out.println("Leaderboard button clicked");
            }
        });
    }

    private Stack createIconButton(TextureBank textures, Skin skin, String iconId) {
        Stack stack = new Stack();

        Table bgTable = new Table();
        if (skin.has("image_ui_generic_brownbutton_10", Drawable.class)) {
            bgTable.setBackground(skin.getDrawable("image_ui_generic_brownbutton_10"));
        }
        Container<Table> bgContainer = new Container<>(bgTable);
        bgContainer.size(100, 100);

        Image icon = imageFor(textures, iconId);
        icon.setScaling(Scaling.fit);
        Container<Image> iconContainer = new Container<>(icon);
        iconContainer.size(100, 100);

        stack.add(bgContainer);
        stack.add(iconContainer);

        return stack;
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
