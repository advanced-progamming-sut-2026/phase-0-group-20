package io.java.pvz.net.server.game;

import io.java.pvz.models.entities.Sun;
import io.java.pvz.models.entities.plants.Plant;
import io.java.pvz.models.entities.projectiles.Projectile;
import io.java.pvz.models.entities.zombies.Zombie;
import io.java.pvz.models.fields.Brain;
import io.java.pvz.models.game.Arena;
import io.java.pvz.models.game.GameSession;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class MatchStateSnapshotBuilder {

    private MatchStateSnapshotBuilder() {
    }

    public static Map<String, Object> build(NetworkMatchState match) {
        GameSession session = match.getGameSession();
        Arena arena = session.getArena();

        Map<String, Object> snapshot = new HashMap<>();
        snapshot.put("matchId", match.getMatchId());
        snapshot.put("tick", session.getTimeManager().getCurrentTick());
        snapshot.put("timeLimitTicks", match.getLevel().getSurvivalTimeLimitTicks());
        snapshot.put("redLineCol", match.getLevel().getRedLineCol());
        snapshot.put("currentSun", session.getCurrentSun());
        snapshot.put("rows", arena.getRows());
        snapshot.put("cols", arena.getCols());

        snapshot.put("plants", arena.getActivePlants().stream().map(MatchStateSnapshotBuilder::plant).toList());
        snapshot.put("zombies", arena.getActiveZombies().stream().map(MatchStateSnapshotBuilder::zombie).toList());
        snapshot.put("projectiles",
            arena.getActiveProjectiles().stream().map(MatchStateSnapshotBuilder::projectile).toList());
        snapshot.put("suns", arena.getActiveSuns().stream()
            .filter(s -> !s.isCollected()).map(MatchStateSnapshotBuilder::sun).toList());
        snapshot.put("brains", brains(arena));

        return snapshot;
    }

    private static Map<String, Object> plant(Plant p) {
        Map<String, Object> m = new HashMap<>();
        m.put("id", System.identityHashCode(p));
        m.put("name", p.getName());
        m.put("row", p.getPlacedTile() != null ? p.getPlacedTile().getRow() : -1);
        m.put("col", p.getPlacedTile() != null ? p.getPlacedTile().getCol() : -1);
        m.put("hp", p.getCurrentHp());
        m.put("maxHp", p.getMaxHp());
        m.put("stackCount", p.getStackCount());
        return m;
    }

    private static Map<String, Object> zombie(Zombie z) {
        Map<String, Object> m = new HashMap<>();
        m.put("id", System.identityHashCode(z));
        m.put("type", z.getType() != null ? z.getType().name() : z.getName());
        m.put("row", z.getRow());
        m.put("x", z.getX());
        m.put("hp", z.getHealth());
        m.put("state", z.getState() != null ? z.getState().name() : null);
        m.put("dead", z.isDead());
        return m;
    }

    private static Map<String, Object> projectile(Projectile p) {
        Map<String, Object> m = new HashMap<>();
        m.put("id", System.identityHashCode(p));
        m.put("type", p.getType() != null ? p.getType().name() : null);
        m.put("x", p.getX());
        m.put("y", p.getY());
        return m;
    }

    private static Map<String, Object> sun(Sun s) {
        Map<String, Object> m = new HashMap<>();
        m.put("id", System.identityHashCode(s));
        m.put("row", s.getRow());
        m.put("col", s.getCol());
        m.put("value", s.getType() != null ? s.getType().getValue() : 0);
        return m;
    }

    private static List<Map<String, Object>> brains(Arena arena) {
        List<Map<String, Object>> list = new ArrayList<>();
        for (int row = 0; row < arena.getRows(); row++) {
            Brain brain = arena.getBrainInRow(row);
            Map<String, Object> m = new HashMap<>();
            m.put("row", row);
            m.put("eaten", brain == null || brain.isEaten());
            list.add(m);
        }
        return list;
    }
}
