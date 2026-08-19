package io.java.pvz.models.game.events;

import io.java.pvz.controllers.AudioManager;
import io.java.pvz.models.entities.plants.Plant;
import io.java.pvz.models.enums.plants.PlantTag;
import io.java.pvz.views.sound.MusicType;
import io.java.pvz.views.sound.SfxType;

public class AudioListener implements GameEventListener {
    private final AudioManager audioManager = AudioManager.getInstance();

    private long lastDamageSoundTime = 0;

    @Override
    public void onEvent(GameEvent event, GameEventPayload payload) {
        switch (event) {
            case ENTERED_MENUS -> audioManager.playMusic(MusicType.MENU_BGM);
            case ENTERED_BIG_WAVE_BEACH ->  audioManager.playMusic(MusicType.BIG_WAVE_BEACH);
            case ENTERED_DARK_AGES ->   audioManager.playMusic(MusicType.DARK_AGES);
            case ENTERED_EGYPT -> audioManager.playMusic(MusicType.ANCIENT_EGYPT);
            case ENTERED_FROZEN_CAVES ->   audioManager.playMusic(MusicType.FROZEN_CAVES);
            case ENTERED_ZEN_GARDEN ->   audioManager.playMusic(MusicType.ZEN_GARDEN);
            case PLANT_EXPLODED -> audioManager.playSfx(SfxType.EXPLOSION);
            case LAWNMOWER_TRIGGERED -> audioManager.playSfx(SfxType.LAWN_MOWER);
            case PLANT_PLACED -> handlePlantPlacement(payload.getPlant());
            case PROJECTILE_HIT -> audioManager.playSfx(SfxType.ZOMBIE_HIT);
//            case PLANT_TAKING_DAMAGE -> handlePlantTakingDamage();
            case PLANT_LOST -> audioManager.playSfx(SfxType.SHOVEL);
        }
    }

    private void handlePlantPlacement(Plant plant) {
        if(plant.getTags().contains(PlantTag.WATER)){
            audioManager.playSfx(SfxType.PLANT_WATER);
        }else{
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
