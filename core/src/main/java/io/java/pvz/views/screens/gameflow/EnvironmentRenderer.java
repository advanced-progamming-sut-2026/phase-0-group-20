package io.java.pvz.views.screens.gameflow;

import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import io.java.pvz.loader.AssetLoader;
import io.java.pvz.models.entities.obstacle.GraveHolder;
import io.java.pvz.models.fields.tiles.*;
import io.java.pvz.models.game.Arena;
import io.java.pvz.models.game.GameSession;
import io.java.pvz.models.game.adventure.SeasonType;
import io.java.pvz.utils.Ids;
import io.java.pvz.utils.PamAnimatedActor;
import io.java.pvz.utils.UiFactory;

import java.util.*;

import static io.java.pvz.models.enums.PhysicalConstants.*;

public class EnvironmentRenderer {

    private final Group layerGroup;

    private final Map<Tile, PamAnimatedActor> graveActors = new HashMap<>();
    private final Map<Tile, PamAnimatedActor> slipperyActors = new HashMap<>();
    private final Map<Tile, PamAnimatedActor> waterActors = new HashMap<>();
    private final Map<Tile, PamAnimatedActor> vaseActors = new HashMap<>();
    private final Map<Tile, PamAnimatedActor> craterActors = new HashMap<>();

    private final Map<Tile, Image> firedActors = new HashMap<>();
    private PamAnimatedActor bigWaveForeground;
    PamAnimatedActor maxLineAnimation;
    private final Map<Tile, Image> darkOverlayActors = new HashMap<>();
    private final Texture darkTexture;

    public EnvironmentRenderer(Group layerGroup) {
        this.layerGroup = layerGroup;

        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(0, 0, 0, 0.35f);
        pixmap.fill();
        darkTexture = new Texture(pixmap);
        pixmap.dispose();
    }

    public void sync(Arena arena) {
        if (arena == null) return;

        Set<Tile> activeGraves = new HashSet<>();
        Set<Tile> activeSlippery = new HashSet<>();
        Set<Tile> activeWater = new HashSet<>();
        Set<Tile> activeVases = new HashSet<>();
        Set<Tile> activeDarkTiles = new HashSet<>();
        Set<Tile> activeCraters = new HashSet<>();
        Set<Tile> activeFiredTiles = new HashSet<>();

        List<Tile> allTiles = new ArrayList<>();

        for (Tile[] row : arena.getTiles()) {
            for (Tile tile : row) {
                allTiles.add(tile);
                float pixelX = tile.getCol() * TILE_WIDTH + GRID_START_X;
                float pixelY = tile.getRow() * TILE_HEIGHT + GRID_START_Y;

                syncCrater(tile, activeCraters, pixelX, pixelY);
                syncFiredTile(tile, activeFiredTiles, pixelX, pixelY);
                syncDarkTile(tile, activeDarkTiles, pixelX, pixelY);
                syncSlipperyTile(tile, activeSlippery, pixelX, pixelY);
                syncGraveTile(tile, activeGraves, pixelX, pixelY);
                syncVaseTile(tile, activeVases, pixelX, pixelY);
            }
        }

        despawnMissingImages(darkOverlayActors, activeDarkTiles);
        despawnMissingImages(firedActors, activeFiredTiles);

        syncBigWaveBeach(arena, allTiles);

        despawnMissingTiles(graveActors, activeGraves);
        despawnMissingTiles(slipperyActors, activeSlippery);
        despawnMissingTiles(waterActors, activeWater);
        despawnMissingTiles(vaseActors, activeVases);
        despawnMissingTiles(craterActors, activeCraters);
    }

    public void clear() {
        layerGroup.clearChildren();
        graveActors.clear();
        slipperyActors.clear();
        waterActors.clear();
        vaseActors.clear();
        firedActors.clear();
        craterActors.clear();
    }

    private void syncCrater(Tile tile, Set<Tile> activeCraters, float pixelX, float pixelY) {
        if (tile.isCrater()&& !tile.isFired()) {
            activeCraters.add(tile);
            PamAnimatedActor actor = craterActors.computeIfAbsent(tile, t -> {
                PamAnimatedActor animatedActor = PamAnimatedActor.createEffectAnimated(
                    "768/FULL/BACKGROUNDS/GOLDTILE/GOLDTILE.PAM", "active_idle"
                );
                animatedActor.setSize(TILE_WIDTH * 1.3f, TILE_HEIGHT * 1.3f);
                layerGroup.addActor(animatedActor);
                animatedActor.toBack();
                return animatedActor;
            });
            centerOnPoint(actor, pixelX + TILE_WIDTH / 2f, pixelY + TILE_HEIGHT / 2f + TILE_HEIGHT);
        }
    }

