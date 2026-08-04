package io.java.pvz;

import com.badlogic.gdx.Game;
import io.java.pvz.controllers.ScreenManager;
import io.java.pvz.loader.AssetLoader;
import io.java.pvz.views.screens.AssetLoaderScreen;

public class Pvz extends Game {
    @Override
    public void create() {
//        AssetLoader.getInstance().init();

        ScreenManager.getInstance().initialize(this);
        ScreenManager.getInstance().setRootScreen(new AssetLoaderScreen(this));
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
