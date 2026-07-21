package models.enums.commands;

public enum MiniGameCommands implements Commands {

    BREAK_VASE("^\\s*break\\s+vase\\s+-l\\s+\\(\\s*(?<x>\\d+)\\s*,\\s*(?<y>\\d+)\\s*\\)\\s*$"),

    PUT_ZOMBIE("^\\s*put\\s+zombie\\s+-t\\s+(?<zombieName>\\S+)\\s+-l\\s+\\(\\s*(?<x>\\d+)\\s*,\\s*(?<y>\\d+)\\s*\\)\\s*$"),

    BOWL("^\\s*bowl\\s+-i\\s+(?<index>\\d+)\\s+-l\\s+\\(\\s*(?<x>\\d+)\\s*,\\s*(?<y>\\d+)\\s*\\)\\s*$"),

    SHOW_ZOMBIES("^\\s*show\\s+zombies\\s*$"),

    SWAP_PLANTS("^\\s*swap\\s+-l1\\s+\\(\\s*(?<x1>\\d+)\\s*,\\s*(?<y1>\\d+)\\s*\\)\\s+-l2\\s+\\(\\s*(?<x2>\\d+)\\s*,\\s*(?<y2>\\d+)\\s*\\)\\s*$"),
    UPGRADE_PLANTS("^\\s*upgrade\\s+-p\\s+(?<plantName>.+)\\s*$");

    private final String pattern;

    MiniGameCommands(String pattern) {
        this.pattern = pattern;
    }

    public String getPattern() {
        return pattern;
    }
}