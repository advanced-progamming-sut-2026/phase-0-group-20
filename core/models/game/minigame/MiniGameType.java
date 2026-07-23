package models.game.minigame;

public enum MiniGameType {
    VASE_BREAKER("VaseBreaker"),
    BOWLING("Bowling"),
    I_ZOMBIE("IZombie"),
    BEGHOULED("Beghouled"),
    ZOMBOTANY("Zombotany");

    private final String name;

    MiniGameType(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public static MiniGameType findByName(String name) {
        for (MiniGameType type : MiniGameType.values()) {
            if (type.getName().equalsIgnoreCase(name)) {
                return type;
            }
        }
        return null;
    }
}
