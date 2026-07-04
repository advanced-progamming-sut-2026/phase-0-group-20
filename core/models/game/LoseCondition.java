package models.game;

public interface LoseCondition {
    boolean isLost(GameSession session);
}
