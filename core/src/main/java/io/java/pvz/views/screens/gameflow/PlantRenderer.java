package io.java.pvz.views.screens.gameflow;

import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.utils.Align;
import io.java.pvz.loader.AssetLoader;
import io.java.pvz.models.entities.plants.Plant;
import io.java.pvz.models.entities.plants.strategy.DigestionStrategy;
import io.java.pvz.models.entities.plants.strategy.tag_strategy.TrapStrategy;
import io.java.pvz.utils.AnimationCatalog;
import io.java.pvz.utils.PamAnimatedActor;
import io.java.pvz.utils.UiFactory;

import java.util.*;

import static io.java.pvz.models.enums.PhysicalConstants.TILE_HEIGHT;
import static io.java.pvz.models.enums.PhysicalConstants.TILE_WIDTH;

public class PlantRenderer {

    private static final String CLIP_IDLE = "idle";
    private static final float DESPAWN_LINGER_SECONDS = 0.5f;
    private static final float DESPAWN_FADE_SECONDS = 0.25f;

    private final Group plantLayer;

    private final Map<Plant, PamAnimatedActor> plantActors = new HashMap<>();
    private final Map<Plant, PamAnimatedActor> plantChillOverlays = new HashMap<>();
    private final Map<Plant, PamAnimatedActor> plantFreezeOverlays = new HashMap<>();
    private final Map<Plant, PamAnimatedActor> plantOctopusOverlays = new HashMap<>();
    private final Map<Plant, PamAnimatedActor> plantSheepOverlays = new HashMap<>();

    public PlantRenderer(Group plantLayer) {
        this.plantLayer = plantLayer;
    }

