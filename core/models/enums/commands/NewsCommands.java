package models.enums.commands;

public enum NewsCommands implements Commands {
    SHOW_UNREAD_NEWS("^(?:\\s+)?menu\\s+news\\s+show-unread(?:\\s+)?$"),
    SHOW_ALL_NEWS("^(?:\\s+)?menu\\s+news\\s+show-all(?:\\s+)?$");


    private final String pattern;

    NewsCommands(String pattern) {
        this.pattern = pattern;
    }

    public String getPattern() {
        return pattern;
    }
}
