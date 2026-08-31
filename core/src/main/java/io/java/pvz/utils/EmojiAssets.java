package io.java.pvz.utils;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;

import java.util.HashMap;
import java.util.Map;

public final class EmojiAssets {

    private static final String[] PATHS = {
        "emojis/Loudly Crying Face Emoji.png",
        "emojis/Tears of Joy Emoji.png",
        "emojis/Very Angry Emoji.png"
    };

    private static final Map<Integer, Texture> cache = new HashMap<>();

    private EmojiAssets() {
    }

    public static int count() {
        return PATHS.length;
    }

    public static Image imageFor(int index) {
        if (index < 0 || index >= PATHS.length) {
            Gdx.app.error("EmojiAssets", "No emoji registered for index " + index);
            return new Image();
        }

        Texture texture = cache.computeIfAbsent(index, i -> {
            String path = PATHS[i];
            if (!Gdx.files.internal(path).exists()) {
                Gdx.app.error("EmojiAssets", "Missing emoji file: " + path);
                return null;
            }
            Texture t = new Texture(Gdx.files.internal(path));
            t.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
            return t;
        });

        if (texture == null) {
            return new Image();
        }

        return new Image(new TextureRegionDrawable(new TextureRegion(texture)));
    }

    public static void dispose() {
        for (Texture t : cache.values()) {
            if (t != null) t.dispose();
        }
        cache.clear();
    }
}
