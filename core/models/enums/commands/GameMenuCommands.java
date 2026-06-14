package models.enums.commands;

public enum GameMenuCommands {
    ENTER_CHAPTER("^(?:\\s+)?menu\\s+enter\\s+chapter\\s+-c\\s+(?<chaptername>\\S+)(?:\\s+)$"),
    ENTER_GREEN_HOUSE("^(?:\\s+)?menu\\s+greenhouse(?:\\s+)?$"),
    ENTER_TRAVEL_LOG("^(?:\\s+)?menu\\s+travel-log(?:\\s+)?$"),
    ENTER_LEADERBOARD("^(?:\\s+)?menu\\s+leaderboard(?:\\s+)?$"),
    ENTER_COIN_WALLET("^(?:\\s+)?menu\\s+coin-wallet(?:\\s+)?$"),
    ENTER_GEM_WALLET("^(?:\\s+)?menu\\s+gem-wallet(?:\\s+)?$");

    private final String pattern;

    GameMenuCommands(String pattern) {
        this.pattern = pattern;
    }

    public String getPattern() {
        return pattern;
    }
}
