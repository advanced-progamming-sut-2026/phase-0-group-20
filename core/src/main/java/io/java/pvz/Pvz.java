package io.java.pvz;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.ScreenUtils;
import io.java.pvz.loader.AssetLoader;

public class Pvz extends Game {
    private SpriteBatch batch;
    private float stateTime;

    private static final String ZOMBIE_PAM = "768/INITIAL/ZOMBIE/EGYPT_GARGANTUAR/EGYPT_GARGANTUAR.PAM";
    private static final String CLIP_NAME = "walk";

    @Override
    public void create() {
        batch = new SpriteBatch();

        AssetLoader.getInstance().init();

        AssetLoader.getInstance().loadPamSync(ZOMBIE_PAM);
    }

    @Override
    public void render() {
        stateTime += Gdx.graphics.getDeltaTime();

        AssetLoader.getInstance().updateTextures();

        ScreenUtils.clear(0.15f, 0.15f, 0.2f, 1f);
        batch.begin();

        AssetLoader.getInstance().getPlayer().draw(
            batch, ZOMBIE_PAM, CLIP_NAME, stateTime, 300, 200, true
        );

        batch.end();
    }

    @Override
    public void dispose() {
        batch.dispose();
        // آزادسازی منابع لودر
        AssetLoader.getInstance().dispose();
    }
}
