package models.enums.commands;

public enum LeaderBoardCommands implements Commands {
    SHOW("^\\s*show\\s*$"),
    SORT("^\\s*sort\\s+-t\\s+(?<type>score|minigame|season|quests)\\s*$");

    private final String pattern;

    LeaderBoardCommands(String pattern) {
        this.pattern = pattern;
    }

    @Override
    public String getPattern() {
        return pattern;
    }
}
