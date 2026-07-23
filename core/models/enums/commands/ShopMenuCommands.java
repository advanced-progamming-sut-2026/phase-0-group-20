package models.enums.commands;

public enum ShopMenuCommands implements Commands {
    SHOP_LIST("^\\s*shop\\s+list\\s*$"),

    SHOP_DAILY("^\\s*shop\\s+daily\\s*$"),

    SHOP_BUY("^\\s*shop\\s+buy\\s+-?i\\s+(?<itemId>.+?)\\s+-n\\s+(?<count>\\d+)(?:\\s+-t\\s+(?<plantType>.+?))?\\s*$"),

    SHOP_LIST_PLANTS("^\\s*shop\\s+list\\s+plants\\s*$"),

    SHOP_BUY_PLANT("^\\s*shop\\s+buy\\s+plant\\s+-p\\s+(?<plantName>.+?)\\s*$");

    private final String pattern;

    ShopMenuCommands(String pattern) {
        this.pattern = pattern;
    }

    public String getPattern() {
        return pattern;
    }

}
