package models.enums.commands;

import java.util.regex.Matcher;

public enum PlantSelectionCommands implements Commands{
    SHOW_ALL_PLANTS("^\\s*show\\s+all\\s+plants\\s*$"),
    SHOW_AVAILABLE_PLANTS("^\\s*show\\s+available\\s+plants\\s*$"),
    ADD_PLANT("^\\s*add\\s+plant\\s+-t\\s+(?<type>.+?)\\s*$"),
    REMOVE_PLANT("^\\s*remove\\s+plant\\s+-t\\s+(?<type>.+?)\\s*$"),
    BOOST_PLANT("^\\s*boost\\s+plant\\s+-t\\s+(?<type>.+?)\\s*$"),
    START_GAME("^\\s*start\\s+game\\s*$");

    private final String pattern;

    PlantSelectionCommands(String pattern) {
        this.pattern = pattern;
    }

    public String getPattern() {
        return pattern;
    }
}
