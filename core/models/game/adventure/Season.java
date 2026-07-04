package models.game.adventure;

import models.fields.modifiers.BigWaveModifier;
import models.fields.modifiers.DarkAgesModifier;
import models.fields.modifiers.EgyptModifier;
import models.fields.modifiers.IceCaveModifier;
import models.fields.modifiers.SeasonModifier;

/**
 * The environment side of a chapter: maps a SeasonType to its
 * SeasonModifier (graves, icy wind, water waves, necromancy, ...)
 * and to season-wide rules like "no sun falls at night".
 */
public class Season {

    private final SeasonType type;
    private final SeasonModifier modifier;

    public Season(SeasonType type) {
        this.type = type;
        this.modifier = createModifier(type);
    }

    public static SeasonModifier createModifier(SeasonType type) {
        return switch (type) {
            case ANCIENT_EGYPT -> new EgyptModifier();
            case FROZEN_CAVES -> new IceCaveModifier();
            case BIG_WAVE_BEACH -> new BigWaveModifier();
            case DARK_AGES -> new DarkAgesModifier();
        };
    }

    public static String displayName(SeasonType type) {
        return switch (type) {
            case ANCIENT_EGYPT -> "Ancient Egypt";
            case FROZEN_CAVES -> "Frozen Caves";
            case BIG_WAVE_BEACH -> "Big Wave Beach";
            case DARK_AGES -> "Dark Ages";
        };
    }

    /** Dark Ages is night: no sun falls from the sky, sunflowers are the only source. */
    public boolean isNight() { return type == SeasonType.DARK_AGES; }

    public SeasonType getType() { return type; }
    public SeasonModifier getModifier() { return modifier; }
    public String getDisplayName() { return displayName(type); }
}
