package io.java.pvz.loader;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import pvz.libpvz.pam.PamPlayer;
import pvz.libpvz.textures.TextureBank;

public class AssetLoader {
    private static AssetLoader instance;
    private TextureBank textures;
    private PamPlayer player;
    public static AssetLoader getInstance ()
    {
        if (instance == null)
        {
            instance = new AssetLoader ();
        }
        return instance;
    }
    public void init(){
        if (textures == null && player == null) {
            FileHandle assetsFolder = Gdx.files.internal("pvz2assets");
            textures = new TextureBank("768", assetsFolder);
            player = new PamPlayer(textures, assetsFolder);
        }
    }

    //load animation
    public void loadPamSync(String pamPath) {
        player.loadSync(pamPath);
    }

    // update animation
    public void updateTextures() {
        if (textures != null) {
            textures.update();
        }
    }

    public PamPlayer getPlayer() {
        return player;
    }

    public void dispose() {
        if (textures != null) {
            textures.dispose();
        }
    }




}
