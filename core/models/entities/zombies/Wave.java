package models.entities.zombies;

import java.util.ArrayList;
import java.util.List;

public class Wave {
    private boolean lastWave;
    private int currentNumber;
    private List<Zombie> zombies;
    private int difficulty;
    private int totalWaveCost;
    private int totalBaseHp;

    public Wave(int currentNumber, boolean lastWave) {
        this.currentNumber = currentNumber;
        this.lastWave = lastWave;
        this.zombies = new ArrayList<>();
    }

    public void addZombie(Zombie zombie) {
        this.zombies.add(zombie);
        this.totalWaveCost += zombie.getWaveCost();
        this.totalBaseHp += zombie.getBaseHp();
    }

    public boolean isLastWave() {
        return lastWave;
    }

    public void setLastWave(boolean lastWave) {
        this.lastWave = lastWave;
    }

    public int getCurrentNumber() {
        return currentNumber;
    }

    public void setCurrentNumber(int currentNumber) {
        this.currentNumber = currentNumber;
    }

    public List<Zombie> getZombies() {
        return zombies;
    }

    public void setZombies(List<Zombie> zombies) {
        this.zombies = zombies;
    }

    public int getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(int difficulty) {
        this.difficulty = difficulty;
    }

    public int getTotalWaveCost() {
        return totalWaveCost;
    }

    public void setTotalWaveCost(int totalWaveCost) {
    }

    public int getTotalBaseHp() {
        return totalBaseHp;
    }

    public void setTotalBaseHp(int totalBaseHp) {
    }

}