    public void syncPlants(List<Plant> livePlants) {
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
                removePlantIceOverlay(entry.getKey());

                PamAnimatedActor octopusActor = plantOctopusOverlays.remove(entry.getKey());
                if (octopusActor != null) octopusActor.remove();

                PamAnimatedActor sheepActor = plantSheepOverlays.remove(entry.getKey());
                if (sheepActor != null) sheepActor.remove();

                it.remove();
            }
        }

        plantLayer.getChildren().sort((a, b) -> {
            float ay = a.getY();
            float by = b.getY();
            return Float.compare(by, ay);
        });
    }

    private PamAnimatedActor spawnPlant(Plant plant) {
        AnimationCatalog.EntityAnimation anim = AnimationCatalog.getPlantAnimation(plant);
        String clip = resolvePlantClip(plant);

        PamAnimatedActor actor;
        if (anim != null) {
            actor = new PamAnimatedActor(AssetLoader.getInstance().getPlayer(), clip, anim.path) {
                @Override
                public void act(float delta) {
                    super.act(plant.isFrozen() ? 0f : delta);
                }
            };
        } else {
            actor = PamAnimatedActor.createPlantAnimated(UiFactory.getAtlasName(plant), clip);
        }

        actor.setSize(TILE_WIDTH, TILE_HEIGHT);
        actor.setOrigin(Align.center);

        actor.setUserObject(plant);

        plantLayer.addActor(actor);
        return actor;
    }

    private void updatePlantActor(Plant plant, PamAnimatedActor actor) {
        actor.setClip(resolvePlantClip(plant));
        centerOnPoint(actor, plant.getPosition().getX(), plant.getPosition().getY() + actor.getHeight() / 2);
    }

    private String resolvePlantClip(Plant plant) {
        AnimationCatalog.EntityAnimation anim = AnimationCatalog.getPlantAnimation(plant);
        if (anim == null) return CLIP_IDLE;

        String action = plant.getCurrentAction();
        if (action != null && anim.hasClip(action)) {
            return action;
        }

        if (plant.isBoosted() && action == null) {
            if (anim.hasClip("plantfood_loop")) return "plantfood_loop";
            if (anim.hasClip("plantfood_stage" + plant.getSize())) return "plantfood_stage" + plant.getSize();
            if (anim.hasClip("plantfood")) return "plantfood";
        }

        DigestionStrategy digestion = plant.getStrategy(DigestionStrategy.class);
        if (digestion != null && digestion.isDigesting()) {
            if (anim.hasClip("special_idle")) return "special_idle";
            if (anim.hasClip("special")) return "special";
        }

        TrapStrategy trap = plant.getStrategy(TrapStrategy.class);
        if (trap != null && !trap.isArmed()) {
            if (anim.hasClip("plant_idle")) return "plant_idle";
            if (anim.hasClip("plant")) return "plant";
        }

        if (plant.isAsleep() && anim.hasClip("sleep")) return "sleep";

        if (plant.getStackCount() > 1 && plant.getName().equalsIgnoreCase("Pea Pod")) {
            String peaIdle = "idle" + plant.getStackCount();
            if (anim.hasClip(peaIdle)) return peaIdle;
        }

        int size = plant.getSize();
        if (size > 1) {
            String stageIdle = "idle_stage" + size;
            String altStageIdle = "idle" + size;
            if (anim.hasClip(stageIdle)) return stageIdle;
            if (anim.hasClip(altStageIdle)) return altStageIdle;
            if (anim.hasClip("stage" + size + "_idle")) return "stage" + size + "_idle";
        }

        float hpRatio = (float) plant.getCurrentHp() / plant.getMaxHp();
        if (hpRatio <= 0.33f) {
            if (anim.hasClip("damage3")) return "damage3";
            if (anim.hasClip("idle_damage3")) return "idle_damage3";
            if (anim.hasClip("damage2")) return "damage2";
        } else if (hpRatio <= 0.66f) {
            if (anim.hasClip("damage2")) return "damage2";
            if (anim.hasClip("idle_damage2")) return "idle_damage2";
            if (anim.hasClip("damage")) return "damage";
            if (anim.hasClip("idle_damage")) return "idle_damage";
        }

        if (anim.hasClip("idle")) return "idle";
        if (anim.hasClip("loop")) return "loop";
        if (anim.hasClip("idle_stage1")) return "idle_stage1";
        if (anim.hasClip("idle1_1")) return "idle1_1";
        if (anim.hasClip("stage1_idle")) return "stage1_idle";

        Iterator<String> anyClip = anim.getClipNames().iterator();
        return anyClip.hasNext() ? anyClip.next() : CLIP_IDLE;
    }

    public void spawnSheepOnPlant(Plant plant) {
        if (plant == null) return;

        PamAnimatedActor plantActor = plantActors.get(plant);
        if (plantActor != null) {
            plantActor.setVisible(false);
        }

        String pamPath = "768/FULL/EFFECTS/DARK_WIZARD_SHEEPENING/DARK_WIZARD_SHEEPENING.PAM";
        PamAnimatedActor sheepActor = plantSheepOverlays.get(plant);

        if (sheepActor == null) {
            sheepActor = PamAnimatedActor.createEffectAnimated(pamPath, "animation");
            sheepActor.setSize(TILE_WIDTH, TILE_HEIGHT);
            sheepActor.setOrigin(Align.center);
            plantLayer.addActor(sheepActor);
            plantSheepOverlays.put(plant, sheepActor);
        }

        if (plantActor != null) {
            sheepActor.setPosition(plantActor.getX(), plantActor.getY() + 15f);
        }

        PamAnimatedActor finalSheep = sheepActor;
        sheepActor.addAction(Actions.sequence(
            Actions.delay(1.7f),
            Actions.run(() -> finalSheep.setClip("idle"))
        ));
    }

    public void removeSheepFromPlant(Plant plant) {
        if (plant == null) return;

        PamAnimatedActor sheepActor = plantSheepOverlays.remove(plant);
        if (sheepActor != null) {
            sheepActor.clearActions();
            sheepActor.setClip("animation2");

            sheepActor.addAction(Actions.sequence(
                Actions.delay(1.1f),
                Actions.removeActor()
            ));
        }

        PamAnimatedActor plantActor = plantActors.get(plant);
        if (plantActor != null) {
            plantActor.addAction(Actions.sequence(
                Actions.delay(0.5f),
                Actions.visible(true)
            ));
        }
    }

    public void updatePlantIceOverlay(Plant plant, int stacks) {
        if (plant == null) return;

        PamAnimatedActor plantActor = plantActors.get(plant);
        float targetX = plantActor != null ? plantActor.getX() : 0;
        float targetY = plantActor != null ? plantActor.getY() : 0;

        if (stacks == 1 || stacks == 2) {
            String clipName = stacks == 1 ? "chill_stage1" : "chill_stage2";
            PamAnimatedActor chillActor = plantChillOverlays.get(plant);

            if (chillActor == null) {
                chillActor = PamAnimatedActor.createEffectAnimated(
                    "768/FULL/EFFECTS/FROSTBITE_CHILL_PLANT/FROSTBITE_CHILL_PLANT.PAM", clipName);
                chillActor.setSize(TILE_WIDTH, TILE_HEIGHT);
                chillActor.setOrigin(Align.center);
                plantLayer.addActor(chillActor);
                plantChillOverlays.put(plant, chillActor);
            } else {
                chillActor.setClip(clipName);
            }
            chillActor.setPosition(targetX, targetY);

        } else if (stacks >= 3) {
            PamAnimatedActor chillActor = plantChillOverlays.remove(plant);
            if (chillActor != null) chillActor.remove();

            PamAnimatedActor freezeActor = plantFreezeOverlays.get(plant);
            if (freezeActor == null) {
                freezeActor = PamAnimatedActor.createEffectAnimated(
                    "768/FULL/EFFECTS/FROSTBITE_ICE_BLOCK_PLANT/FROSTBITE_ICE_BLOCK_PLANT.PAM", "freeze_idle");
                freezeActor.setSize(TILE_WIDTH, TILE_HEIGHT);
                freezeActor.setOrigin(Align.center);
                plantLayer.addActor(freezeActor);
                plantFreezeOverlays.put(plant, freezeActor);
            }

            freezeActor.getColor().a = 0.4f;
            freezeActor.setPosition(targetX, targetY);
        }
    }

    public void spawnOctopusOnPlant(Plant plant) {
        if (plant == null) return;

        PamAnimatedActor plantActor = plantActors.get(plant);
        float targetX = plantActor != null ? plantActor.getX() : 0;
        float targetY = plantActor != null ? plantActor.getY() : 0;

        String pamPath = "768/FULL/EFFECTS/ZOMBIE_OCTOPUS_PROJECTILE/ZOMBIE_OCTOPUS_PROJECTILE.PAM";

        PamAnimatedActor octopusActor = plantOctopusOverlays.get(plant);
        if (octopusActor == null) {
            octopusActor = PamAnimatedActor.createEffectAnimated(pamPath, "animation");
            octopusActor.setSize(TILE_WIDTH, TILE_HEIGHT);
            octopusActor.setOrigin(Align.center);
            plantLayer.addActor(octopusActor);
            plantOctopusOverlays.put(plant, octopusActor);
        }

        float startX = targetX + 400f;
        float startY = targetY + 200f;

        octopusActor.setPosition(startX, startY);

        float flyTime = 0.8f;

        PamAnimatedActor finalOctopusActor = octopusActor;
        octopusActor.addAction(Actions.sequence(
            Actions.parallel(
                Actions.moveTo(targetX, targetY, flyTime, Interpolation.linear),
                Actions.moveBy(0, -200f, flyTime, Interpolation.pow2In)
            ),
            Actions.run(() -> finalOctopusActor.setClip("animation2")),
            Actions.delay(0.9f),

            Actions.run(() -> finalOctopusActor.setClip("animation3"))
        ));
    }

    public void killOctopusOnPlant(Plant plant) {
        if (plant == null) return;
        PamAnimatedActor octopusActor = plantOctopusOverlays.remove(plant);

        if (octopusActor != null) {
            octopusActor.clearActions();
            octopusActor.setClip("die");

            octopusActor.addAction(Actions.sequence(
                Actions.delay(2.0f),
                Actions.removeActor()
            ));
        }
    }

    public void updateIceCracks(Plant plant, int remainingHp) {
        PamAnimatedActor freezeActor = plantFreezeOverlays.get(plant);
        if (freezeActor == null) return;

        Map<String, Boolean> visMap = new HashMap<>();

        if (remainingHp <= 200) {
            visMap.put("damage2", true);
            visMap.put("damage1", false);
            visMap.put("undamaged", false);
        } else if (remainingHp <= 400) {
            visMap.put("damage1", true);
            visMap.put("damage2", false);
            visMap.put("undamaged", false);
        } else {
            visMap.put("undamaged", true);
            visMap.put("damage1", false);
            visMap.put("damage2", false);
        }

        freezeActor.setVisibilityMap(visMap);
    }

    public void removePlantIceOverlay(Plant plant) {
        if (plant == null) return;
        PamAnimatedActor chill = plantChillOverlays.remove(plant);
        if (chill != null) chill.remove();

        PamAnimatedActor freeze = plantFreezeOverlays.remove(plant);
        if (freeze != null) freeze.remove();
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

    public void clear() {
        plantActors.clear();
        plantChillOverlays.clear();
        plantFreezeOverlays.clear();
        plantOctopusOverlays.clear();
        plantSheepOverlays.clear();
        plantLayer.clearChildren();
    }
}