    private void syncFiredTile(Tile tile, Set<Tile> activeFiredTiles, float pixelX, float pixelY) {
        if (tile.isFired()) {
            activeFiredTiles.add(tile);
            Image firedImg = firedActors.computeIfAbsent(tile, t -> {
                Image img = UiFactory.imageFor(AssetLoader.getInstance().getTextures(),
                    "IMAGE_ZOMBIE_ZOMBIE_DARK_ZOMBOSS_ZOMBIE_DARK_ZOMBOSS_177X196");
                img.setSize(TILE_WIDTH + 60f, TILE_HEIGHT + 80f);
                layerGroup.addActor(img);
                return img;
            });
            firedImg.setPosition(pixelX - 30f, pixelY - 40f);
            firedImg.toBack();
        }
    }

    private void syncDarkTile(Tile tile, Set<Tile> activeDarkTiles, float pixelX, float pixelY) {
        boolean shouldBeDark = tile instanceof NecromanceTile ||
            (tile instanceof LowShoreTile lt && !lt.isFlooded());

        if (shouldBeDark) {
            activeDarkTiles.add(tile);
            Image overlay = darkOverlayActors.computeIfAbsent(tile, t -> {
                Image img = new Image(darkTexture);
                img.setSize(TILE_WIDTH, TILE_HEIGHT);
                layerGroup.addActor(img);
                return img;
            });
            overlay.setPosition(pixelX, pixelY);
            overlay.toBack();
        }
    }

    private void syncSlipperyTile(Tile tile, Set<Tile> activeSlippery, float pixelX, float pixelY) {
        if (tile instanceof SlipperyTile st) {
            activeSlippery.add(tile);
            PamAnimatedActor actor = slipperyActors.computeIfAbsent(tile, t -> {
                String dir = st.getDirection() != SlipperyTile.SlideDirection.DOWN ?
                    Ids.ArenaEffects.TILESLIDER_DOWN :
                    Ids.ArenaEffects.TILESLIDER_UP;
                PamAnimatedActor animatedActor = PamAnimatedActor.createEffectAnimated(dir, "idle");
                layerGroup.addActor(animatedActor);
                animatedActor.toBack();
                return animatedActor;
            });
            centerOnPoint(actor, pixelX + TILE_WIDTH / 2f, pixelY + TILE_HEIGHT / 2);
        }
    }

    private void syncGraveTile(Tile tile, Set<Tile> activeGraves, float pixelX, float pixelY) {
        if (tile instanceof GraveHolder gh && gh.getGraveStone() != null) {
            activeGraves.add(tile);
            PamAnimatedActor actor = graveActors.computeIfAbsent(tile, t -> {
                PamAnimatedActor animatedActor = PamAnimatedActor.createEffectAnimated(
                    Ids.ArenaEffects.GRAVE, "undamaged");
                animatedActor.setScale(0.85f);
                layerGroup.addActor(animatedActor);
                return animatedActor;
            });
            actor.setClip(resolveGraveClip(gh.getGraveStone().getHp()));
            centerOnPoint(actor, pixelX + TILE_WIDTH / 2f, pixelY + TILE_HEIGHT / 2);
        }
    }

