package models.entities.zombies;

public enum ZombieType {
    NORMAL("ZombieDefault"),
    CONE("ZombieArmor1"),
    BUCKET("ZombieArmor2"),
    BRICK("ZombieArmor4"),
    GARGANTUAR("ZombieGargantuar"),
    IMP("ZombieImp"),
    RA("ZombieRa"),
    EXPLORER("ZombieExplorer"),
    TOMB_RAISER("ZombieTombRaiser"),

    DODO("ZombieIceAgeDodo"),
    HUNTER("ZombieIceAgeHunter"),
    TROGLOBITE("ZombieIceAgeTroglobite"),

    FISHERMAN("ZombieBeachFisherman"),
    OCTOPUS("ZombieBeachOctopus"),
    SNORKEL("ZombieBeachSnorkel"),

    JUGGLER("ZombieDarkJuggler"),
    WIZARD("ZombieWizard"),
    KING("ZombieDarkKing"),
    IMP_DRAGON("ZombieDarkImpDragon"),

    ALL_STAR("ZombieModernAllStar"),
    JANE("ZombieLostCityJane"), // Zombie Parasol
    CRYSTAL_SKULL("ZombieCrystalSkull"), //Turquoise Zombie
    PROSPECTOR("ZombieProspector"),
    PIANIST("ZombiePiano"),
    NEWSPAPER("ZombieNewspaper"),
    ARCADE("ZombieArcade"),
    BARREL_ROLLER(""), // WARNING : We don't have this yet

    DARK_ARMOR("ZombieDarkArmor3"); //knight

    private final String jsonAlias;

    ZombieType(String jsonAlias) {
        this.jsonAlias = jsonAlias;
    }

    public String getJsonAlias() {
        return jsonAlias;
    }

    public static ZombieType fromAlias(String alias) {
        for (ZombieType t : values()) {
            if (t.jsonAlias.equalsIgnoreCase(alias)) return t;
        }
        throw new IllegalArgumentException("Unknown zombie alias: " + alias);
    }
}
