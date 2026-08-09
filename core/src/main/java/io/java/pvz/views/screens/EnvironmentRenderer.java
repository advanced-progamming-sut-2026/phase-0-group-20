package io.java.pvz.views.screens;

import com.badlogic.gdx.scenes.scene2d.Group;
import io.java.pvz.models.fields.obstacle.GraveHolder;
import io.java.pvz.models.fields.tiles.*;
import io.java.pvz.models.game.Arena;
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

    public EnvironmentRenderer(Group layerGroup) {
        this.layerGroup = layerGroup;
    }

    public void sync(Arena arena) {
        if (arena == null) return;

        Set<Tile> activeGraves = new HashSet<>();
        Set<Tile> activeSlippery = new HashSet<>();
        Set<Tile> activeWater = new HashSet<>();
        Set<Tile> activeVases = new HashSet<>();

        for (Tile[] row : arena.getTiles()) {
            for (Tile tile : row) {
                float pixelX = tile.getCol() * TILE_WIDTH + GRID_START_X;
                float pixelY = tile.getRow() * TILE_HEIGHT + GRID_START_Y;

                if (tile instanceof SlipperyTile st) {
                    activeSlippery.add(tile);
                    PamAnimatedActor actor = slipperyActors.computeIfAbsent(tile, t -> {
                        String dir = st.getDirection() == SlipperyTile.SlideDirection.DOWN ?
                            Ids.ArenaEffects.TILESLIDER_DOWN :
                            Ids.ArenaEffects.TILESLIDER_UP;
                        PamAnimatedActor animatedActor = PamAnimatedActor.createEffectAnimated(dir, "active_idle");
                        animatedActor.setSize(TILE_WIDTH, TILE_HEIGHT);
                        layerGroup.addActor(animatedActor);
                        animatedActor.toBack();
                        return animatedActor;
                    });
                    centerOnPoint(actor, pixelX + TILE_WIDTH / 2f, pixelY + TILE_HEIGHT / 2f);
                }

                if (tile instanceof GraveHolder gh && gh.getGraveStone() != null) {
                    activeGraves.add(tile);
                    PamAnimatedActor actor = graveActors.computeIfAbsent(tile, t -> {
                        PamAnimatedActor animatedActor = PamAnimatedActor.createEffectAnimated(
                            Ids.ArenaEffects.GRAVE, "undamaged");
                        layerGroup.addActor(animatedActor);
                        return animatedActor;
                    });
                    actor.setClip(resolveGraveClip(gh.getGraveStone().getHp()));
                    centerOnPoint(actor, pixelX + TILE_WIDTH / 2f, pixelY + TILE_HEIGHT/2);
                }

                // for minigame vase breaker
                if (tile instanceof VaseTile vt && !vt.isBroken()) {
                    activeVases.add(tile);
                    if (tile instanceof RandomVaseTile) {
                        PamAnimatedActor actor = vaseActors.computeIfAbsent(tile, t -> {
                            PamAnimatedActor animatedActor = PamAnimatedActor.createEffectAnimated(
                                Ids.ArenaEffects.VASE_RANDOM, "idle");
                            layerGroup.addActor(animatedActor);
                            return animatedActor;
                        });
                        centerOnPoint(actor, pixelX + TILE_WIDTH / 2f, pixelY + TILE_HEIGHT / 2f);
                    } else if (tile instanceof PlantVaseTile) {
                        PamAnimatedActor actor = vaseActors.computeIfAbsent(tile, t -> {
                            PamAnimatedActor animatedActor = PamAnimatedActor.createEffectAnimated(
                                Ids.ArenaEffects.VASE_PLANTS, "idle");
                            layerGroup.addActor(animatedActor);
                            return animatedActor;
                        });
                        centerOnPoint(actor, pixelX + TILE_WIDTH / 2f, pixelY + TILE_HEIGHT / 2f);
                    } else {
                        PamAnimatedActor actor = vaseActors.computeIfAbsent(tile, t -> {
                            PamAnimatedActor animatedActor = PamAnimatedActor.createEffectAnimated(
                                Ids.ArenaEffects.VASE_ZOMBIE, "idle");
                            layerGroup.addActor(animatedActor);
                            return animatedActor;
                        });
                        centerOnPoint(actor, pixelX + TILE_WIDTH / 2f, pixelY + TILE_HEIGHT / 2f);
                    }
                }
            }
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
