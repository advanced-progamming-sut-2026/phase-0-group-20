package io.java.pvz.views.sound;

public enum MusicType implements SoundType {
    MENU_BGM("sounds/bgm/1-01. Title Screen.mp3");

    private final String path;
    MusicType(String path){
        this.path = path;
    }
    @Override
    public String getPath(){
        return path;
    }
}
