package models.enums.commands;

public enum SettingCommands {
    CHANGE_DIFFICULTY("^(?:\\s+)?menu\\s+settings\\s+change-difficulty\\s+-l\\s+(?<level>\\S+)(?:\\s+)?$");

    private final String pattern;

    SettingCommands(String pattern) {
        this.pattern = pattern;
    }

    public String getPattern() {
        return pattern;
    }
}
