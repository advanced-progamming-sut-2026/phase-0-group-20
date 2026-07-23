package models.enums.commands;

public enum ProfileCommands implements Commands {
    CHANGE_USERNAME("^(?:\\s+)?menu\\s+profile\\s+change-username\\s+-u\\s+(?<username>\\S+)(?:\\s+)?$"),
    CHANGE_NICKNAME("^(?:\\s+)?menu\\s+profile\\s+change-nickname\\s+-u\\s+(?<nickname>.+)$"),
    CHANGE_EMAIL("^(?:\\s+)?menu\\s+profile\\s+change-email\\s+-e\\s+(?<email>\\S+)(?:\\s+)?$"),
    CHANGE_PASSWORD("^(?:\\s+)?menu\\s+profile\\s+change-password\\s+" +
            "-p\\s+(?<new>\\S+)\\s+-o\\s+(?<old>\\S+)(?:\\s+)?$"),
    SHOW_INFO("^(?:\\s+)?menu\\s+profile\\s+show-info(?:\\s+)?$");

    private final String pattern;

    ProfileCommands(String pattern) {
        this.pattern = pattern;
    }

    public String getPattern() {
        return pattern;
    }
}
