package io.java.pvz.models.entities.obstacle;

import io.java.pvz.models.entities.zombies.ZombieType;

public enum PushableObjectType {
    BARREL(ZombieType.BARREL_ROLLER),
    ARCADE_MACHINE(ZombieType.ARCADE),
    ICE_BLOCK(ZombieType.TROGLOBITE);

    private final ZombieType pusherZombieType;

    PushableObjectType(ZombieType pusherZombieType) {
        this.pusherZombieType = pusherZombieType;
    }

    public ZombieType getPusherZombieType() {
        return pusherZombieType;
    }
}
