package io.java.pvz.utils;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.scenes.scene2d.Actor;
import io.java.pvz.loader.AssetLoader;
import io.java.pvz.models.entities.zombies.Zombie;
import io.java.pvz.models.entities.zombies.ZombieType;
import io.java.pvz.models.entities.zombies.armour.Armor;
import pvz.libpvz.pam.PamPlayer;

import java.util.HashMap;
import java.util.Map;

public class PamAnimatedActor extends Actor {
    private final PamPlayer player;
    private String clipName;
    private String successfulPath = null;
    private float stateTime = 0f;
    private boolean isLoaded = false;
    private Map<String, Boolean> visibilityMap = null;
    private boolean paused = false;

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
        PamPlayer player = AssetLoader.getInstance().getPlayer();

        AnimationCatalog.EntityAnimation animData = AnimationCatalog.getPlantAnimation(atlasName);

        if (animData != null) {
            return new PamAnimatedActor(player, clipName, animData.path);
        }

        System.err.println("⚠️ " + atlasName + " not found in Catalog. Guessing paths...");
        String cleanAtlas = atlasName.toUpperCase();
        String pamPath1 = "768/INITIAL/PLANT/" + cleanAtlas + "/" + cleanAtlas + ".PAM";
        String pamPath2 = "768/FULL/PLANT/" + cleanAtlas + "/" + cleanAtlas + ".PAM";

        return new PamAnimatedActor(player, clipName, pamPath1, pamPath2);
    }

    public static PamAnimatedActor createPlantIdle(String atlasName) {
        AnimationCatalog.EntityAnimation animData = AnimationCatalog.getPlantAnimation(atlasName);

        String clipName = "idle";

        if (animData != null) {
            if (!animData.hasClip("idle")) {
                if (animData.hasClip("idle_stage1")) {
                    clipName = "idle_stage1";
                } else if (animData.hasClip("idle1_1")) {
                    clipName = "idle1_1";
                } else if (!animData.getClipNames().isEmpty()) {
                    clipName = animData.getClipNames().iterator().next();
                }
            }
        }

        return createPlantAnimated(atlasName, clipName);
    }

    public static PamAnimatedActor createZombieAnimated(ZombieType type, String clipName) {
        PamPlayer player = AssetLoader.getInstance().getPlayer();

        AnimationCatalog.EntityAnimation animData = AnimationCatalog.getZombieAnimation(type);

        if (animData != null) {
            return new PamAnimatedActor(player, clipName, animData.path);
        }

        System.err.println("⚠️ Zombie " + type + " not found in Catalog. Guessing paths...");
        String cleanAddress = "ZOMBIE_" + type.name();
        String pamPath1 = "768/FULL/ZOMBIE/" + cleanAddress + "/" + cleanAddress + ".PAM";
        String pamPath2 = "768/INITIAL/ZOMBIE/" + cleanAddress + "/" + cleanAddress + ".PAM";

        return new PamAnimatedActor(player, clipName, pamPath1, pamPath2);
    }

    public static PamAnimatedActor createZombieIdle(ZombieType type) {
        AnimationCatalog.EntityAnimation animData = AnimationCatalog.getZombieAnimation(type);

        String clipName = "idle";
        if (animData != null) {
            if (!animData.hasClip("idle")) {
                if (animData.hasClip("idle_newspaper")) {
                    clipName = "idle_newspaper";
                } else if (animData.hasClip("walk")) {
                    clipName = "walk";
                } else if (!animData.getClipNames().isEmpty()) {
                    clipName = animData.getClipNames().iterator().next();
                }
            }
        }

        return createZombieAnimated(type, clipName);
    }

    public void setVisibilityMap(Map<String, Boolean> visibilityMap) {
        this.visibilityMap = visibilityMap;
    }

    public void setClip(String clipName) {
        if (clipName == null || clipName.equals(this.clipName)) return;
        this.clipName = clipName;
        this.stateTime = 0f;
    }

    public void setPaused(boolean paused) {
        this.paused = paused;
    }

    public boolean isPaused() {
        return paused;
    }

    public String getClip() {
        return clipName;
    }

    public boolean isLoaded() {
        return isLoaded;
    }

    public PamPlayer getPlayer() {
        return player;
    }

    public String getPamPath() {
        return successfulPath;
    }

    public float getStateTime() {
        return stateTime;
    }

    public float getDrawX() {
        return getX() + (getWidth() / 2f);
    }

    public float getDrawY() {
        return getY();
    }

    @Override
    public void act(float delta) {
        super.act(delta);
        if (isLoaded && !paused) {
            stateTime += delta;
        }
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        if (isLoaded && player != null && successfulPath != null) {
            float drawX = getX() + (getWidth() / 2f);
            float drawY = getY();

            Matrix4 originalMatrix = batch.getTransformMatrix().cpy();
            com.badlogic.gdx.graphics.Color originalColor = batch.getColor().cpy();

            Matrix4 scaledMatrix = originalMatrix.cpy()
                .translate(drawX, drawY, 0)
                .scale(getScaleX(), getScaleY(), 1f)
                .translate(-drawX, -drawY, 0);

            batch.setTransformMatrix(scaledMatrix);
            batch.setColor(getColor().r, getColor().g, getColor().b, getColor().a * parentAlpha);

            try {
                if (visibilityMap != null) {
                    player.draw(batch, successfulPath, clipName, stateTime, drawX, drawY, true, visibilityMap);
                } else {
                    player.draw(batch, successfulPath, clipName, stateTime, drawX, drawY, true);
                }
            } catch (Exception e) {
                System.err.println("❌ Rendering Error for PAM: " + successfulPath + " - " + e.getMessage());
                isLoaded = false;
            } finally {
                batch.setTransformMatrix(originalMatrix);
                batch.setColor(originalColor);
            }
        }
    }

    public static PamAnimatedActor createEffectAnimated(String pamPath, String clipName) {
        PamPlayer player = AssetLoader.getInstance().getPlayer();
        return new PamAnimatedActor(player, clipName, pamPath);
    }

    public void applyZombieArmor(Zombie zombie) {
        Map<String, Boolean> currentVisibility = new HashMap<>();

        if (zombie.getArmorPieces() != null) {
            for (Armor armor : zombie.getArmorPieces()) {
                if (!armor.isDestroyed()) {
                    int damageLayer = armor.getDamageLayer();
                    String state = armor.getData().getArmorLayer(damageLayer);
                    if (state != null) currentVisibility.put(state, true);

                    String group = armor.getData().getArmorLayerGroup();
                    if (group != null) currentVisibility.put(group, true);
                }
            }
        }

        if (!currentVisibility.isEmpty()) {
            this.setVisibilityMap(currentVisibility);
        }
    }
}
