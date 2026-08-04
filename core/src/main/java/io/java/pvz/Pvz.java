package io.java.pvz;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.ScreenUtils;
import io.java.pvz.controllers.ScreenManager;
import io.java.pvz.loader.AssetLoader;
import io.java.pvz.views.screens.MainMenuScreen;
import io.java.pvz.views.screens.SignupScreen;

public class Pvz extends Game {
    @Override
    public void create() {
        AssetLoader.getInstance().init();

        ScreenManager.getInstance().initialize(this);
        ScreenManager.getInstance().setRootScreen(new SignupScreen(this));
    }

    @Override
    public void render() {
        AssetLoader.getInstance().updateTextures();
        super.render();
    }

    @Override
    public void dispose() {
        if (screen != null) screen.dispose();
        AssetLoader.getInstance().dispose();
    }
}
