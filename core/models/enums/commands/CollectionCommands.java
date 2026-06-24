package models.enums.commands;

public enum CollectionCommands implements Commands {
    SHOW_PLANTS("^\\s*menu\\s+collection\\s+show-plants\\s*$"),
    SHOW_ALL_PLANTS("^\\s*menu\\s+collection\\s+show-all-plants\\s*$"),
    SHOW_ZOMBIES("^\\s*menu\\s+collection\\s+show-zombies\\s*$"),
    SHOW_ALL_ZOMBIES("^\\s*menu\\s+collection\\s+show-all-zombies\\s*$"),
    SHOW_PLANT("^\\s*menu\\s+collection\\s+show-plant\\s+-p\\s+(?<plantName>.+)\\s*$"),
    SHOW_ZOMBIE("^\\s*menu\\s+collection\\s+show-zombie\\s+(?:-z|z)\\s+(?<zombieName>.+)\\s*$"), // Matches 'z' or '-z'
    UPGRADE_PLANT("^\\s*menu\\s+collection\\s+upgrade-plant\\s+-p\\s+(?<plantName>.+)\\s*$"),
    PURCHASE_PLANT("^\\s*menu\\s+collection\\s+purchase-plant\\s+-p\\s+(?<plantName>.+)\\s*$");

    private final String pattern;

    CollectionCommands(String pattern) {
        this.pattern = pattern;
    }

    public String getPattern() {
        return pattern;
    }
}