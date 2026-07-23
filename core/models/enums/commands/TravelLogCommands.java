package models.enums.commands;

public enum TravelLogCommands implements Commands {
    CHANGE_PAGE("^\\s*travel\\s+log\\s+page\\s+(?<pageName>\\S+)\\s*$"),
    ENTER_MINIGAME("^\\s*enter\\s+minigame\\s+(?<name>\\S+)\\s+-l\\s+(?<level>\\S+)\\s*$"),
    PRINT_MINIGAME("^\\s*print\\s+minigames?\\s*$"),
    SHOW_PAGE("^\\s*travel\\s+log\\s+show\\s+page\\s*$");

    private final String pattern;

    TravelLogCommands(String pattern) {
        this.pattern = pattern;
    }

    @Override
    public String getPattern() {
        return pattern;
    }
}