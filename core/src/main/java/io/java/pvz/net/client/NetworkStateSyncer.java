package io.java.pvz.net.client;

import io.java.pvz.models.entities.plants.Plant;
import io.java.pvz.models.entities.zombies.Zombie;
import io.java.pvz.models.entities.zombies.ZombieState;
import io.java.pvz.models.game.Arena;
import io.java.pvz.models.game.GameSession;

import java.util.List;
import java.util.Map;

public class NetworkStateSyncer {

    @SuppressWarnings("unchecked")
    public static void syncWithServer(Map<String, Object> snapshot) {
        GameSession session = GameSession.getInstance();
        if (session == null || session.getArena() == null) return;
        Arena arena = session.getArena();

        if (session.getCurrentMode() instanceof io.java.pvz.models.game.minigame.IZombieLevel iZombieLevel) {
            Number redLineColValue = (Number) snapshot.get("redLineCol");
            if (redLineColValue != null) {
                iZombieLevel.setRedLineCol(redLineColValue.intValue());
            }
        }

        int serverSun = ((Number) snapshot.get("currentSun")).intValue();
        int diff = serverSun - session.getCurrentSun();
        if (diff != 0) session.addSun(diff);

        List<Map<String, Object>> zombiesData = (List<Map<String, Object>>) snapshot.get("zombies");
        if (zombiesData != null) {
            for (Map<String, Object> zData : zombiesData) {
                String zId = (String) zData.get("id");
                float zX = ((Number) zData.get("x")).floatValue();
                int zHp = ((Number) zData.get("hp")).intValue();
                String stateStr = (String) zData.get("state");

                Zombie localZ = findZombieById(arena, zId);
                if (localZ != null && !localZ.isDead()) {

                    if (Math.abs(localZ.getX() - zX) > 5.0f)
                        localZ.setX(zX);

                    if (localZ.getHealth() != zHp)
                        localZ.setHealth(zHp);

                    if (stateStr != null) {
                        try {
                            ZombieState sState = ZombieState.valueOf(stateStr);
                            if (sState == ZombieState.EATING && !localZ.isAttacking()) {
                                localZ.setAttacking(true);
                            } else if (sState != ZombieState.EATING && localZ.isAttacking()) {
                                localZ.setAttacking(false);
                            }
                        } catch (Exception ignored) {}
                    }
                }
            }
        }

        List<Map<String, Object>> plantsData = (List<Map<String, Object>>) snapshot.get("plants");
        if (plantsData != null) {
            for (Map<String, Object> pData : plantsData) {
                String pId = (String) pData.get("id");
                int pHp = ((Number) pData.get("hp")).intValue();

                Plant localP = findPlantById(arena, pId);
                if (localP != null && !localP.isDead()) {
                    if (localP.getCurrentHp() != pHp) {
                        localP.setCurrentHp(pHp);
                    }
                }
            }
        }
    }

    private static Zombie findZombieById(Arena arena, String id) {
        for (Zombie zombie : arena.getActiveZombies())
            if (id.equals(zombie.getNetworkId())) return zombie;
        return null;
    }

    private static Plant findPlantById(Arena arena, String id) {
        for (Plant plant : arena.getActivePlants())
            if (id.equals(plant.getNetworkId())) return plant;
        return null;
    }
}
