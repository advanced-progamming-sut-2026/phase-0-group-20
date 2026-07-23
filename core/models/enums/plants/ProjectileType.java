package models.enums.plants;

public enum ProjectileType {
    PEA,
    ICE_PEA,
    ROTOBAGA_SEED,
    FIRE_PEA,
    GOO_PEA,
    MAGIC_BEAM,
    LIGHTNING_CLOUD,
    CABBAGE,
    CORN,
    BUTTER,
    MELON,
    WINTER_MELON,
    PEPPER,
    GRAPE,
    FUME,
    SPIKE,
    PLASMA_BALL,
    WALLNUT_BOWL,
    EXPLODE_NUT_BOWL,
    GIANT_NUT_BOWL;

    public static final int NORMAL_PEA_DAMAGE = 20;

    public static boolean isLobbed(ProjectileType p) {
        return p == CABBAGE || p == BUTTER || p == CORN || p == MELON || p == WINTER_MELON || p == PEPPER;
    }

    public static boolean isDeflectByJuggler(ProjectileType p) {
        return switch (p) {
            case PEA, ICE_PEA, ROTOBAGA_SEED, FIRE_PEA, GOO_PEA,
                 CABBAGE, CORN, BUTTER, MELON, WINTER_MELON,
                 PEPPER, GRAPE, SPIKE, WALLNUT_BOWL, EXPLODE_NUT_BOWL, GIANT_NUT_BOWL -> true;
            case MAGIC_BEAM, LIGHTNING_CLOUD, FUME, PLASMA_BALL -> false;
            default -> false;
        };
    }

    public static boolean isIceProjectile(ProjectileType type) {
        return type == ICE_PEA || type == WINTER_MELON;
    }

    public static boolean isFireProjectile(ProjectileType type) {
        return type == FIRE_PEA || type == PEPPER;
    }
}
