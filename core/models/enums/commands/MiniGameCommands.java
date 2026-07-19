package models.enums.commands;

public enum MiniGameCommands implements Commands {

    BREAK_VASE("^\\s*break\\s+vase\\s+-l\\s+\\(\\s*(?<x>\\d+)\\s*,\\s*(?<y>\\d+)\\s*\\)\\s*$"),

    PUT_ZOMBIE("^\\s*put\\s+zombie\\s+-t\\s+(?<zombieName>\\S+)\\s+-l\\s+\\(\\s*(?<x>\\d+)\\s*,\\s*(?<y>\\d+)\\s*\\)\\s*$"),

    BOWL("^\\s*bowl\\s+-i\\s+(?<index>\\d+)\\s+-l\\s+\\(\\s*(?<x>\\d+)\\s*,\\s*(?<y>\\d+)\\s*\\)\\s*$"),

    SHOW_ZOMBIES("^\\s*show\\s+zombies\\s*$");

    private final String pattern;

    MiniGameCommands(String pattern) {
        this.pattern = pattern;
    }

    public String getPattern() {
        return pattern;
    }
}