package io.java.pvz.views.sound;

public enum MusicType implements SoundType {
;

    private final String path;
    MusicType(String path){
        this.path = path;
    }
    @Override
    public String getPath(){
        return path;
    }
}
