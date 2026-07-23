package models.game.minigame;

public enum MiniGameType {
    VASE_BREAKER("vase breaker"),
    BOWLING("bowling"),
    I_ZOMBIE("I zombie"),
    BEGHOULED("Beghouled"),
    ZOMBOTANY("Zombotany");

    private final String name;

    MiniGameType(String name) {
        this.name = name;
    }

    public static MiniGameType findByName(String name) {
        for (MiniGameType type : MiniGameType.values()) {
            if (type.getName().equalsIgnoreCase(name)) {
                return type;
            }
        }
        return null;
    }

    public String getName() {
        return name;
    }
}
