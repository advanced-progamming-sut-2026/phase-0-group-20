package models.enums.commands;

public enum GameFlowCommands implements Commands {
    ADVANCE_TIME("^\\s*advance\\s+time\\s+-t\\s+(?<count>\\S+)\\s+ticks\\s*$"),

    COLLECT_SUN("^\\s*collect\\s+sun\\s+-l\\s+\\(\\s*(?<x>\\S+)\\s*,\\s*(?<y>\\S+)\\s*\\)\\s*$"),

    SHOW_SUN_AMOUNT("^\\s*show\\s+sun\\s+amount\\s*$"),

    CHEAT_ADD_SUN("^\\s*cheat\\s+add\\s+-n\\s+(?<count>\\S+)\\s+suns\\s*$"),

    RELEASE_NUKE("^\\s*release\\s+the\\s+nuke\\s*$"),

    PLANT_PLANT("^\\s*plant\\s+plant\\s+-t\\s+(?<plantName>\\S+)\\s+-l\\s+\\(\\s*(?<x>\\S+)\\s*,\\s*(?<y>\\S+)\\s*\\)\\s*$"),

    CHEAT_REMOVE_COOLDOWN("^\\s*cheat\\s+remove-cooldown\\s*$"),

    PLUCK_PLANT("^\\s*pluck\\s+plant\\s+-l\\s+\\(\\s*(?<x>\\S+)\\s*,\\s*(?<y>\\S+)\\s*\\)\\s*$"),

    FEED_PLANT("^\\s*feed\\s+plant\\s+-l\\s+\\(\\s*(?<x>\\S+)\\s*,\\s*(?<y>\\S+)\\s*\\)\\s*$"),

    CHEAT_ADD_PLANT_FOOD("^\\s*cheat\\s+add-plant-food\\s*$"),

    SHOW_MAP("^\\s*show\\s+map\\s*$"),

    SHOW_STATE("^\\s*show\\s+state\\s*$"),

    SHOW_PLANTS_STATUS("^\\s*show\\s+plants\\s+status\\s*$"),

    SHOW_TILE_STATUS("^\\s*show\\s+tile\\s+status\\s+-l\\s+\\(\\s*(?<x>\\S+)\\s*,\\s*(?<y>\\S+)\\s*\\)\\s*$");

    private final String pattern;

    GameFlowCommands(String pattern) {
        this.pattern = pattern;
    }

    public String getPattern() {
        return pattern;
    }
}
