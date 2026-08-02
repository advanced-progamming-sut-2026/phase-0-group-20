package io.java.pvz;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.ScreenUtils;

import pvz.libpvz.pam.PamPlayer;
import pvz.libpvz.textures.TextureBank;

public class Pvz extends ApplicationAdapter {
    private SpriteBatch batch;

    private TextureBank textures;
    private PamPlayer player;
    private float stateTime;

    private static final String ZOMBIE_PAM = "768/INITIAL/ZOMBIE/ZOMBIE_TUTORIAL/ZOMBIE_TUTORIAL.PAM";
    private static final String CLIP_NAME = "walk";

    @Override
    public void create() {
        batch = new SpriteBatch();

        FileHandle assetsFolder = Gdx.files.internal("pvz2assets");

        textures = new TextureBank("768", assetsFolder);
        player = new PamPlayer(textures, assetsFolder);

        player.loadSync(ZOMBIE_PAM);
    }

    @Override
    public void render() {
        stateTime += Gdx.graphics.getDeltaTime();

        textures.update();

        ScreenUtils.clear(0.15f, 0.15f, 0.2f, 1f);
        batch.begin();
        player.draw(batch, ZOMBIE_PAM, CLIP_NAME, stateTime, 300, 200, true);
        batch.end();
    }

    @Override
    public void dispose() {
        batch.dispose();
        textures.dispose();
    }
}
