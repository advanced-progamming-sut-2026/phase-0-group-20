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

    @Override
    public void execute(Plant context, int currentTick) {
        int intervalInTicks = (int) (context.getActionInterval() * TimeManager.TICKS_PER_SECOND);

        if (intervalInTicks > 0 && (currentTick - lastLobTick) >= intervalInTicks) {
            int plantRow = context.getPlacedTile().getRow();
            float plantCol = context.getPlacedTile().getCol();
            boolean zombieFound = false;

            for (Zombie z : GameSession.getInstance().getArena().zombieInRow(plantRow)) {
                if (!z.isDead() && z.getX() / PhysicalConstants.TILE_UNIT_LENGTH >= plantCol) {
                    zombieFound = true;
                    break;
                }
            }

            if (zombieFound) {
                executeNewLobbedProjectile(context);
                lastLobTick = currentTick;
            }
        }
    }


    private void executeNewLobbedProjectile(Plant context) {
        String name = context.getName();
        float spawnX = context.getPlacedTile().getCol();
        float spawnY = context.getPlacedTile().getRow();

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
                    context,
                    type, effect, damage,
                    new Position(spawnX, spawnY),
                    1.5f, 0,
                    false,
                    true
            );
            GameSession.getInstance().getArena().addProjectile(projectile);
            System.out.println("🥔 " + name + " lobbed a " + type.name() + "!");
        }
    }
}
