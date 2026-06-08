package models.enums.commands;

public enum CheatCommands {
    ADD_DIAMOND("^(?:\\s+)?menu\\s+cheat\\s+add\\s+(?<amount>\\S+)\\s+diamond(?:\\s+)?$"),
    ADD_COIN("^(?:\\s+)?menu\\s+cheat\\s+add\\s+(?<amount>\\S+)\\s+coin(?:\\s+)?$");


    private final String pattern;

    CheatCommands(String pattern) {
        this.pattern = pattern;
    }

    public String getPattern() {
        return pattern;
    }
}

