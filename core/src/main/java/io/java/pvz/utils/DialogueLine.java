package io.java.pvz.utils;

public class DialogueLine {
    private final String speakerName;
    private final String text;
    private final String pamPath;
    private final String clipName;
    private final boolean isLeft;

    public DialogueLine(String speakerName, String text, String pamPath, String clipName, boolean isLeft) {
        this.speakerName = speakerName;
        this.text = text;
        this.pamPath = pamPath;
        this.clipName = clipName;
        this.isLeft = isLeft;
    }

    public String getSpeakerName() { return speakerName; }
    public String getText() { return text; }
    public String getPamPath() { return pamPath; }
    public String getClipName() { return clipName; }
    public boolean isLeft() { return isLeft; }
}
