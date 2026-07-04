package models.entities.plants.strategy.category_strategy;

import models.entities.plants.Plant;
import models.entities.plants.strategy.IPlantStrategy;
import models.entities.projectiles.*;
import models.entities.zombies.Zombie;
import models.enums.plants.ProjectileType;
import models.game.GameSession;
import models.timeManager.TimeManager;

public class LobberStrategy implements IPlantStrategy {
    private int lastLobTick = 0;

    @Override
    public void execute(Plant context, int currentTick, GameSession gameSession) {
        int intervalInTicks = (int) (context.getActionInterval() * TimeManager.TICKS_PER_SECOND);

        if (intervalInTicks > 0 && (currentTick - lastLobTick) >= intervalInTicks) {
            int plantRow = context.getPlacedTile().getRow();
            double plantCol = context.getPlacedTile().getCol();
            boolean zombieFound = false;

            for (Zombie z : gameSession.zombieInRow(plantRow)) {
                if (!z.isDead() && z.getX() >= plantCol) {
                    zombieFound = true;
                    break;
                }
            }

            if (zombieFound) {
                executeNewLobbedProjectile(context, gameSession);
                lastLobTick = currentTick;
            }
        }
    }


    private void executeNewLobbedProjectile(Plant context, GameSession gameSession) {
        String name = context.getName();
        double spawnX = context.getPlacedTile().getCol();
        double spawnY = context.getPlacedTile().getRow();

        ProjectileType type = null;
        ProjectileEffect effect = new NormalEffect();
        int damage = 0;

        switch (name) {
            case "Cabbage-pult":
                type = ProjectileType.CABBAGE;
                damage = 40;
                break;
            case "Kernel-pult": //25% for butter and 75% for corn
                if (Math.random() < 0.25) {
                    type = ProjectileType.BUTTER;
                    damage = 40;
                     effect = new ButterEffect();
                } else {
                    type = ProjectileType.CORN;
                    damage = 20;
                }
                break;
            case "Melon-pult":
                type = ProjectileType.MELON;
                damage = 80;
                effect = new SplashEffect();
                break;
            case "Winter Melon":
                type = ProjectileType.WINTER_MELON;
                damage = 80;
                effect = new IceSplashEffect();
                break;
            case "Pepper-pult":
                type = ProjectileType.PEPPER;
                damage = 50;
                effect = new FireSplashEffect();
                break;
        }

        if (type != null) {
            Projectile projectile = new Projectile(
                    type, effect, gameSession, damage,
                    spawnX, spawnY,
                    1.5, 0,
                    false,
                    true
            );
            gameSession.addProjectile(projectile);
            System.out.println("🥔 " + name + " lobbed a " + type.name() + "!");
        }
    }
}
