package models.enums.commands;

public enum MiniGameCommands implements Commands {

    // break vase -l (2, 4)
    BREAK_VASE("^\\s*break\\s+vase\\s+-l\\s+\\(\\s*(?<x>\\d+)\\s*,\\s*(?<y>\\d+)\\s*\\)\\s*$"),

    //put zombie -t ZombieRa -l (1, 5)
    PUT_ZOMBIE("^\\s*put\\s+zombie\\s+-t\\s+(?<zombieName>\\S+)\\s+-l\\s+\\(\\s*(?<x>\\d+)\\s*,\\s*(?<y>\\d+)\\s*\\)\\s*$"),

    //bowl -i 0 -l (2, 6) **note: i means the index of the plant in belt**
    BOWL("^\\s*bowl\\s+-i\\s+(?<index>\\d+)\\s+-l\\s+\\(\\s*(?<x>\\d+)\\s*,\\s*(?<y>\\d+)\\s*\\)\\s*$"),

    //show zombies
    SHOW_ZOMBIES("^\\s*show\\s+zombies\\s*$"),

    //swap -l1 (1,2) -l2 (3,5)
    SWAP_PLANTS("^\\s*swap\\s+-l1\\s+\\(\\s*(?<x1>\\d+)\\s*,\\s*(?<y1>\\d+)\\s*\\)\\s+-l2\\s+\\(\\s*(?<x2>\\d+)\\s*,\\s*(?<y2>\\d+)\\s*\\)\\s*$"),

    //upgrade -p peashooter
    UPGRADE_PLANTS("^\\s*upgrade\\s+-p\\s+(?<plantName>.+)\\s*$");

    private final String pattern;

    MiniGameCommands(String pattern) {
        this.pattern = pattern;
    }

    public String getPattern() {
        return pattern;
    }
}