package io.java.pvz.models.game.events;

import io.java.pvz.controllers.AudioManager;
import io.java.pvz.models.entities.plants.Plant;
import io.java.pvz.models.entities.plants.PlantTag;
import io.java.pvz.models.entities.projectiles.ProjectileType;
import io.java.pvz.views.sound.MusicType;
import io.java.pvz.views.sound.SfxType;

import java.util.Random;

public class AudioListener implements GameEventListener {
    private final AudioManager audioManager = AudioManager.getInstance();

    private long lastDamageSoundTime = 0;

    @Override
    public void onEvent(GameEvent event, GameEventPayload payload) {
        switch (event) {
            case ENTERED_MENUS -> audioManager.playMusic(MusicType.MENU_BGM);
            case ENTERED_BIG_WAVE_BEACH -> audioManager.playMusic(MusicType.BIG_WAVE_BEACH);
            case ENTERED_DARK_AGES -> audioManager.playMusic(MusicType.DARK_AGES);
            case ENTERED_EGYPT -> audioManager.playMusic(MusicType.ANCIENT_EGYPT);
            case ENTERED_FROZEN_CAVES -> audioManager.playMusic(MusicType.FROZEN_CAVES);
            case ENTERED_ZEN_GARDEN -> audioManager.playMusic(MusicType.ZEN_GARDEN);
            case ZOMBOSS_PHASE_1 -> audioManager.playMusic(MusicType.ZOMBOSS_1);
            case ZOMBOSS_PHASE_2 -> audioManager.playMusic(MusicType.ZOMBOSS_2);
            case ZOMBOSS_PHASE_3 -> audioManager.playMusic(MusicType.ZOMBOSS_3);
            case PLANT_EXPLODED -> audioManager.playSfx(SfxType.EXPLOSION);
            case LAWNMOWER_TRIGGERED -> audioManager.playSfx(SfxType.LAWN_MOWER);
            case PLANT_PLACED -> handlePlantPlacement(payload.getPlant());
            case PROJECTILE_HIT -> playProjectileHit(payload.getProjectileType());
//            case PLANT_TAKING_DAMAGE -> handlePlantTakingDamage();
            case PLANT_LOST -> audioManager.playSfx(SfxType.SHOVEL);
            case PROJECTILE_FIRED -> playProjectileSound(payload.getProjectileType());
            case ZOMBOSS_TALKS -> audioManager.playSfx(SfxType.ZOMBOSS_TALKS);
            case DAVE_TALKS -> playDave();
            case GAME_OVER -> audioManager.playSfx(SfxType.LOSS_AUDIO);
            case LEVEL_COMPLETED -> {
                audioManager.playSfx(SfxType.WIN_AUDIO);
                System.out.println("lbjf");
            }
            case WAVE_STARTED_PLAYTIME -> {
                System.out.println("good");
                audioManager.playSfx(SfxType.START_WAVE_SOUND);
            }
        }
    }

    private void playProjectileHit(ProjectileType projectileType) {
        if (projectileType == ProjectileType.WALLNUT_BOWL
            || projectileType == ProjectileType.EXPLODE_NUT_BOWL
            || projectileType == ProjectileType.GIANT_NUT_BOWL) {
            audioManager.playSfx(SfxType.BOWLING_HIT);
        } else
            audioManager.playSfx(SfxType.ZOMBIE_HIT);
    }

    private void playDave() {
        int rnd = new Random().nextInt(2) + 1;
        if (rnd == 1) {
            audioManager.playSfx(SfxType.DAVE_TALKS);
        } else {
            audioManager.playSfx(SfxType.DAVE_TALKS_2);
        }
    }

    private void playProjectileSound(ProjectileType projectileType) {
        if (projectileType == ProjectileType.WALLNUT_BOWL
            || projectileType == ProjectileType.EXPLODE_NUT_BOWL
            || projectileType == ProjectileType.GIANT_NUT_BOWL) {
            audioManager.playSfx(SfxType.PLANT_BOWLING);
        } else {
            audioManager.playSfx(SfxType.FIRED_PROJECTILE);
        }
    }

    private void handlePlantPlacement(Plant plant) {
        if (plant.getTags().contains(PlantTag.WATER)) {
            audioManager.playSfx(SfxType.PLANT_WATER);
        } else {
            audioManager.playSfx(SfxType.PLANT);
        }
    }

    private void handlePlantTakingDamage() {
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastDamageSoundTime >= 1000) {
            lastDamageSoundTime = currentTime;

            if (Math.random() < 0.5) {
                audioManager.playSfx(SfxType.ZOMBIE_EAT_1);
            } else {
                audioManager.playSfx(SfxType.ZOMBIE_EAT_2);
            }
        }
    }
}
