package io.java.pvz.views.sound;

public enum SfxType {
   ;

    private final String path;

    SfxType(String path) {
        this.path = path;
    }

    public String getPath() {
        return path;
    }
}
