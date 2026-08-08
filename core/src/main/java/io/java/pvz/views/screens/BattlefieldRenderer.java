package io.java.pvz.views.screens;

import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.utils.Align;
import io.java.pvz.controllers.GameController.GameFlowController;
import io.java.pvz.loader.AssetLoader;
import io.java.pvz.models.Result;
import io.java.pvz.models.entities.Sun;
import io.java.pvz.models.entities.plants.Plant;
import io.java.pvz.models.entities.projectiles.Projectile;
import io.java.pvz.models.entities.zombies.Zombie;
import io.java.pvz.models.entities.zombies.ZombieState;
import io.java.pvz.models.game.Arena;
import io.java.pvz.utils.PamAnimatedActor;
import io.java.pvz.utils.UiFactory;

import java.util.*;

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
    private final Map<Projectile, PamAnimatedActor> projectileActors = new HashMap<>();
    private final Map<Sun, PamAnimatedActor> sunActors = new HashMap<>();
    private final GameFlowController  gameFlowController = new GameFlowController();

    public Group getGroup() {
        return group;
    }

    public void sync(Arena arena) {
        if (arena == null) return;

        syncPlants(arena.getActivePlants());
        syncZombies(arena.getActiveZombies());

        syncProjectiles(arena.getActiveProjectiles());
        syncSuns(arena.getActiveSuns());
    }

    public void clear() {
        group.clearChildren();
        plantActors.clear();
        zombieActors.clear();
        projectileActors.clear();
        sunActors.clear();
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
        centerOnPoint(actor, plant.getPosition().getX(), plant.getPosition().getY() + actor.getHeight() / 2);
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
        centerOnPoint(actor, zombie.getPosition().getX(), zombie.getPosition().getY() + actor.getHeight() / 2f + 30);
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

    private void syncSuns(List<Sun> liveSuns) {
        for (Sun sun : liveSuns) {
            if (sun.isCollected()) continue;

            PamAnimatedActor actor = sunActors.get(sun);
            if (actor == null) {
                actor = spawnSun(sun);
                sunActors.put(sun, actor);
            }
            updateSunActor(sun, actor);
        }

        Set<Sun> stillAlive = new HashSet<>(liveSuns);
        Iterator<Map.Entry<Sun, PamAnimatedActor>> it = sunActors.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<Sun, PamAnimatedActor> entry = it.next();
            Sun sun = entry.getKey();
            if (!stillAlive.contains(sun) || sun.isCollected()) {
                entry.getValue().addAction(Actions.sequence(
                    Actions.fadeOut(0.2f),
                    Actions.removeActor()
                ));
                it.remove();
            }
        }
    }

    private PamAnimatedActor spawnSun(Sun sun) {
        String pamPath1 = "768/FULL/EFFECTS/SUN/SUN.PAM"; //add a method to find all sun type animation
        String pamPath2 = "768/INITIAL/EFFECTS/SUN/SUN.PAM";

        PamAnimatedActor actor = new PamAnimatedActor(AssetLoader.getInstance().getPlayer(),
            "animation", pamPath1, pamPath2);

        actor.setSize(80, 80);
        actor.setOrigin(Align.center);

        float scale = 1.0f;
        if (sun.getType() != null) {
            switch (sun.getType()) {
                case TINY_SUN -> scale = 0.5f;
                case LARGE_SUN -> scale = 1.3f;
                case SPECIAL_SUN, HUGE_SUN -> scale = 1.6f;
                default -> scale = 1.0f;
            }
        }
        actor.setScale(scale, scale);

        float targetX = sun.getPosition().getX() - actor.getWidth() / 2f;
        float targetY = sun.getPosition().getY() - actor.getHeight() / 2f + 15f;

        boolean isFromSky = (sun.getType() != null);

        if (isFromSky) {
            actor.setPosition(targetX, 1180f);
            actor.addAction(Actions.moveTo(targetX, targetY, 2.5f, Interpolation.linear));
        } else {
            actor.setPosition(targetX, targetY + 40f);
            actor.addAction(Actions.moveTo(targetX, targetY, 1.0f, Interpolation.bounceOut));
        }

        float finalScale = scale;
        actor.addListener(new InputListener() {
            @Override
            public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
                if (!sun.isCollected() && pointer == -1) {

                    Result result = gameFlowController.collectSun(sun.getCol(), sun.getRow());

                    if (result.isSuccessful()) {
                        actor.addAction(Actions.sequence(
                            Actions.parallel(
                                Actions.scaleTo(finalScale * 1.5f, finalScale * 1.5f, 0.2f),
                                Actions.fadeOut(0.2f)
                            ),
                            Actions.removeActor()
                        ));
                    }
                }
            }
        });

        group.addActor(actor);
        return actor;
    }

    private void updateSunActor(Sun sun, PamAnimatedActor actor) {
        if (!sun.isFalling()) {
            float targetX = sun.getPosition().getX() - actor.getWidth() / 2f;
            float targetY = sun.getPosition().getY() - actor.getHeight() / 2f + 15f;
            actor.setPosition(targetX, targetY);
        }    }

    private void syncProjectiles(List<Projectile> liveProjectiles) {
        for (Projectile proj : liveProjectiles) {
            PamAnimatedActor actor = projectileActors.get(proj);
            if (actor == null) {
                actor = spawnProjectile(proj);
                projectileActors.put(proj, actor);
            }
            updateProjectileActor(proj, actor);
        }

        Set<Projectile> stillAlive = new HashSet<>(liveProjectiles);
        Iterator<Map.Entry<Projectile, PamAnimatedActor>> it = projectileActors.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<Projectile, PamAnimatedActor> entry = it.next();
            Projectile proj = entry.getKey();
            if (!stillAlive.contains(proj) || proj.isDestroyed()) {
                entry.getValue().remove();
                it.remove();
            }
        }
    }

    private PamAnimatedActor spawnProjectile(Projectile proj) {
        String[] pamPaths = getProjectilePamPaths(proj);

        PamAnimatedActor actor = new PamAnimatedActor(AssetLoader.getInstance().getPlayer(),
            "animation", pamPaths[0], pamPaths[1]);


        actor.setSize(30, 30);
        actor.setOrigin(Align.center);

        actor.setScale(0.25f, 0.25f);
        group.addActor(actor);
        return actor;
    }

    private void updateProjectileActor(Projectile proj, PamAnimatedActor actor) {
        float projX = proj.getPosition().getX();
        float projY = proj.getPosition().getY();

        actor.setPosition(
            projX - actor.getWidth() / 2f,
            projY - actor.getHeight() / 2f + 40f
        );
    }

    private String[] getProjectilePamPaths(Projectile proj) { // just for now you can use switch case Elyas
        String baseName;
        baseName = "PEAPOD_PLANTFOOD_GIANTPEA";

        return new String[]{
            "768/FULL/EFFECTS/" + baseName + "/" + baseName + ".PAM",
            "768/INITIAL/EFFECTS/" + baseName + "/" + baseName + ".PAM"
        };
    }
}
