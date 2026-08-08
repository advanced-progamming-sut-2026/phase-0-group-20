package io.java.pvz.utils;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.scenes.scene2d.Actor;
import io.java.pvz.loader.AssetLoader;
import pvz.libpvz.pam.PamPlayer;

public class PamAnimatedActor extends Actor {
    private final PamPlayer player;
    private String clipName;
    private String successfulPath = null;
    private float stateTime = 0f;
    private boolean isLoaded = false;

    public PamAnimatedActor(PamPlayer player, String clipName, String... pamPaths) {
        this.player = player;
        this.clipName = clipName;

        for (String path : pamPaths) {
            try {
                AssetLoader.getInstance().loadPamSync(path);
                this.successfulPath = path;
                this.isLoaded = true;
                break;
            } catch (Exception e) {
                System.err.println("⚠️ Fallback: Failed to load PAM from: " + path);
            }
        }

        if (!isLoaded) {
            System.err.println("❌ Critical Error: Could not load PAM animation from any of the provided paths.");
        }
    }

    public static PamAnimatedActor createPlantAnimated(String atlasName, String clipName) {
        String cleanAtlas = atlasName.toUpperCase();
        String pamPath1 = "768/INITIAL/PLANT/" + cleanAtlas + "/" + cleanAtlas + ".PAM";
        String pamPath2 = "768/FULL/PLANT/" + cleanAtlas + "/" + cleanAtlas + ".PAM";

        PamPlayer player = AssetLoader.getInstance().getPlayer();
        return new PamAnimatedActor(player, clipName, pamPath1, pamPath2);
    }

    public static PamAnimatedActor createPlantIdle(String atlasName) {
        return createPlantAnimated(atlasName, "idle");
    }

    public static PamAnimatedActor createZombieAnimated(String zombieAddress, String clipName) {
        String cleanAddress = "ZOMBIE_" + zombieAddress;
        String pamPath1 = "768/FULL/ZOMBIE/" + cleanAddress + "/" + cleanAddress + ".PAM";
        String pamPath2 = "768/INITIAL/ZOMBIE/" + cleanAddress + "/" + cleanAddress + ".PAM";

        PamPlayer player = AssetLoader.getInstance().getPlayer();
        return new PamAnimatedActor(player, clipName, pamPath1, pamPath2);
    }

    public void setClip(String clipName) {
        if (clipName == null || clipName.equals(this.clipName)) return;
        this.clipName = clipName;
        this.stateTime = 0f;
    }

    public String getClip() {
        return clipName;
    }

    public boolean isLoaded() {
        return isLoaded;
    }

    @Override
    public void act(float delta) {
        super.act(delta);
        if (isLoaded) {
            stateTime += delta;
        }
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        if (isLoaded && player != null && successfulPath != null) {
            float drawX = getX() + (getWidth() / 2f);
            float drawY = getY();

            Matrix4 originalMatrix = batch.getTransformMatrix().cpy();

            Matrix4 scaledMatrix = originalMatrix.cpy()
                .translate(drawX, drawY, 0)
                .scale(getScaleX(), getScaleY(), 1f)
                .translate(-drawX, -drawY, 0);

            batch.setTransformMatrix(scaledMatrix);

            try {
                player.draw(batch, successfulPath, clipName, stateTime, drawX, drawY, true);
            } catch (Exception e) {
                System.err.println("❌ Rendering Error for PAM: " + successfulPath + " - " + e.getMessage());
                isLoaded = false;
            } finally {
                batch.setTransformMatrix(originalMatrix);
            }
        }
    }
}
