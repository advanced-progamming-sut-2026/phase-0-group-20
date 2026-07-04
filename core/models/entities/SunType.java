package models.entities;

public enum SunType {
    TINY_SUN(25, "tiny"),
    NORMAL_SUN(50, "normal"),
    LARGE_SUN(75, "large"),
    SPECIAL_SUN(100, "special"),
    HUGE_SUN(375, "huge"),
    RADIOACTIVE_SUN(25, "radioactive");

    private final int value;
    private final String label;

    SunType(int value, String label) {
        this.value = value;
        this.label = label;
    }

    public int getValue() {
        return value;
    }

    public String getLabel() {
        return label;
    }
}
