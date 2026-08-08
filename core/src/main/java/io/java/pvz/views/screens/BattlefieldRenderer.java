package io.java.pvz.views.screens;

import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.utils.Align;
import io.java.pvz.models.entities.plants.Plant;
import io.java.pvz.models.entities.zombies.Zombie;
import io.java.pvz.models.entities.zombies.ZombieState;
import io.java.pvz.models.game.Arena;
import io.java.pvz.utils.PamAnimatedActor;
import io.java.pvz.utils.UiFactory;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static io.java.pvz.models.enums.PhysicalConstants.TILE_HEIGHT;
import static io.java.pvz.models.enums.PhysicalConstants.TILE_WIDTH;

public class BattlefieldRenderer {

    private static final String CLIP_IDLE = "idle";
    private static final String CLIP_WALK = "walk";
    private static final String CLIP_EAT = "eat";
    private static final String CLIP_STUNNED = "stunned";
    private static final String CLIP_DEATH = "death";

    private static final float DESPAWN_LINGER_SECONDS = 0.5f;
    private static final float DESPAWN_FADE_SECONDS = 0.25f;

    private final Group group = new Group();

    private final Map<Plant, PamAnimatedActor> plantActors = new HashMap<>();
    private final Map<Zombie, PamAnimatedActor> zombieActors = new HashMap<>();

    public Group getGroup() {
        return group;
    }

    public void sync(Arena arena) {
        if (arena == null) return;

        syncPlants(arena.getActivePlants());
        syncZombies(arena.getActiveZombies());
    }

    public void clear() {
        group.clearChildren();
        plantActors.clear();
        zombieActors.clear();
    }


    private void syncPlants(List<Plant> livePlants) {
        for (Plant plant : livePlants) {
            PamAnimatedActor actor = plantActors.get(plant);
            if (actor == null) {
                actor = spawnPlant(plant);
                plantActors.put(plant, actor);
            }
            updatePlantActor(plant, actor);
        }

        Set<Plant> stillAlive = new HashSet<>(livePlants);
        Iterator<Map.Entry<Plant, PamAnimatedActor>> it = plantActors.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<Plant, PamAnimatedActor> entry = it.next();
            if (!stillAlive.contains(entry.getKey())) {
                despawn(entry.getValue());
                it.remove();
            }
        }
    }

    private PamAnimatedActor spawnPlant(Plant plant) {
        String atlasName = UiFactory.getAtlasName(plant);
        PamAnimatedActor actor = PamAnimatedActor.createPlantAnimated(atlasName, resolvePlantClip(plant));
        actor.setSize(TILE_WIDTH, TILE_HEIGHT);
        actor.setOrigin(Align.center);
        group.addActor(actor);
        return actor;
    }

    private void updatePlantActor(Plant plant, PamAnimatedActor actor) {
        actor.setClip(resolvePlantClip(plant));
        centerOnPoint(actor, plant.getPosition().getX(), plant.getPosition().getY());
    }

    private String resolvePlantClip(Plant plant) {
        return CLIP_IDLE;
    }

    private void syncZombies(List<Zombie> liveZombies) {
        for (Zombie zombie : liveZombies) {
            PamAnimatedActor actor = zombieActors.get(zombie);
            if (actor == null) {
                actor = spawnZombie(zombie);
                zombieActors.put(zombie, actor);
            }
            updateZombieActor(zombie, actor);
        }

        Set<Zombie> stillAlive = new HashSet<>(liveZombies);
        Iterator<Map.Entry<Zombie, PamAnimatedActor>> it = zombieActors.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<Zombie, PamAnimatedActor> entry = it.next();
            if (!stillAlive.contains(entry.getKey())) {
                entry.getValue().setClip(CLIP_DEATH);
                despawn(entry.getValue());
                it.remove();
            }
        }
    }

    private PamAnimatedActor spawnZombie(Zombie zombie) {
        String address = UiFactory.getZombieAddress(zombie);
        PamAnimatedActor actor = PamAnimatedActor.createZombieAnimated(address, resolveZombieClip(zombie));
        actor.setSize(TILE_WIDTH, TILE_HEIGHT);
        actor.setOrigin(Align.center);
        actor.setScale(1f, 1f);
        group.addActor(actor);
        return actor;
    }

    private void updateZombieActor(Zombie zombie, PamAnimatedActor actor) {
        actor.setClip(resolveZombieClip(zombie));
        centerOnPoint(actor, zombie.getPosition().getX(), zombie.getPosition().getY());
    }

    private String resolveZombieClip(Zombie zombie) {
        if (zombie.isDead()) return CLIP_DEATH;
        if (zombie.isAttacking()) return CLIP_EAT;
        if (zombie.getState() == ZombieState.STUNNED) return CLIP_STUNNED;
        return CLIP_WALK;
    }

    private void centerOnPoint(PamAnimatedActor actor, float pixelX, float pixelY) {
        actor.setPosition(pixelX - actor.getWidth() / 2f, pixelY - actor.getHeight() / 2f);
    }

    private void despawn(PamAnimatedActor actor) {
        actor.addAction(Actions.sequence(
            Actions.delay(DESPAWN_LINGER_SECONDS),
            Actions.fadeOut(DESPAWN_FADE_SECONDS),
            Actions.removeActor()
        ));
    }
}
