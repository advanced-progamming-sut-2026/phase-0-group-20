package io.java.pvz.views.screens;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
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
        mainLayer.top();

        Image logo = imageFor(textures, Ids.MainMenu.LOGO);
        mainLayer.add(logo).padTop(40).row();

        mainLayer.add().expand().row();

        TextField nameField = new TextField("eieio", skin);
        mainLayer.add(nameField).width(320).height(64).padBottom(16).row();

        TextButton playBtn = new TextButton("Play", skin, "purple");

        playBtn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                // TODO : push game screen later
            }
        });
        mainLayer.add(playBtn).width(280).height(90).padBottom(30).row();

        Table bottomRow = new Table();
        bottomRow.add(imageFor(textures, Ids.MainMenu.CLOUD_ICON)).size(56).padRight(10);
        bottomRow.add(imageFor(textures, Ids.MainMenu.NEWS_ICON)).size(56);
        bottomRow.add().expandX();
        bottomRow.add(imageFor(textures, Ids.MainMenu.SETTINGS_ICON)).size(56);

        mainLayer.add(bottomRow).growX().padBottom(20).padLeft(20).padRight(20);
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
