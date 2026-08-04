package io.java.pvz.views.screens;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Scaling;
import io.java.pvz.controllers.ButtonAnimator;
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
        logoTable.add(logo).size(900, 500);

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
        ButtonAnimator.applyHoverAndClickEffect(profileIcon, 1.1f, 0.9f, () -> {
            System.out.println("Profile Icon Clicked!");
            // TODO: push profile ui later
        });

        TextField.TextFieldStyle baseStyle = skin.get(TextField.TextFieldStyle.class);
        TextField.TextFieldStyle customFieldStyle = new TextField.TextFieldStyle(baseStyle);
        customFieldStyle.background = null;
        customFieldStyle.focusedBackground = null;

        customFieldStyle.font = skin.getFont("FBUSV8C5EI_1");

        TextField nameField = new TextField("eieio", customFieldStyle);
        nameField.setAlignment(Align.center);

        nameEntryTable.add(nameField).expandX().fillX().padRight(15);
        nameEntryTable.add(profileIcon).left().size(40, 40).pad(15);

        center.add(nameEntryTable).width(360).height(65).padBottom(30).row();

        mainLayer.add(buildButtons(textures, skin, center)).growX().padBottom(60).bottom();
    }

    private Table buildButtons(TextureBank textures, Skin skin, Table center) {

        TextButton playBtn = new TextButton("Play", skin, "purple");
        playBtn.getLabel().setFontScale(1.5f);
        ButtonAnimator.applyHoverAndClickEffect(playBtn, 1.1f, 0.9f, () -> {
            System.out.println("Play Button clicked!");
            // TODO : push game menu later
        });
        center.add(playBtn).prefSize(110).width(200).height(90).row();

        Table bottomContainer = new Table();

        Stack cloudBtn = createIconButton(textures, skin, Ids.MainMenu.CLOUD_ICON, () -> {
            System.out.println("Cloud Icon Clicked!");
            // TODO : push later
        });
        Stack newsBtn = createIconButton(textures, skin, Ids.MainMenu.NEWS_ICON, () -> {
            System.out.println("News Icon Clicked!");
            // TODO : push later
        });
        Stack leaderboardBtn = createIconButton(textures, skin, Ids.MainMenu.LEADERBOARD_ICON, () -> {
            System.out.println("Leader Board Clicked!");
            // TODO : push later
        });
        Stack settingsBtn = createIconButton(textures, skin, Ids.MainMenu.SETTINGS_ICON, () -> {
            System.out.println("Settings button clicked");
            // TODO : push later
        });

        bottomContainer.add(cloudBtn).padLeft(50).padRight(50).bottom();
        bottomContainer.add(newsBtn).bottom();

        bottomContainer.add(center).expandX().padBottom(70).center().bottom();

        bottomContainer.add(settingsBtn).padRight(50).bottom();
        bottomContainer.add(leaderboardBtn).padRight(50).bottom();

        return bottomContainer;
    }

    private Stack createIconButton(TextureBank textures,
                                   Skin skin, String iconId, ButtonAnimator.OnClickListener clickListener) {
        Stack stack = new Stack();
        stack.setTouchable(Touchable.enabled);

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

        bgContainer.setTouchable(Touchable.disabled);
        iconContainer.setTouchable(Touchable.disabled);

        stack.add(bgContainer);
        stack.add(iconContainer);

        ButtonAnimator.applyHoverAndClickEffect(stack, 1.5f, 0.9f, clickListener);

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
