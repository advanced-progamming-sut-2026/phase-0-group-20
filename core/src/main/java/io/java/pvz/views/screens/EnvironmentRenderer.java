package io.java.pvz.views.screens;

import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import io.java.pvz.models.fields.obstacle.GraveHolder;
import io.java.pvz.models.fields.tiles.*;
import io.java.pvz.models.game.Arena;
import io.java.pvz.models.game.GameSession;
import io.java.pvz.models.game.adventure.SeasonType;
import io.java.pvz.utils.Ids;
import io.java.pvz.utils.PamAnimatedActor;

import java.util.*;

import static io.java.pvz.models.enums.PhysicalConstants.*;

public class EnvironmentRenderer {

    private final Group layerGroup;

    private final Map<Tile, PamAnimatedActor> graveActors = new HashMap<>();
    private final Map<Tile, PamAnimatedActor> slipperyActors = new HashMap<>();
    private final Map<Tile, PamAnimatedActor> waterActors = new HashMap<>();
    private final Map<Tile, PamAnimatedActor> vaseActors = new HashMap<>();

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

        List<Tile> allTiles = new ArrayList<>();

        for (Tile[] row : arena.getTiles()) {
            for (Tile tile : row) {
                allTiles.add(tile);
                float pixelX = tile.getCol() * TILE_WIDTH + GRID_START_X;
                float pixelY = tile.getRow() * TILE_HEIGHT + GRID_START_Y;

                boolean shouldBeDark = tile instanceof NecromanceTile ||
                    (tile instanceof LowShoreTile lt && !lt.isFlooded());

                if (shouldBeDark) {
                    activeDarkTiles.add(tile);
                    Image overlay = darkOverlayActors.computeIfAbsent(tile, t -> {
                        Image img = new Image(darkTexture);
                        img.setSize(TILE_WIDTH, TILE_HEIGHT);
                        layerGroup.addActor(img);
                        img.toBack();
                        return img;
                    });
                    overlay.setPosition(pixelX, pixelY);
                }

                Iterator<Map.Entry<Tile, Image>> darkIt = darkOverlayActors.entrySet().iterator();
                while (darkIt.hasNext()) {
                    Map.Entry<Tile, Image> entry = darkIt.next();
                    if (!activeDarkTiles.contains(entry.getKey())) {
                        entry.getValue().remove();
                        darkIt.remove();
                    }
                }

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

                // for minigame vase breaker
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
        }

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
            maxLineAnimation.setPosition((maxCol + 1) * TILE_WIDTH + GRID_START_X - TILE_WIDTH / 2f - 15, gridCenterY + 40);

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

            bigWaveForeground.setPosition((minWaterCol + 5) * TILE_WIDTH + GRID_START_X, gridCenterY);
        }

        despawnMissingTiles(graveActors, activeGraves);
        despawnMissingTiles(slipperyActors, activeSlippery);
        despawnMissingTiles(waterActors, activeWater);
        despawnMissingTiles(vaseActors, activeVases);
    }

    public void clear() {
        layerGroup.clearChildren();
        graveActors.clear();
        slipperyActors.clear();
        waterActors.clear();
        vaseActors.clear();
    }

    private String resolveGraveClip(int currentHp) {
        if (currentHp > 560) return "undamaged";
        if (currentHp > 420) return "damage1";
        if (currentHp > 280) return "damage2";
        if (currentHp > 140) return "damage3";
        return "damage4";
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

    private void centerOnPoint(PamAnimatedActor actor, float pixelX, float pixelY) {
        actor.setPosition(pixelX - actor.getWidth() / 2f, pixelY - actor.getHeight() / 2f);
    }
}
