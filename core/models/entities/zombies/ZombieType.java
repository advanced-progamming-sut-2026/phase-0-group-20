package models.entities.zombies;

import models.game.adventure.SeasonType;

public enum ZombieType {
    // Ordinary
    NORMAL("ZombieDefault", null),
    CONE("ZombieArmor1", null),
    BUCKET("ZombieArmor2", null),
    BRICK("ZombieArmor4", null),
    DARK_ARMOR("ZombieDarkArmor3", null), //knight
    GARGANTUAR("ZombieGargantuar", null),
    IMP("ZombieImp", null),
    ALL_STAR("ZombieModernAllStar", null),
    ARCADE("ZombieArcade", null),
    JANE("ZombieLostCityJane", null), // Zombie Parasol
    CRYSTAL_SKULL("ZombieCrystalSkull", null), //Turquoise Zombie
    PROSPECTOR("ZombieProspector", null),
    PIANIST("ZombiePiano", null),
    NEWSPAPER("ZombieNewspaper", null),
    BARREL_ROLLER("ZombieBarrelRoller", null), // WARNING : We don't have this yet


    // Ancient Egypt
    RA("ZombieRa", SeasonType.ANCIENT_EGYPT),
    EXPLORER("ZombieExplorer", SeasonType.ANCIENT_EGYPT),
    TOMB_RAISER("ZombieTombRaiser", SeasonType.ANCIENT_EGYPT),

    // frostbite Caves
    DODO("ZombieIceAgeDodo", SeasonType.FROZEN_CAVES),
    HUNTER("ZombieIceAgeHunter", SeasonType.FROZEN_CAVES),
    TROGLOBITE("ZombieIceAgeTroglobite", SeasonType.FROZEN_CAVES),

    // big wave beach
    FISHERMAN("ZombieBeachFisherman", SeasonType.BIG_WAVE_BEACH),
    OCTOPUS("ZombieBeachOctopus", SeasonType.BIG_WAVE_BEACH),
    SNORKEL("ZombieBeachSnorkel", SeasonType.BIG_WAVE_BEACH),

    // dark age
    JUGGLER("ZombieDarkJuggler", SeasonType.DARK_AGES),
    WIZARD("ZombieWizard", SeasonType.DARK_AGES),
    KING("ZombieDarkKing", SeasonType.DARK_AGES),
    IMP_DRAGON("ZombieDarkImpDragon", SeasonType.DARK_AGES),

    // minigame
    SUN_PRODUCER("ZombieSunProducer", null);


    private final String jsonAlias;
    private final SeasonType type;

    ZombieType(String jsonAlias, SeasonType type) {
        this.jsonAlias = jsonAlias;
        this.type = type;
    }

    public static ZombieType fromAlias(String alias) {
        for (ZombieType t : values()) {
            if (t.jsonAlias.equalsIgnoreCase(alias)) return t;
        }
        throw new IllegalArgumentException("Unknown zombie alias: " + alias);
    }

    public String getJsonAlias() {
        return jsonAlias;
    }

    public SeasonType getType() {
        return type;
    }
}
