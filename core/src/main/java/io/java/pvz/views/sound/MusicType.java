package io.java.pvz.views.sound;

public enum MusicType implements SoundType {
    MENU_BGM("sounds/bgm/1-01. Title Screen.mp3"),
    ANCIENT_EGYPT("sounds/bgm/Ancient Egypt (First Wave) MP3" +
        " - Plants vs. Zombies 2- It's About Time Original Soundtrack (Android, iOS) (gamerip) (2013) " +
        "- Download Soundtracks for FREE!.mp3"),
    BIG_WAVE_BEACH("sounds/bgm/Big Wave Beach (First Wave) MP3 " +
        "- Plants vs. Zombies 2- It's About Time Original Soundtrack (Android, iOS) (gamerip) (2013) " +
        "- Download Soundtracks for FREE!.mp3"),
    FROZEN_CAVES("sounds/bgm/Frostbite Caves (Final Wave) MP3 " +
        "- Plants vs. Zombies 2- It's About Time Original Soundtrack (Android, iOS) (gamerip) (2013) " +
        "- Download Soundtracks for FREE!.mp3"),
    DARK_AGES("sounds/bgm/Dark Ages (Final Wave) MP3 - Plants vs. Zombies 2" +
        "- It's About Time Original Soundtrack (Android, iOS) (gamerip) (2013) " +
        "- Download Soundtracks for FREE!.mp3")
    ,ZEN_GARDEN("sounds/bgm/zen garden.mp3"),
    ZOMBOSS_1("sounds/bgm/Zomboss (Phase 1) MP3 " +
        "- Plants vs. Zombies 2- It's About Time Original Soundtrack (Android, iOS) (gamerip) (2013) " +
        "- Download Soundtracks for FREE!.mp3"),
    ZOMBOSS_2("sounds/bgm/Zomboss (Phase 2) MP3 - Plants vs. Zombies 2" +
        "- It's About Time Original Soundtrack (Android, iOS) (gamerip) (2013)" +
        " - Download Soundtracks for FREE!.mp3"),
    ZOMBOSS_3("sounds/bgm/Zomboss (Phase 3) MP3 - Plants vs. Zombies 2" +
        "- It's About Time Original Soundtrack (Android, iOS) (gamerip) (2013)" +
        " - Download Soundtracks for FREE!.mp3");

    private final String path;
    MusicType(String path){
        this.path = path;
    }
    @Override
    public String getPath(){
        return path;
    }
}
