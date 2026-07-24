package models.entities.plants.strategy.category_strategy;

import models.Position;
import models.entities.plants.Plant;
import models.entities.plants.strategy.IPlantStrategy;
import models.entities.projectiles.*;
import models.entities.zombies.Zombie;
import models.enums.PhysicalConstants;
import models.enums.plants.ProjectileType;
import models.game.GameSession;
import models.timeManager.TimeManager;

public class LobberStrategy implements IPlantStrategy {
    private int lastLobTick = 0;
    private int splashDamageBonus = 0;
    private float paperRadiusBonus = 0;

    private float butterChanceBonus = 0.0f;

    @Override
    public void execute(Plant context, int currentTick) {
        int intervalInTicks = (int) (context.getActionInterval() * TimeManager.TICKS_PER_SECOND);

        if (intervalInTicks > 0 && (currentTick - lastLobTick) >= intervalInTicks) {
            int plantRow = context.getPlacedTile().getRow();
            float plantCol = context.getPlacedTile().getCol();

            Zombie targetZombie = null;
            float minDistance = Float.MAX_VALUE;

            for (Zombie z : GameSession.getInstance().getArena().zombieInRow(plantRow)) {
                if (!z.isDead()) {
                    float distance = Math.abs(z.getCol() - plantCol);
                    if (distance < minDistance) {
                        minDistance = distance;
                        targetZombie = z;
                    }
                }
            }

            if (targetZombie != null) {
                executeNewLobbedProjectile(context, targetZombie);
                lastLobTick = currentTick;
            }
        }
    }


    private void executeNewLobbedProjectile(Plant context, Zombie targetZombie) {
        String name = context.getName();
        float spawnX = context.getPlacedTile().getCol();
        float spawnY = context.getPlacedTile().getRow();

        ProjectileType type = null;
        int damage = 0;
        ProjectileEffect effect = new NormalEffect();

        switch (name) {
            case "Cabbage-pult":
                type = ProjectileType.CABBAGE;
                damage = context.getDamage();
                break;
            case "Kernel-pult": //25% for butter and 75% for corn
                double finalButterChance = 0.25 + butterChanceBonus;
                if (Math.random() < finalButterChance) {
                    type = ProjectileType.BUTTER;
                    damage = context.getDamage();
                    effect = new ButterEffect();
                } else {
                    type = ProjectileType.CORN;
                    damage = context.getDamage();
                }
                break;
            case "Melon-pult":
                type = ProjectileType.MELON;
                damage = context.getDamage();
                effect = new SplashEffect(damage + splashDamageBonus);
                break;
            case "Winter Melon":
                type = ProjectileType.WINTER_MELON;
                damage = context.getDamage();
                effect = new IceSplashEffect(damage + splashDamageBonus);
                break;
            case "Pepper-pult":
                type = ProjectileType.PEPPER;
                damage = context.getDamage();
                effect = new FireSplashEffect(paperRadiusBonus + 1.5);
                break;
        }

        if (type != null) {
            Projectile projectile = new Projectile(
                    context,
                    type, effect, damage,
                    new Position(spawnX, spawnY),
                    0, 0,
                    false,
                    true // canPassObstacles
            );
            projectile.setHomingTarget(targetZombie, 1.5f);
            GameSession.getInstance().getTimeManager().registerNewTicker(projectile);
            GameSession.getInstance().getArena().addProjectile(projectile);
            notify("🥔 " + name + " lobbed a " + type.name() + "!");
        }
    }

    public void increaseSplashDamage(int damage) {
        this.splashDamageBonus += damage;
    }

    public void increaseWarmRadius(float r) {
        this.paperRadiusBonus += r;
    }

    public void increaseButterChance(float chance) {
        this.butterChanceBonus += chance;
    }
}
