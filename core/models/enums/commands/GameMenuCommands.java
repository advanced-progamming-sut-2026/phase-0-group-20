package models.enums.commands;

public enum GameMenuCommands implements Commands {
    ENTER_CHAPTER("^\\s*menu\\s+enter\\s+chapter\\s+-c\\s+(?<chaptername>.+?)\\s*$"),
    ENTER_LEVEL("^\\s*menu\\s+enter\\s+level\\s+-l\\s+(?<levelNumber>.+?)\\s*$"), // menu enter level -l NUMBER
    ENTER_DAILY_SCORE_LEVEL("^\\s*menu\\s+enter\\s+daily\\s+score\\s+level\\s*$"),
    ENTER_TRAVEL_LOG("^\\s*menu\\s+travel-log\\s*$"),
    ENTER_LEADERBOARD("^\\s*menu\\s+leaderboard\\s*$"),
    ENTER_COIN_WALLET("^\\s*menu\\s+coin-wallet\\s*$"),
    ENTER_GEM_WALLET("^\\s*menu\\s+gem-wallet\\s*$"),
    CHEAT_ADD("^\\s*menu\\s+cheat\\s+add\\s+(?<amount>\\d+)\\s+(?<type>coin|diamond)\\s*$");

    private final String pattern;

    GameMenuCommands(String pattern) {
        this.pattern = pattern;
    }

    public String getPattern() {
        return pattern;
    }
}
