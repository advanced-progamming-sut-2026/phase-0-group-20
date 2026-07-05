package models.enums.commands;

public enum GreenHouseCommands implements Commands {
    SHOW_GREENHOUSE("^\\s*show\\s+greenhouse\\s*$"),
    PLANT_POT("^\\s*plant\\s+pot\\s+at\\s*\\(\\s*(?<x>\\S+)\\s*,\\s*(?<y>\\S+)\\s*\\)\\s*$"),
    COLLECT_POT("^\\s*collect\\s*\\(\\s*(?<x>\\S+)\\s*,\\s*(?<y>\\S+)\\s*\\)\\s*$"),
    GROW_POT("^\\s*grow\\s*\\(\\s*(?<x>\\S+)\\s*,\\s*(?<y>\\S+)\\s*\\)\\s*$");

    private final String pattern;

    GreenHouseCommands(String pattern) {
        this.pattern = pattern;
    }

    public String getPattern() {
        return pattern;
    }
}