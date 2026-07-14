package models.greenhouse;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class GreenHouse {
    private Pot[][] pots = new Pot[4][5];

    public GreenHouse() {
        for (int i = 0; i < pots.length; i++) {
            for (int j = 0; j < pots[i].length; j++) {
                pots[i][j] = new Pot();
                if (i >= 1)
                    pots[i][j].setPotCondition(PotCondition.LOCKED);
                else {
                    pots[i][j].setPotCondition(PotCondition.EMPTY);
                }
            }
        }
    }

    public Pot[][] getPots() {
        return pots;
    }

    public void setPots(Pot[][] pots) {
        this.pots = pots;
    }

    public String showGreenHouse() {
        StringBuilder sb = new StringBuilder();
        sb.append("--- Greenhouse Status ---\n");

        for (int i = 0; i < pots.length; i++) {
            for (int j = 0; j < pots[i].length; j++) {
                Pot currentPot = pots[i][j];

                sb.append("Pot [").append(i + 1).append("][").append(j + 1).append("]: ");

                switch (currentPot.getPotCondition()) {
                    case LOCKED -> sb.append("Locked");
                    case EMPTY -> sb.append("Empty");
                    case PLANTED -> {
                        String plantName;
                        if (currentPot.isItMari()) {
                            plantName = "Marigold";
                        } else if (currentPot.getPlantedPlant() != null) {
                            plantName = currentPot.getPlantedPlant().getClass().getSimpleName();
                        } else {
                            plantName = "Unknown";
                        }

                        sb.append("Growing - Plant: ").append(plantName)
                                .append(" | Time left: ").append(currentPot.getRemainedTimeToCollect()).append(" ticks");
                    }
                    case COLLECTABLE -> sb.append("Ready");
                    default -> sb.append("Unknown State");
                }
                sb.append("\n");
            }
        }
        return sb.toString();
    }

    public Pot getSpecificPot(int x, int y) {
        return pots[x][y];
    }
}
