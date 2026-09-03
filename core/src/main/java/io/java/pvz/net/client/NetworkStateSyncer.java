package io.java.pvz.net.client;

import io.java.pvz.models.entities.Sun;
import io.java.pvz.models.entities.SunType;
import io.java.pvz.models.entities.plants.Plant;
import io.java.pvz.models.entities.zombies.Zombie;
import io.java.pvz.models.entities.zombies.ZombieState;
import io.java.pvz.models.game.Arena;
import io.java.pvz.models.game.GameSession;
import io.java.pvz.models.game.minigame.IZombieLevel;

import java.util.List;
import java.util.Map;

public class NetworkStateSyncer {

    @SuppressWarnings("unchecked")
    public static void syncWithServer(Map<String, Object> snapshot) {
        GameSession session = GameSession.getInstance();
        if (session == null || session.getArena() == null) return;
        Arena arena = session.getArena();

        if (session.getCurrentMode() instanceof IZombieLevel iZombieLevel) {
            Number redLineColValue = (Number) snapshot.get("redLineCol");
            if (redLineColValue != null) {
                iZombieLevel.setRedLineCol(redLineColValue.intValue());
            }
        }

        int serverSun = ((Number) snapshot.get("currentSun")).intValue();
        int diff = serverSun - session.getCurrentSun();
        if (diff != 0) session.addSun(diff);

        syncZombies(arena, (List<Map<String, Object>>) snapshot.get("zombies"));
        syncPlants(arena, (List<Map<String, Object>>) snapshot.get("plants"));
        syncSuns(arena, (List<Map<String, Object>>) snapshot.get("suns"));
    }

    private static void syncZombies(Arena arena, List<Map<String, Object>> zombiesData) {
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
                        } catch (Exception ignored) {
                        }
                    }
                }
            }
        }
    }

    private static void syncPlants(Arena arena, List<Map<String, Object>> plantsData) {
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

    private static void syncSuns(Arena arena, List<Map<String, Object>> sunsData) {
        if (sunsData != null) {
            java.util.Set<String> serverSunIds = new java.util.HashSet<>();

            for (Map<String, Object> sData : sunsData) {
                String sId = (String) sData.get("id");
                if (sId == null) continue;
                serverSunIds.add(sId);

                int sCol = ((Number) sData.get("col")).intValue();
                int sRow = ((Number) sData.get("row")).intValue();
                String typeStr = (String) sData.get("type");
                Boolean sFalling = (Boolean) sData.get("falling");

                Sun localSun = findSunById(arena, sId);
                if (localSun == null) {
                    SunType sunType;
                    try {
                        sunType = typeStr != null
                            ? SunType.valueOf(typeStr)
                            : SunType.NORMAL_SUN;
                    } catch (Exception e) {
                        sunType = SunType.NORMAL_SUN;
                    }

                    Sun newSun = new Sun(sunType, sCol, sRow);
                    newSun.setNetworkId(sId);
                    if (sFalling != null) newSun.setFalling(sFalling);
                    arena.addSun(newSun);
                } else {
                    if (sFalling != null && localSun.isFalling() != sFalling)
                        localSun.setFalling(sFalling);

                }
            }

            arena.getActiveSuns().removeIf(sun ->
                sun.getNetworkId() != null && !serverSunIds.contains(sun.getNetworkId()) && !sun.isCollected());
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

    private static Sun findSunById(Arena arena, String id) {
        for (Sun sun : arena.getActiveSuns())
            if (id.equals(sun.getNetworkId())) return sun;
        return null;
    }
}
