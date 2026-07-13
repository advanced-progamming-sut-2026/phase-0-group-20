package models.enums.commands;

public enum TravelLogCommands implements Commands {
    SHOW_PAGE("^\\s*travel\\s+log\\s+page\\s+(?<pageName>\\S+)\\s*$"),
    NEXT_PAGE("^\\s*travel\\s+log\\s+next\\s+page\\s*$");

    private final String pattern;

    TravelLogCommands(String pattern) {
        this.pattern = pattern;
    }

    @Override
    public String getPattern() {
        return pattern;
    }
}