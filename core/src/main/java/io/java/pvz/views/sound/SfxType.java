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
    FIRED_PROJECTILE("sounds/sfx/1-36. SFX floop.mp3")
   ;

    private final String path;

    SfxType(String path) {
        this.path = path;
    }

    public String getPath() {
        return path;
    }
}
