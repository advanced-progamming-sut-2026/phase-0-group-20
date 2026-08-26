package io.java.pvz.views.sound;

public enum SfxType {
    EXPLOSION("sounds/sfx/explosion.mp3"),
    LAWN_MOWER("sounds/sfx/lawn mower.mp3"),
    PLANT("sounds/sfx/plant.mp3"),
    PLANT_BOWLING("sounds/sfx/plant bowling.mp3"),
    PLANT_WATER("sounds/sfx/plant water.mp3"),
    SHOVEL("sounds/sfx/shovel.mp3"),
    ZOMBIE_EAT_1("sounds/sfx/zombie eat 1.mp3"),
    ZOMBIE_EAT_2("sounds/sfx/zombie eat 2.mp3"),
    ZOMBIE_HIT("sounds/sfx/1-53. SFX kernelpult2.mp3"),
    BOWLING_HIT("sounds/sfx/1-14. SFX bowlingimpact.mp3"),
    FIRED_PROJECTILE("sounds/sfx/1-36. SFX floop.mp3"),
    DAVE_TALKS("sounds/sfx/2-10. Voices crazydavelong2.mp3"),
    DAVE_TALKS_2("sounds/sfx/2-05. Voices crazydavecrazy.mp3"),
    ZOMBOSS_TALKS("sounds/sfx/2-41. Voices sukhbir2.mp3"),
    LOSS_AUDIO("sounds/sfx/loss audio.mp3"),
    WIN_AUDIO("sounds/sfx/win audio.mp3"),
    START_WAVE_SOUND("sounds/sfx/zombies are comming.mp3");

   ;

    private final String path;

    SfxType(String path) {
        this.path = path;
    }

    public String getPath() {
        return path;
    }
}
