package io.java.pvz.net;

public class NetworkIdGenerator {

    public static String generatePlantId(String plantName, int col, int row, int tick) {
        return "PLANT_" + plantName + "_" + col + "_" + row + "_" + tick;
    }

    public static String generateZombieId(String zombieType, int col, int row, int tick) {
        return "ZOM_" + zombieType + "_" + col + "_" + row + "_" + tick;
    }

    public static String generateProjectileId(String ownerId, int tick) {
        return "PROJ_" + ownerId + "_" + tick;
    }

    public static String generateSunId(int col, int row, int tick) {
        return "SUN_" + col + "_" + row + "_" + tick;
    }
}
