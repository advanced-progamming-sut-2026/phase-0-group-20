package models.enums.commands;

public enum ShopMenuCommands implements Commands {
    SHOW_SHOP("^\\s*show\\s+shop\\s*$"),
    BUY_ITEM("^\\s*buy\\s+(?<item>.+?)\\s*$");

    private final String pattern;

    ShopMenuCommands(String pattern) {
        this.pattern = pattern;
    }

    public String getPattern() {
        return pattern;
    }

}