    private void syncVaseTile(Tile tile, Set<Tile> activeVases, float pixelX, float pixelY) {
        if (tile instanceof VaseTile vt && !vt.isBroken()) {
            activeVases.add(tile);
            if (tile instanceof RandomVaseTile) {
                PamAnimatedActor actor = vaseActors.computeIfAbsent(tile, t -> {
                    PamAnimatedActor animatedActor = PamAnimatedActor.createEffectAnimated(
                        Ids.ArenaEffects.VASE_RANDOM, "idle");
                    animatedActor.setScale(0.8f);
                    layerGroup.addActor(animatedActor);
                    return animatedActor;
                });
                centerOnPoint(actor, pixelX + TILE_WIDTH / 2f, pixelY + TILE_HEIGHT / 2f);
            } else if (tile instanceof PlantVaseTile) {
                PamAnimatedActor actor = vaseActors.computeIfAbsent(tile, t -> {
                    PamAnimatedActor animatedActor = PamAnimatedActor.createEffectAnimated(
                        Ids.ArenaEffects.VASE_PLANTS, "idle");
                    layerGroup.addActor(animatedActor);
                    animatedActor.setScale(0.8f);
                    return animatedActor;
                });
                centerOnPoint(actor, pixelX + TILE_WIDTH / 2f, pixelY + TILE_HEIGHT / 2f);
            } else {
                PamAnimatedActor actor = vaseActors.computeIfAbsent(tile, t -> {
                    PamAnimatedActor animatedActor = PamAnimatedActor.createEffectAnimated(
                        Ids.ArenaEffects.VASE_ZOMBIE, "idle");
                    layerGroup.addActor(animatedActor);
                    animatedActor.setScale(0.8f);
                    return animatedActor;
                });
                centerOnPoint(actor, pixelX + TILE_WIDTH / 2f, pixelY + TILE_HEIGHT / 2f);
            }
        }
    }

    private void syncBigWaveBeach(Arena arena, List<Tile> allTiles) {
        if (GameSession.getInstance() != null &&
            GameSession.getInstance().getCurrentChapter().getSeasonType() == SeasonType.BIG_WAVE_BEACH) {
            float gridCenterY = GRID_START_Y + (arena.getRows() * TILE_HEIGHT) / 2f;

            int maxCol = allTiles.stream()
                .filter(t -> t instanceof LowShoreTile)
                .mapToInt(Tile::getCol)
                .min()
                .orElse(arena.getCols());

            if (maxLineAnimation == null) {
                maxLineAnimation = PamAnimatedActor.createEffectAnimated(Ids.ArenaEffects.WATER_MAX_LINE, "idle");
                layerGroup.addActor(maxLineAnimation);
            }

            maxLineAnimation.setScale(0.7f);
            maxLineAnimation.setPosition((maxCol + 1) * TILE_WIDTH + GRID_START_X - TILE_WIDTH / 2f - 15,
                gridCenterY + 40);

            int minWaterCol = allTiles.stream()
                .filter(t -> t instanceof WaterTile || (t instanceof LowShoreTile lt && lt.isFlooded()))
                .mapToInt(Tile::getCol)
                .min()
                .orElse(arena.getCols());

            if (bigWaveForeground == null) {
                bigWaveForeground = PamAnimatedActor.createEffectAnimated(Ids.ArenaEffects.WATER_FORE_GROUND, "water");
                layerGroup.addActor(bigWaveForeground);
                layerGroup.addActor(bigWaveForeground);
                layerGroup.addActor(bigWaveForeground);
                layerGroup.addActor(bigWaveForeground);
                layerGroup.addActor(bigWaveForeground);
                layerGroup.addActor(bigWaveForeground);
            }

            bigWaveForeground.setScaleY(0.75f);

            float targetX = (minWaterCol + 5) * TILE_WIDTH + GRID_START_X;
            float currentX = bigWaveForeground.getX();

            if (currentX == 0) currentX = targetX;
            currentX += (targetX - currentX) * 0.05f;
            bigWaveForeground.setPosition(currentX, gridCenterY);
        }
    }

    private String resolveGraveClip(int currentHp) {
        if (currentHp > 525) return "undamaged";
        if (currentHp > 350) return "damage1";
        if (currentHp > 175) return "damage2";
        return "damage3";
    }

    private void despawnMissingTiles(Map<Tile, PamAnimatedActor> actorMap, Set<Tile> activeTiles) {
        Iterator<Map.Entry<Tile, PamAnimatedActor>> it = actorMap.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<Tile, PamAnimatedActor> entry = it.next();
            if (!activeTiles.contains(entry.getKey())) {
                entry.getValue().remove();
                it.remove();
            }
        }
    }

    private void despawnMissingImages(Map<Tile, Image> actorMap, Set<Tile> activeTiles) {
        Iterator<Map.Entry<Tile, Image>> it = actorMap.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<Tile, Image> entry = it.next();
            if (!activeTiles.contains(entry.getKey())) {
                entry.getValue().remove();
                it.remove();
            }
        }
    }

    private void centerOnPoint(PamAnimatedActor actor, float pixelX, float pixelY) {
        actor.setPosition(pixelX - actor.getWidth() / 2f, pixelY - actor.getHeight() / 2f);
    }
}
