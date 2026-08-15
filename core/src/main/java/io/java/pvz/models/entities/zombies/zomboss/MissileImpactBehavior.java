package io.java.pvz.models.entities.zombies.zomboss;

import io.java.pvz.models.fields.tiles.Tile;

public interface MissileImpactBehavior {
    void onImpact(Tile targetTile);
}
