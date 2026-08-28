package io.java.pvz;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Cursor;
import com.badlogic.gdx.graphics.Pixmap;
import io.java.pvz.controllers.ScreenManager;
import io.java.pvz.loader.AssetLoader;
import io.java.pvz.views.screens.AssetLoaderScreen;

public class Pvz extends Game {
    @Override
    public void create() {
        ScreenManager.getInstance().initialize(this);
        ScreenManager.getInstance().setRootScreen(new AssetLoaderScreen(this));
        setCursor();
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

    private void setCursor() {
        Pixmap original = new Pixmap(Gdx.files.internal(
            "Icon_And_Cursor/Plants vs. Zombies Crazy Dave Cursor - pointer - SweezyCursors.png"));

        int newWidth = original.getWidth() * 2;
        int newHeight = original.getHeight() * 2;

        Pixmap cursorPixmap = new Pixmap(newWidth, newHeight, Pixmap.Format.RGBA8888);

        cursorPixmap.drawPixmap(
            original,
            0, 0, original.getWidth(), original.getHeight(),
            0, 0, newWidth, newHeight
        );

        Cursor customCursor = Gdx.graphics.newCursor(cursorPixmap, 0, 0);
        Gdx.graphics.setCursor(customCursor);

        original.dispose();
        cursorPixmap.dispose();
    }
}
