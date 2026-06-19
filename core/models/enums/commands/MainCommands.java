package models.enums.commands;

public enum MainCommands implements Commands {
    ENTER_MENU("^(?:\\s+)?menu\\s+enter\\s+(?<name>.+)$"),
    SHOW_CURRENT_MENU("^(?:\\s+)?menu\\s+show\\s+current(?:\\s+)?$"),
    EXIT_MENU("^(?:\\s+)?menu\\s+exit(?:\\s+)?$");

    private final String pattern;

    MainCommands(String pattern) {
        this.pattern = pattern;
    }

    public String getPattern() {
        return pattern;
    }
}
