package models.enums.commands;

public enum SignUpCommands implements Commands {
    REGISTER("^(?:\\s+)?register\\s+-u\\s+(?<username>\\S+)\\s+-p\\s+(?<password>\\S+)\\s+(?<confirm>\\S+)\\s+" +
            "-n\\s+(?<nickname>.{3,30})\\s+-e\\s+(?<email>\\S+)\\s+-g\\s+(?<gender>\\S+)(?:\\s+)?$"),
    PICK_QUESTION("^(?:\\s+)?pick\\s+question\\s+-q\\s+(?<number>\\S+)\\s+-a\\s+(?<answer>.+)\\s+" +
            "-c\\s+(?<confirm>.+)$");

    private final String pattern;

    SignUpCommands(String pattern) {
        this.pattern = pattern;
    }

    public String getPattern() {
        return this.pattern;
    }
}
