package models.enums.commands;

public enum MainMenuCommands {
    LOGOUT("^(?:\\s+)?menu\\s+logout(?:\\s+)$");

    private final String pattern;

    MainMenuCommands(String pattern) {
        this.pattern = pattern;
    }

    public String getPattern() {
        return pattern;
    }
}
