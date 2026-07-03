package models.entities;

public enum SunType {
    NORMAL_SUN(25, "normal"),
    SPECIAL_SUN(100, "special"),
    RADIOACTIVE_SUN(25, "radioactive");

    private final int value;
    private final String label;

    SunType(int value, String label) {
        this.value = value;
        this.label = label;
    }

    public int getValue() { return value; }
    public String getLabel() { return label; }
}
