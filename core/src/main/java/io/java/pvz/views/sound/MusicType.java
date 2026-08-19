package io.java.pvz.views.sound;

public enum MusicType implements SoundType {
    MENU_BGM("sounds/bgm/1-01. Title Screen.mp3"),
    ANCIENT_EGYPT("sounds/bgm/Ancient_Egypt_First_Wave_MP3_Plants_vs_Zombies_2_It's_About_Time.mp3"),
    BIG_WAVE_BEACH("sounds/bgm/Big_Wave_Beach_First_Wave_MP3_Plants_vs_Zombies_2_It's_About_Time.mp3"),
    FROZEN_CAVES("sounds/bgm/Frostbite_Caves_Final_Wave_MP3_Plants_vs_Zombies_2_It's_About_Time.mp3"),
    DARK_AGES("sounds/bgm/Dark_Ages_Final_Wave_MP3_Plants_vs_Zombies_2_It's_About_Time_Original.mp3")
    ,ZEN_GARDEN("sounds/bgm/zen garden.mp3"),
    ZOMBOSS_1("sounds/bgm/Zomboss_Phase_1_MP3_Plants_vs_Zombies_2_It's_About_Time_Original.mp3"),
    ZOMBOSS_2("sounds/bgm/Zomboss_Phase_2_MP3_Plants_vs_Zombies_2_It's_About_Time_Original.mp3"),
    ZOMBOSS_3("sounds/bgm/Zomboss_Phase_3_MP3_Plants_vs_Zombies_2_It's_About_Time_Original.mp3");

    private final String path;
    MusicType(String path){
        this.path = path;
    }
    @Override
    public String getPath(){
        return path;
    }
}
