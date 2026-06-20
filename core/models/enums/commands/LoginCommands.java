package models.enums.commands;

public enum LoginCommands implements Commands {
    LOGIN("^\\s*login\\s+-u\\s+(?<username>\\S+)\\s+-p\\s+(?<password>\\S+)\\s*(?:\\s+(?<stayLoggedIn>-stay-logged-in))?\\s*$"),
    FORGOT_PASSWORD("^(?:\\s+)?forget\\s+password\\s+-u\\s+(?<username>\\S+)\\s+-e\\s+(?<email>\\S+)(?:\\s+)?$"),
    ANSWER_QUESTION("^(?:\\s+)?answer\\s+-a\\s+(?<answer>.+)$"),
    RESET_PASSWORD("\\s*(?<password>\\S+)\\s+(?<confirm>\\S+)\\s*");

    private final String pattern;

    LoginCommands(String pattern) {
        this.pattern = pattern;
    }

    public String getPattern() {
        return pattern;
    }
}
